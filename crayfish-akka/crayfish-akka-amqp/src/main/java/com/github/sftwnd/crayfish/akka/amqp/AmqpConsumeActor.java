package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import com.github.sftwnd.crayfish.amqp.consume.AmqpOnEventListener;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import com.github.sftwnd.crayfish.akka.spring.di.SpringExtension;
import com.github.sftwnd.crayfish.amqp.consume.AMQPMessageConverter;
import com.github.sftwnd.crayfish.amqp.consume.AmqpOnAckListener;
import com.github.sftwnd.crayfish.amqp.consume.AmqpOnArriveListener;
import com.github.sftwnd.crayfish.amqp.consume.AmqpOnCompleteListener;
import com.github.sftwnd.crayfish.amqp.consume.AmqpOnShutdownListener;
import com.github.sftwnd.crayfish.amqp.consume.ConvertableAmqpConsumer;
import com.github.sftwnd.crayfish.amqp.consume.DefaultAmqpConsumer;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessageTag;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.sftwnd.crayfish.akka.amqp.AmqpConsumeActor.State.*;

/**
 * Created by ashindarev on 04.08.16.
 */
@Component("crayfish-AmqpConsumeActor")
@Scope("prototype")
@DependsOn("crayfish-actorSystem")
public class AmqpConsumeActor<Payload> extends AbstractFSM<AmqpConsumeActor.State, AmqpConsumeActor.Data<Payload>> {

    private static final Logger logger = LoggerFactory.getLogger(AmqpConsumeActor.class);

    protected enum State { Init, Consume, Connected }

    public static final class Complete extends AMQPMessageTag {

        public Complete(AMQPMessageTag tag) {
            super(tag);
        }

    }

    public static final class Acknowledged extends AMQPMessageTag {

        public Acknowledged(AMQPMessageTag tag) {
            super(tag);
        }

    }

    public static final class Rejected extends AMQPMessageTag {

        private ShutdownSignalException signal;

        public Rejected(AMQPMessageTag tag, ShutdownSignalException signal) {
            super(tag);
            this.signal = signal;
        }

        public ShutdownSignalException getSig() {
            return this.signal;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(Rejected.class, ToStringStyle.JSON_STYLE)
                               .append("consumerTag", getKey())
                               .append("tag", getValue())
                             .toString();
        }
    }

    public static final class ForceACK {

        private long tag;

        public ForceACK(long tag) {
            this.tag = tag;
        }

        public long getTag() {
            return this.tag;
        }

    }

    public static class Data<Payload> {

        private String connectionName;
        private String queueName;
        private AMQPMessageConverter<Payload> messageConverter;
        private AmqpOnEventListener amqpOnEventListener;
        private DefaultAmqpConsumer<Payload> amqpConsumer;
        private ActorRef subscription;

        public Data(String connectionName, String queueName, AMQPMessageConverter<Payload> messageConverter, AmqpOnEventListener amqpOnEventListener, ActorRef subscription) {
            assert connectionName != null;
            assert queueName != null;
            assert messageConverter != null;
            this.connectionName = connectionName;
            this.queueName = queueName;
            this.messageConverter = messageConverter;
            this.amqpOnEventListener = amqpOnEventListener;
            this.subscription = subscription;
        }

        public String getConnectionName() {
            return connectionName;
        }

        public String getQueueName() {
            return queueName;
        }

        public AMQPMessageConverter<Payload> getMessageConverter() {
            return messageConverter;
        }

        public AmqpOnEventListener getAmqpOnEventListener() {
            return this.amqpOnEventListener;
        }

        public DefaultAmqpConsumer<Payload> getAmqpConsumer() {
            return amqpConsumer;
        }

        public void setAmqpConsumer(DefaultAmqpConsumer<Payload> amqpConsumer) {
            this.amqpConsumer = amqpConsumer;
        }

        public ActorRef getSubscription() {
            return this.subscription;
        }

    }

    @Autowired
    @Qualifier(value="crayfish-amqpActor")
    ActorRef amqpActor;

    final AtomicLong lastForceAckTag = new AtomicLong(-1);

    private AmqpConsumeActor(String connectionName, String queueName, AMQPMessageConverter<Payload> messageConverter, AmqpOnEventListener amqpOnEventListener, ActorRef subscription, final int prefetchSize, final int ackSize) {
        assert subscription != null;
        constructActor(connectionName, queueName,  messageConverter, amqpOnEventListener, subscription, prefetchSize, ackSize);
    }

    private AmqpConsumeActor(String connectionName, String queueName, AMQPMessageConverter<Payload> messageConverter, AmqpOnEventListener amqpOnEventListener, ActorRef preacknowledger, ActorRef subscriber, final int prefetchSize, final int ackSize) {
        ActorRef subscription = context().actorOf(
                                    SpringExtension.SpringExtProvider.get(context().system()).props("crayfish-AmqpSubscriptionProcessorActor", preacknowledger, subscriber)
                                );
        constructActor(connectionName, queueName,  messageConverter, amqpOnEventListener, subscription, prefetchSize, ackSize);
    }

