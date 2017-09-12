package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.github.sftwnd.crayfish.spring.amqp.AmqpConnectionFactories;
import com.github.sftwnd.crayfish.akka.spring.di.SpringExtension;
import com.github.sftwnd.crayfish.messaging.impl.DefaultMessage;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.Option;
import scala.concurrent.duration.FiniteDuration;

import static com.github.sftwnd.crayfish.akka.amqp.AmqpActor.State.Active;
import static com.github.sftwnd.crayfish.akka.amqp.AmqpActor.State.Init;

/**
 * Created by ashindarev on 01.03.17.
 */
@Component("crayfish-AmqpActor")
@Scope("prototype")
@DependsOn("crayfish-actorSystem")
public class AmqpActor extends AbstractFSM<AmqpActor.State, Object> {

    protected enum State { Init, Active }

    // Сообщение с требованием создать "подактор", занимающийся оформлением соединения с RabbitMQ
    public static class ConnectionFactoryMessage extends DefaultMessage<String, ConnectionFactory> {
        public ConnectionFactoryMessage(String name, ConnectionFactory connectionFactory) {
            super(name, connectionFactory);
            assert name != null;
            assert connectionFactory != null;
        }
    }

    public interface ConnectionReceiver {
        void onConnect(Connection connection);
        void onThrow(Throwable throwable);
    }

    // Сообщение с требованием создать "подактор", занимающийся оформлением соединения с RabbitMQ
    public static class ConnectMessage  {

        private String name;
        private ConnectionReceiver connectionReceiver;

        public ConnectMessage(String name, ConnectionReceiver connectionReceiver) {
            this.name = name;
            this.connectionReceiver = connectionReceiver;
        }

        public String getName() {
            return this.name;
        }

        public ConnectionReceiver getConnectionReceiver() {
            return this.connectionReceiver;
        }

        @Override
        public String toString() {
            return new ToStringBuilder("ConnectMessage", ToStringStyle.JSON_STYLE)
                               .append("name", name)
                               .append("connectionReceiver", connectionReceiver)
                             .toString();
        }
    }

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    @Qualifier("crayfish-actorSystem")
    ActorSystem actorSystem;

    {
        startWith(Init, null, FiniteDuration.Zero());

        when( Init, matchEventEquals (StateTimeout(), (event, data) -> goTo(Active) ));

        onTransition(
            matchState (Init, Active, () -> {
                applicationContext.getBeansOfType(ConnectionFactory.class).entrySet().forEach(
                        bean -> {
                            context().actorOf (
                                SpringExtension.SpringExtProvider.get(actorSystem).props("crayfish-AmqpConnectionFactoryActor",bean.getValue())
                                    , AmqpConnectionFactories.getName(bean.getKey())
                            );
                        }
                );
            })
        );

        when( Active,
              matchEvent (
                   ConnectMessage.class, (event, data) -> {
                       Option<ActorRef> option = getContext().child(event.getName());
                       if (option.isDefined()) {
                           option.get().tell(event.getConnectionReceiver(), sender());
                       } else {
                           event.getConnectionReceiver().onThrow(new Exception("Unable to find connection factory: "+event));
                       }
                       return stay();
                   }
              )
        );

        initialize();

    }

}
