package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import javafx.util.Pair;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by ashindarev on 26.08.2017
 */
@Component("crayfish-AmqpSubscriptionProcessorActor")
@Scope("prototype")
@Profile("crayfish-akka-amqp")
public class AmqpSubscriptionProcessorActor<Payload> extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    // Данный класс содержит связь актора, содержащего сообщение, сообщения и его тега
    private class Record extends Pair<ActorRef, AMQPMessage<Payload>> {

        public Record(ActorRef actorRef, AMQPMessage<Payload> message) {
            super(actorRef, message);
        }

    }

    private ActorRef preacknowledger;
    private ActorRef subscriber;

    public AmqpSubscriptionProcessorActor(ActorRef preacknowledger, ActorRef subscriber) {
        this.preacknowledger = preacknowledger;
        this.subscriber = subscriber;
    }

    private List<Record> messages = new LinkedList<>();

    @Override
    @SuppressWarnings("unchecked")
    public Receive createReceive() {

        return receiveBuilder()

                .match(
                        // Если пришло AMQP сообщение с Payload, то создаём дочерний актор для него и помещаем в вектор сообщений,
                        // но при условии, что содержимое не пусто.
                        // Иначе просто отмечаем его как принятое с выводом соответствующей информации в журнал.
                        AMQPMessage.class
                        ,(msg) -> {
                            if (msg.getPayload() != null) {
                                log.debug("AMQP message has been received by subscription processor: {}", msg);
                                // Если получили не пустой Payload, то создаем его как "безымянный" child
                                messages.add(
                                    new Record(
                                            context().actorOf(Props.create(AmqpMessageActor.class, msg, sender(), preacknowledger, subscriber))
                                           ,msg
                                        )
                                );
                            } else {
                                log.warning("Received AMQP message is null");
                                // Пришлось городить огород с приведением в силу того, что компилятор считает, что getTag
                                // возвращает просто Comparable, а не AMQPMessageTag, как на самом деле
                                sender().tell(new AmqpConsumeActor.Complete(((AMQPMessage<Payload>)msg).getTag()), self());
                            }
                        }
                )

                .match(
                        AmqpConsumeActor.Acknowledged.class
                        ,(tag) -> {
                            log.debug("Messages has been acknowledged by tag: {}", tag);
                            // Бежим по всем сообщениям
                            for (Iterator<Record> iter = messages.iterator(); iter.hasNext();) {
                                Record record = iter.next();
                                // Если вышли за пределы успешно подтверждённых - завершаем проход
                                if (record.getValue().getTag().getValue() > tag.getValue()) {
                                    break;
                                }
                                // Удаляем сообщение из списка подтверждённых
                                iter.remove();
                                // Нотифицируем сообщение о подтверждении на AMQP брокере
                                record.getKey().forward(tag, context());
                            }
                        }
                )

                .match(
                         AmqpConsumeActor.Rejected.class
                        ,(rejected) -> {
                            log.warning("AMQP Consumer {} connection has been lost after the tag: {} with signal: {}", rejected.getKey(), rejected.getValue(), rejected.getSig());
                            // Бежим по всем сообщениям
                            for (Iterator<Record> iter = messages.iterator(); iter.hasNext();) {
                                Record record = iter.next();
                                // Если зашли за пределы успешно подтверждённых - посылаем им Reject и удаляем из списка
                                if (record.getValue().getTag().getValue() > rejected.getValue()) {
                                    // Удаляем сообщение из списка подтверждённых
                                    iter.remove();
                                    // Нотифицируем сообщение об отказе в ACK на AMQP брокере
                                    record.getKey().forward(AmqpMessageActor.REJECTED, context());
                                }
                            }
                        }
                )

                .matchAny( msg -> log.warning("Unknown message: {}, Type: {}, Sender: {}", msg, msg.getClass(), sender().toString()))

                .build();

    }

    final AtomicLong lastForcedAckTag = new AtomicLong(-1);

}
