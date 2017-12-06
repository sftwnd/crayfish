package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.AbstractFSM;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.pattern.CircuitBreaker;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.sftwnd.crayfish.akka.amqp.AmqpConnectionFactoryActor.State.Connected;
import static com.github.sftwnd.crayfish.akka.amqp.AmqpConnectionFactoryActor.State.Init;

/**
 * Created by ashindarev on 04.08.16.
 */
@Component("crayfish-AmqpConnectionFactoryActor")
@Scope("prototype")
@DependsOn("crayfish-actorSystem")
public class AmqpConnectionFactoryActor extends AbstractFSM<AmqpConnectionFactoryActor.State, Pair<ConnectionFactory, Connection>> {

    protected enum State { Init, Connected }

    @Autowired
    @Qualifier("crayfish-actorSystem")
    ActorSystem actorSystem;

    @Value("${com.github.sftwnd.crayfish.akka.amqp.max-reconnect-timeout:15000}")
    private long maxReconnectTimeout;

    @Value("${com.github.sftwnd.crayfish.akka.amqp.initial-timeout:1000}")
    private volatile long timeout;

    private final CircuitBreaker breaker;

    public AmqpConnectionFactoryActor(ConnectionFactory connectionFactory)
    {
        breaker = new CircuitBreaker(context().dispatcher(), context().system().scheduler(),
                                     3, FiniteDuration.create(7, TimeUnit.SECONDS), FiniteDuration.create(2, TimeUnit.SECONDS))
                     .withExponentialBackoff(FiniteDuration.create(10, TimeUnit.SECONDS));

        startWith( Init, new Pair<>(connectionFactory, null) );

        when( Connected,
              matchEvent (
                      AmqpActor.ConnectionReceiver.class,
                      (receiver, data) -> {
                          receiver.onConnect(data.second());
                          return stay();
                      }
              )
        );

        when( Init,
              matchEvent (
                      AmqpActor.ConnectionReceiver.class,
                      (receiver, data) -> {
                          try {
                              Connection conn = breaker.callWithSyncCircuitBreaker(data.first()::newConnection);
                              log().debug("Connection {}[{}] has been established for: {}", self().path().name(), conn, data.first());
                              try {
                                  receiver.onConnect(conn);
                              } finally {
                                  return goTo(Connected).using(new Pair<>(data.first(), conn));
                              }
                          } catch (Exception ex) {
                              try {
                                  receiver.onThrow(ex);
                              } finally {
                                  return stay();
                              }
                          }
                      }
              )
        );

        initialize();

    }

    @Override
    public void preStart() throws Exception {
        super.preStart();
        log().info("{}[{}] has been started as: '{}'", getClass().getSimpleName(), self().path().name(), self());
    }

    @Override
    public void postStop() {
        try {
            if (stateName().equals(Connected)) {
                Connection conn = stateData().second();
                if (conn != null && conn.isOpen()) {
                    try {
                        conn.close();
                    } catch (IOException ioex) {
                        log().debug("Unable to close AMQP connection: {}, {}", conn, stateData().first());
                    }
                }
            }
        } finally {
            super.postStop();
            log().info("{}[{}] has been stopped. '{}'", getClass().getSimpleName(), self().path().name(), self());
        }

    }

}
