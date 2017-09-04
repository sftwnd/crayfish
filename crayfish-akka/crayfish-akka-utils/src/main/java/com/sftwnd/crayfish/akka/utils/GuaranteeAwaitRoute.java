package com.sftwnd.crayfish.akka.utils;

import akka.actor.AbstractFSM;
import akka.actor.ActorIdentity;
import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Identify;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.pattern.AskableActorSelection;
import akka.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ashindarev on 02.03.17.
 */
public class GuaranteeAwaitRoute<Payload> extends AbstractFSM<GuaranteeAwaitRoute.State, Payload> {

    private static final Logger logger = LoggerFactory.getLogger(GuaranteeAwaitRoute.class);
    private static final String NO_SENDER = "ActorRef.noSender()";
    private static final long MAX_TIMEOUT = 2000L;
    private static final Timeout RESOLVE_TIMEOUT = new Timeout(25, TimeUnit.MILLISECONDS);

    protected static enum State {
        Init, Active, Complete
    }

    private Payload payload;

    public static void guarantee(ActorSystem actorSystem, Object payload, ActorPath target, ActorRef sender) {
        actorSystem.actorOf( Props.create( GuaranteeAwaitRoute.class
                                          ,payload
                                          ,target
                                          ,sender )
                           );
    }

    @SuppressWarnings("unchecked")
    public GuaranteeAwaitRoute(Payload payload, ActorPath target, ActorRef sender) {

        assert payload != null;
        assert target  != null;

        final AtomicLong timeout = new AtomicLong(20);

        startWith(State.Init, null);

        when ( State.Init, matchEvent(payload.getClass(), (message, data) -> goTo(State.Active).using((Payload)message).forMax(Duration.Zero())) );

        when ( State.Active
              ,matchEventEquals(
                      StateTimeout()
                     ,(event, data) -> {
                          ActorSelection selection = context().actorSelection(target);
                          AskableActorSelection asker = new AskableActorSelection(selection);
                          Future<Object> future = asker.ask(new Identify(1), RESOLVE_TIMEOUT);
                          ActorIdentity ident = (ActorIdentity) Await.result(future, RESOLVE_TIMEOUT.duration());
                          Optional<ActorRef> actorRef = ident.getActorRef();
                          if (actorRef.isPresent()) {
                              logger.trace("Target [{}] has been founded for sender: {}", target, sender == null ? NO_SENDER : sender.path());
                              actorRef.get().tell(payload, sender);
                              return goTo(State.Complete);
                          }
                          logger.trace("Target [{}] does not exists, sender: {}", target, sender == null ? NO_SENDER : sender.path());
                          return stay().forMax(FiniteDuration.create(getTimeout(timeout), TimeUnit.MILLISECONDS));
                      }
               )
        );

        onTransition(
             matchState(State.Active, State.Complete, () -> {
                logger.trace("{}[{}] has been completed for targer: {}, sender: {}", GuaranteeAwaitRoute.class.getSimpleName(), self().path(), target, sender == null ? NO_SENDER : sender.path());
                self().tell(PoisonPill.getInstance(), self());
             })
        );

        when ( State.Complete
              ,matchAnyEvent(
                        (event, data) -> stay()
               )
        );

        initialize();

        self().tell(payload, sender);

    }

    @Override
    public void preStart() {
        logger.trace("{}[{}] has been started", GuaranteeAwaitRoute.class.getSimpleName(), self().path());
    }

    @Override
    public void postStop() {
        logger.trace("{}[{}] has been stopped", GuaranteeAwaitRoute.class.getSimpleName(), self().path());
    }

    private long getTimeout(AtomicLong timeout) {
        try {
            return timeout.get();
        } finally {
            timeout.set(Math.min(Math.round(timeout.get() * 1.25), MAX_TIMEOUT));
        }
    }

}