    private void constructActor(String connectionName, String queueName, AMQPMessageConverter<Payload> messageConverter, AmqpOnEventListener amqpOnEventListener, ActorRef subscription, final int prefetchSize, final int ackSize) {

        assert subscription != null;

        Data<Payload> initData = new Data<>(connectionName, queueName, messageConverter, amqpOnEventListener, subscription);

        startWith( Init, initData, FiniteDuration.Zero());

        // Если в Init состояния словили TimeOut, то по завершении задержки возвращаемся в Consume
        // P.S.> Промежуточное состояние Consume позволяет в момент перехода в данное состояние отправить запрос
        // на получение соединения с AMQP сервером. В момент инициализации мы сразу инициируем переход в Consume
        when( Init,
              matchEventEquals(StateTimeout(), (event, data) -> goTo(State.Consume))
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
             ,matchEvent(Connection.class, (connection, data) -> goTo(Connected).using(constructSubscription(connection, prefetchSize, ackSize)))
        );

        // При вереходе из Consume в Connected состояние стартуем подписчика.
        // P.S.> Т.е. все сообщения, которые попадут с начала работы подписчика бедет делать это в Connected состоянии актора
        onTransition(
                matchState (
                        Consume, Connected, () -> stateData().getAmqpConsumer().start()
                )
        );

        // В случае прихода Complete сообщения от подписчика помечаем сообщение как выполненное для дальнейшей
        // отправки подтверждения по нему
        when( Connected
             ,matchEvent(Complete.class, (complete, data) -> {
                                data.getAmqpConsumer().messageComplete(complete);
                                return stay();
                            }
                        )
        );

        // В случае прихода ForceACK сообщения от подписчика инициируем принудительное информирование AMQP сервера обо
        // всех подтверждённых сообщениях
        when( Connected
                ,matchEvent(ForceACK.class, (forceACK, data) -> {
                    if (forceACK.getTag() == lastForceAckTag.get()) {
                        logger.debug("Consumer {} has been informed about acknowledge by timeout on tag: {}", stateData().getAmqpConsumer().getConsumerTag(), forceACK.getTag());
                        data.getAmqpConsumer().messageComplete(null);
                    }
                    return stay();
                })
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

    private Data<Payload> constructSubscription(Connection connection, int prefetchSize, int ackSize) throws IOException {
        final List<AmqpOnEventListener> amqpOnEventListeners = new ArrayList<>();
        if (stateData().getAmqpOnEventListener() != null) {
            amqpOnEventListeners.add(stateData().getAmqpOnEventListener());
        }
        amqpOnEventListeners.add(new AmqpConsumeListener<Payload>(self(), stateData().getSubscription()));
        ConvertableAmqpConsumer<Payload> consumer = new ConvertableAmqpConsumer<Payload>(
                connection, stateData().getQueueName(), prefetchSize, ackSize,
                stateData().getMessageConverter(),
                amqpOnEventListeners
        );
        consumer.addOnEventListener (
                new AmqpOnCompleteListener<Payload>() {

                    // В случае получения onComplete если это сообщение <= верхней границы списка всех подтверждённых,
                    // то отправляем отложенный event через секунду на подтверждение пачки меньшего размера, чем ackSize
                    // по TimeOut
                    @Override
                    public void onComplete(AMQPMessageTag tag) {
                        if (tag.getValue().longValue() <= consumer.getLastTag() && consumer.getLastTag() > lastForceAckTag.get()) {
                            lastForceAckTag.set(consumer.getLastTag());
                            context().system().scheduler().scheduleOnce(
                                    FiniteDuration.create(1, TimeUnit.SECONDS), self(), new ForceACK(lastForceAckTag.get()), context().system().dispatcher(), self()
                            );
                        }
                    }

                }
        );
        stateData().setAmqpConsumer(consumer);
        return stateData();
    }

    private void connect(String connectionName) {
        final ActorRef self = getSelf();
        amqpActor.tell(
                new AmqpActor.ConnectMessage(
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
        if (stateData() != null && stateData().getAmqpConsumer() != null) {
            Channel channel = stateData().getAmqpConsumer().getChannel();
            try {
                channel.close();
                logger.info("Channel for `{}:{}` has been closed", stateData().getConnectionName(), stateData().getQueueName());
            } catch (Exception e) {
                logger.warn("Unable to close channel `{}:{}`", stateData().getConnectionName(), stateData().getQueueName());
            }
        }
    }

    private static class AmqpConsumeListener<Payload> implements AmqpOnArriveListener<Payload>, AmqpOnAckListener, AmqpOnShutdownListener {

        private static final Logger logger = LoggerFactory.getLogger(AmqpConsumeListener.class);

        private final ActorRef subscription;
        private final ActorRef consumer;

        public AmqpConsumeListener(ActorRef consumer, ActorRef subscription) {
            this.subscription = subscription;
            this.consumer = consumer;
        }

        @Override
        public void onArrive(AMQPMessage<Payload> message) {
            subscription.tell(message, consumer);
            logger.trace("Message has been arrived to listener: {}", message);
        }

        @Override
        public void onAck(AMQPMessageTag tag) {
            subscription.tell(new AmqpConsumeActor.Acknowledged(tag), consumer);
            logger.debug("Messages has been acknowledged in listener by tag: {}", tag);
        }

        @Override
        public void shutdownSignal(AMQPMessageTag tag, ShutdownSignalException sig) {
            subscription.tell(new AmqpConsumeActor.Rejected(tag, sig), consumer);
            logger.debug("Messages will been rejected after the tag: {} by the shutdownSignal: {}", tag, sig.getLocalizedMessage());
        }
    }

}
