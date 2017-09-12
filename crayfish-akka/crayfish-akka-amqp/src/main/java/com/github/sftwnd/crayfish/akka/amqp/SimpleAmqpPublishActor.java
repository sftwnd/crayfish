package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import com.github.sftwnd.crayfish.amqp.publish.AMQPPublish;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ReturnListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.sftwnd.crayfish.akka.amqp.SimpleAmqpPublishActor.State.Init;
import static com.github.sftwnd.crayfish.akka.amqp.SimpleAmqpPublishActor.State.Consume;
import static com.github.sftwnd.crayfish.akka.amqp.SimpleAmqpPublishActor.State.Connected;

/**
 * Created by ashindarev on 04.08.16.
 */
@Component("crayfish-SimpleAmqpPublishActor")
@Scope("prototype")
@DependsOn("crayfish-actorSystem")
public class SimpleAmqpPublishActor extends AbstractFSM<SimpleAmqpPublishActor.State, SimpleAmqpPublishActor.Data> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAmqpPublishActor.class);

    protected enum State { Init, Consume, Connected }

    public static class Data<Payload> {

        private String connectionName;
        private Channel channel;

        public Data(String connectionName, Channel channel) {
            assert connectionName != null;
            this.connectionName = connectionName;
            this.channel = channel;
        }

        public String getConnectionName() {
            return connectionName;
        }

        public Channel getChannel() {
            return channel;
        }

    }

    public static class Complete {
    }

    public static class Disconnected {
    }

    public static final Complete     COMPLETE = new Complete();
    public static final Disconnected DISCONNECTED = new Disconnected();


    @Autowired
    @Qualifier(value="crayfish-amqpActor")
    ActorRef amqpActor;

    private SimpleAmqpPublishActor(String connectionName) {
        assert connectionName != null;

        Data initData = new Data<>(connectionName, null);

        startWith( Init, initData, FiniteDuration.Zero());

        // Если в Init состояния словили TimeOut, то по завершении задержки возвращаемся в Consume
        // P.S.> Промежуточное состояние Consume позволяет в момент перехода в данное состояние отправить запрос
        // на получение соединения с AMQP сервером. В момент инициализации мы сразу инициируем переход в Consume
        when( Init,
              matchEventEquals(StateTimeout(), (event, data) -> goTo(Consume))
            );

        // При вереходе из Init в Consume отправляем запрос на AMQP соединение
        onTransition(
            matchState (
                Init, Consume, () -> connect(nextStateData().getConnectionName())
            )
        );

        // Если получили исключение - перейти в init состояние на 2 секунды
        when( Consume
             ,matchEvent(Throwable.class, (exception, data) -> goTo(Init).forMax(new FiniteDuration(2, TimeUnit.SECONDS)))
        );

        // Если получили готовое соединение - перейти в Connected состояние
        when( Consume
             ,matchEvent( Connection.class,
                          (connection, data) -> {
                              Channel channel;
                              try {
                                  channel = connection.createChannel();
                                  channel.addReturnListener(
                                          new ReturnListener() {
                                              @Override
                                              public void handleReturn(int replyCode, String replyText, String exchange, String routingKey, AMQP.BasicProperties properties, byte[] body) throws IOException {
                                                  logger.warn("Unable to send message to exchange: `{}`, routing key: `{}`", exchange, routingKey);
                                              }
                                          }
                                  );
                              } catch (IOException ioex) {
                                  logger.error("Unable to create AMQP connection channel.");
                                  return goTo(Init).forMax(new FiniteDuration(2, TimeUnit.SECONDS)).using(stateData());
                              }
                              return goTo(Connected).using(new Data(stateData().getConnectionName(), channel));
                          }
                        )
        );

        when( Connected
             ,matchEvent( AMQPPublish.class
                         ,(message, data) -> {
                                try {
                                    data.channel.basicPublish(
                                             message.getTag().getExchangeName()
                                            ,message.getTag().getRoutingKey()
                                            ,true
                                            ,message.getPayload().getProps()
                                            ,message.getPayload().getBody()
                                    );
                                } catch (IOException ioex) {
                                    logger.warn("Unable to send message `{}` to amqp target: `{}:{}:{}`", String.valueOf(message.getPayload().getBody()), stateData().connectionName, message.getTag().getExchangeName(), message.getTag().getRoutingKey());
                                    sender().tell(ioex, self());
                                }
                                if (!ActorRef.noSender().equals(sender())) {
                                    sender().tell(COMPLETE, self());
                                }
                                return stay();
                            }
                        )
        );

        when ( Init
              ,matchEvent ( AMQPPublish.class
                   ,(message, data) -> {
                        if (!ActorRef.noSender().equals(sender())) {
                            sender().tell(DISCONNECTED, self());
                        }
                        return stay();
                    }
               )
        );

        when ( Consume
              ,matchEvent ( AMQPPublish.class
                  ,(message, data) -> {
                        if (!ActorRef.noSender().equals(sender())) {
                            sender().tell(DISCONNECTED, self());
                        }
                        return stay();
                   }
               )
        );

        // Информацию обо всех сообщениях в статусе Connected логируем как ошибку
        when( Connected
             ,matchAnyEvent((event, data) -> {
                         logger.error("Unhandled event {} for state: {}", event, stateName());
                         return stay();
                     }
             )
        );

        initialize();

    }

    private void connect(String connectionName) {
        final ActorRef self = getSelf();
        amqpActor.tell(
            new AmqpActor.ConnectMessage (
                    connectionName
                   ,new AmqpActor.ConnectionReceiver() {
                            @Override
                            public void onConnect(com.rabbitmq.client.Connection connection) {
                                self.tell(connection, ActorRef.noSender());
                            }
                            @Override
                            public void onThrow(Throwable throwable) {
                                self.tell(throwable, ActorRef.noSender());
                            }
                        }
                )
           ,self
        );
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    @Override
    public void postStop() {
        super.postStop();
        if (stateData() != null && stateData().getChannel() != null && stateData().getChannel().isOpen()) {
            int channelNumber = -1;
            try {
                channelNumber = stateData().getChannel().getChannelNumber();
                stateData().getChannel().close();
                logger.info("Channel #{} for connection `{}` has been closed", channelNumber, stateData().getConnectionName());
            } catch (Exception e) {
                logger.warn("Unable to close channel {}for connection `{}`", channelNumber > 0 ? String.format("#%d ", channelNumber) : "", stateData().getConnectionName());
            }
        }
    }

}
