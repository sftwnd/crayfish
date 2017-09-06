package com.github.sftwnd.crayfish.akka.pattern.aggregation;

import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.sftwnd.crayfish.akka.pattern.aggregation.AggregatorFSM.State.Active;
import static com.github.sftwnd.crayfish.akka.pattern.aggregation.AggregatorFSM.State.Clean;


/**
 * Created by ashindarev on 10.03.17.
 */
public class AggregatorFSM extends AbstractFSM<AggregatorFSM.State, AggregatorFSM.Data<?>> {

    private static final Logger logger = LoggerFactory.getLogger(AggregatorFSM.class);

    protected enum State {
        Clean, Active
    }

    private ActorRef target;

    protected static class Data<X> {

        // Список добавленных элементов
        private List<X> list;
        // Момент добавления первого элемента
        private long    tick;

        // Конструктор подразумевает наличие хотя бы одного элемента в агрегаоре
        protected Data(X obj) {
            assert obj != null;
            this.tick = System.currentTimeMillis();
            this.list = new ArrayList<>();
            add(obj);
        }

        // Конструктор берёт первый элемент для агрегации из сообщения
        protected Data(AggregateEvent<X> event) {
            this(event == null ? null : event.getObj());
        }

        public FiniteDuration getTimeout(long timeout) {
            return FiniteDuration.apply(Math.max(0L, tick + timeout - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
        }

        public FiniteDuration getTimeout(Duration timeoutDuration) {
            return getTimeout(timeoutDuration.toMillis());
        }

        // Добавить элемент в список
        protected void add(X obj) {
            this.list.add(obj);
        }

        // Добавить элемент из сообщения в список
        protected void add(AggregateEvent<X> event) {
            add(event.getObj());
        }

        // Текущее кол-во элементов в агрегаторе
        protected int size() {
            return this.list.size();
        }

    }

    private static final <X> Data<X> constructData(AggregateEvent<X> obj) {
        return new Data<>(obj);
    }

    public static class AggregateEvent<X> {

        private X obj;

        protected AggregateEvent(X obj) {
            this.obj = obj;
        }

        public X getObj() {
            return this.obj;
        }

    }

    public static final <X> AggregateEvent<X> constructEvent(X obj) {
        return new AggregateEvent<>(obj);
    }

    public AggregatorFSM(ActorRef target, int sizeLimit) {
        this(target, sizeLimit, null);
    }

    @SuppressWarnings("unchecked")
    public AggregatorFSM(ActorRef target, int sizeLimit, Duration timeoutDuration) {
        assert target != null;
        long timeout = timeoutDuration == null ? Long.MAX_VALUE : timeoutDuration.toMillis();
        this.target = target;

        context().watch(target);

        startWith(Clean, null);

        when( Clean
             ,matchEvent(
                  AggregateEvent.class
                 ,(event, data) -> goTo(Active).using(constructData(event)).forMax(timeoutDuration == null ? null : FiniteDuration.fromNanos(timeoutDuration.toNanos()))
              )
        );

        when( Active
             ,matchEvent (
                  AggregateEvent.class
                 ,(event, data) -> {
                      stateData().add(event);
                      if (stateData().size() >= sizeLimit) {
                          logger.trace("Aggregation is completed by size. Size: {}, Target: {}", stateData().size(), target.path().toString());
                          target.tell(Collections.unmodifiableList(stateData().list), self());
                          return goTo(Clean).using(null);
                      }
                      return stay().forMax(timeoutDuration == null ? null : stateData().getTimeout(timeoutDuration));
                  }
              )
        );

        when( Active
             ,matchEventEquals(
                  StateTimeout(), (event, data) -> {
                      logger.trace("Aggregation is completed by timeout. Size: {}, Target: {}", stateData().size(), target.path().toString());
                      target.tell(Collections.unmodifiableList(stateData().list), self());
                      return goTo(Clean).using(null);
                  }
              )
        );

        whenUnhandled(
            matchEvent (
                Terminated.class
               ,(event, state) -> {
                    if (target.equals(event.getActor())) {
                        context().stop(self());
                        log().debug("Aggregation actor '{}' has been terminated.", self().path().toString());
                    }
                    return stay();
                }
            )
        );

        logger.debug("Aggregation actor '{}' has been started for the target actor '{}'", self().path().toString(), target.path().toString());

    }
}
