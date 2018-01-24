package com.github.sftwnd.crayfish.akka.pattern.worker;

import akka.actor.AbstractActor;
import akka.pattern.CircuitBreaker;
import akka.pattern.PatternsCS;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SupplyWorker<X> extends AbstractActor {

    private static final int DEFAULT_MAX_FAILURES          = 10;
    private static final int DEFAULT_RESET_TIMEOUT_SEC     = 10;
    private static final int DEFAULT_MAX_RESET_TIMEOUT_SEC = 60;

    private BiFunction<Optional<X>, Optional<Throwable>, Boolean> evenNoAsFailure;
    private CircuitBreaker circuitBreaker;

    public SupplyWorker() {
        this(null);
    }

    public SupplyWorker(CircuitBreaker circuitBreaker) {
        this(circuitBreaker, null);
    }

    public SupplyWorker(CircuitBreaker circuitBreaker, BiFunction<Optional<X>, Optional<Throwable>, Boolean> evenNoAsFailure) {
        this.circuitBreaker = circuitBreaker != null
                            ? circuitBreaker
                            : new CircuitBreaker(
                                       getContext().dispatcher()
                                      ,getContext().system().scheduler()
                                      ,DEFAULT_MAX_FAILURES
                                      ,Duration.create(DEFAULT_RESET_TIMEOUT_SEC, "s")
                                      ,Duration.create(DEFAULT_MAX_RESET_TIMEOUT_SEC, "s")
                                  );
        this.evenNoAsFailure = evenNoAsFailure;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()

              .match(
                      Supplier.class
                     ,msg -> {
                          PatternsCS.pipe(
                                  circuitBreaker.callWithCircuitBreakerCS( () -> CompletableFuture.supplyAsync(((Supplier<X>)msg)::get) )
                                  //ToDo: Определиться с выбором контекста getContext().system().dispatcher() или getContext().dispatcher()
                                  //Выбрал системный, чтобы доставлять незанятым 
                                  ,getContext().system().dispatcher()
                          ).to(sender());
                      }
               )

              .build();
    }

}
