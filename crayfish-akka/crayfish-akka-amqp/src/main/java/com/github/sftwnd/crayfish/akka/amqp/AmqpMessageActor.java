package com.github.sftwnd.crayfish.akka.amqp;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import com.github.sftwnd.crayfish.amqp.message.AMQPMessage;
import scala.concurrent.duration.FiniteDuration;

public class AmqpMessageActor<Payload> extends AbstractFSM<AmqpMessageActor.State, AmqpMessageActor.Redirect> {

    public enum State {
        Init, Preacknowledge, Processed, Rejected
    }

    public static class Processed {
    }

    public static class Rejected<Payload> {

        private Payload payload;

        public Rejected(Payload payload) {
            this.payload = payload;
        }

        public Payload getPayload() {
            return this.payload;
        }
    }

    public static final Processed PROCESSED = new Processed();
    @SuppressWarnings("unchecked")
    public static final Rejected  REJECTED  = new Rejected(null);

    public static class Redirect {
        private ActorRef target;
        private Object   payload;
        public Redirect(ActorRef target, Object payload) {
            this.target = target;
            this.payload = payload;
        }

        public ActorRef getTarget() {
            return target;
        }

        public Object getPayload() {
            return payload;
        }

    }

    public static final Redirect NULL_REDIRECT   = new Redirect(null, null);
    public static final Redirect REJECT_REDIRECT = new Redirect(null, null);

    public AmqpMessageActor(final AMQPMessage<Payload> message, final ActorRef consumer, final ActorRef preacknowledger, final ActorRef subscriber) {

        startWith(State.Init, null, FiniteDuration.Zero());

        if (preacknowledger == null) {
            // Если не задан preacknowledger, то просто атправляем consumer-у complete сигнал по сообщению
            when(State.Init
                    , matchEventEquals(
                            StateTimeout(),
                            (event, data) -> {
                                consumer.tell(new AmqpConsumeActor.Complete(message.getTag()), self());
                                return goTo(State.Processed);
                            }
                    )
            );
        } else {
            // Иначе - отправляем тело preacknowledger-у. Он ничего не знает о тэгировании сообщения,
            // которое скрываем, если оно не попадает в сообщение.
            when(State.Init
                    , matchEventEquals(
                            StateTimeout(),
                            (event, data) -> {
                                preacknowledger.tell(message.getPayload(), self());
                                return goTo(State.Preacknowledge);
                            }
                    )
            );
        }

        // Если получили Processed сообщение, то отправляем Complete consumer-у
        // P.S.> Пока не проверяем тэг. Возможно в дальнейшем добавим проверку - по необходимости...
        // P.P.S.> Сообщение полюбому дожно быть обработано, вот только при success обработке оно отправляется напрямую consumer-у
        //         и оттуда подтверждается success обратным callback. Reject-ы же отправляются выше для того, чтобы сообщение не попало
        //         в дальнейшен подписчику (subscriber). Т.е. этот шаг более затратный по доставке.
        when( State.Preacknowledge
                ,matchEvent(
                         Processed.class
                        ,(complete, data) -> {
                             consumer.tell(new AmqpConsumeActor.Complete(message.getTag()), self());
                             return goTo(State.Processed);
                         }
                )
        );

        // Если получиили Reject сообщение, то сообщаем, что сообщение обработано и отключаем redirect для сообщения вААще
        when( State.Preacknowledge
                ,matchEvent(
                         Rejected.class
                        ,(reject, data) -> {
                            // Сообщаем preacknowledger-у о Reject операции по сообщению (чисто информация, вдруг чё подчистить надо)
                            // FYI: оно придёт после самого Payload
                            // Информацию об отправителе гасим - некому отправлять в силу PoisonPill ;)
                            preacknowledger.tell(new Rejected<>(message.getPayload()), ActorRef.noSender());
                            // Ну и далее - свистим консьюмеру, что сообщение обработано
                            consumer.tell(new AmqpConsumeActor.Complete(message.getTag()), self());
                             return goTo(State.Processed).using(REJECT_REDIRECT);
                         }
                )
        );

        when( State.Preacknowledge
                ,matchEvent(
                        Redirect.class
                        ,(redirect, data) -> {
                            consumer.tell(new AmqpConsumeActor.Complete(message.getTag()), self());
                            return goTo(State.Processed).using(redirect);
                        }
                )
        );

        when( State.Processed
                ,matchEvent(
                        AmqpConsumeActor.Acknowledged.class
                        ,(event, data) -> {
                            // Если не остановили посылку ответа
                            if (stateData() != NULL_REDIRECT) {
                                // Если перенаправили с подписчика на новую точку назначения и она не пуста
                                if (stateData() != null && stateData().getTarget() != null) {
                                    // отправляем туда новый payload, указанный в redirect
                                    stateData().getTarget().tell(stateData().getPayload(), self());
                                } else if (subscriber != null) {
                                    subscriber.tell(
                                            stateData() == null
                                            // Если не задан redirect, то отправляем на подписчика payload
                                            ? message.getPayload()
                                            // Если задан redirect без определения нового subscriber-а, то отправляем
                                            // на подписчика payload редиректа
                                            : stateData().getPayload()
                                           ,self()
                                    );
                                }
                            }
                            // Завершаем работу актора
                            self().tell(PoisonPill.getInstance(), self());
                            return stay();
                        }
                )
        );

        when( State.Processed
                ,matchEvent(
                         Rejected.class
                        ,(event, data) -> {
                            // Если не остановили посылку ответа
                            if (stateData() != NULL_REDIRECT) {
                                // Если перенаправили с подписчика на новую точку назначения и она не пуста
                                if (stateData() != null && stateData().getTarget() != null) {
                                    // отправляем туда Reject сообщение с Payload первичного сообщение
                                    stateData().getTarget().tell(new Rejected<>(message.getPayload()), self());
                                } else if (subscriber != null) {
                                    subscriber.tell(new Rejected<>(message.getPayload()), self());
                                }
                            }
                            // Завершаем работу актора
                            self().tell(PoisonPill.getInstance(), self());
                            return stay();
                        }
                )
        );

        initialize();

    }

}