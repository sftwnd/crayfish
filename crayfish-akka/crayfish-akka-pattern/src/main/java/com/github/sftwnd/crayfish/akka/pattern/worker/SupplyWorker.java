package com.github.sftwnd.crayfish.akka.pattern.worker;

import akka.actor.AbstractActor;
import akka.pattern.CircuitBreaker;
import akka.pattern.PatternsCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class SupplyWorker<X> extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(SupplyWorker.class);

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
                                      ,Duration.ofSeconds(DEFAULT_RESET_TIMEOUT_SEC)
                                      ,Duration.ofSeconds(DEFAULT_MAX_RESET_TIMEOUT_SEC)
                                  );
        this.evenNoAsFailure = evenNoAsFailure;
        logger.trace("{} has been created with CircuitBreaker: [{}], evenNoAsFailure: [{}].", this.toString().replaceAll(".*\\.", ""), circuitBreaker, evenNoAsFailure);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Receive createReceive() {
        return receiveBuilder()

              .match(
                      Supplier.class
                     ,msg -> {
                          if (logger.isTraceEnabled()) {
                              logger.trace("Message: [{}] has been received by {}", msg == null ? "null" : msg.toString().replaceAll(".*\\.", ""), this.toString().replaceAll(".*\\.", ""));
                          }
                          PatternsCS.pipe(
                                  circuitBreaker.callWithCircuitBreakerCS(
                                          () -> CompletableFuture.supplyAsync(((Supplier<X>)msg)::get)
                                  )
                                  //ToDo: Определиться с выбором контекста getContext().system().dispatcher() или getContext().dispatcher()
                                  //Выбрал системный, чтобы доставлять незанятым 
                                  ,getContext().system().dispatcher()
                          ).to(sender());
                     }
               )

              .build();
    }

}
