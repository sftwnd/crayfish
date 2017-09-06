package com.github.sftwnd.crayfish.akka.utils;

import akka.actor.ActorSystem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashindarev on 02.03.17.
 */
public class ActorSystemTermination {

    public static void terminateAndAwait(ActorSystem actorSystem) throws InterruptedException {
        terminateAndAwait(actorSystem, -1L);
    }

    public static void terminateAndAwait(ActorSystem actorSystem, long timeout) throws InterruptedException {
        terminateAndAwait(actorSystem, timeout, TimeUnit.MILLISECONDS);
    }

    public static void terminateAndAwait(ActorSystem actorSystem, long timeout, TimeUnit timeUnit) throws InterruptedException {
        assert actorSystem != null;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        actorSystem.registerOnTermination(
                new Runnable() {
                    @Override
                    public void run() {
                        countDownLatch.countDown();
                    }
                }
        );
        actorSystem.terminate();
        if (timeout > 0) {
            countDownLatch.await(timeout, timeUnit);
        } else {
            countDownLatch.await();
        }
    }


}
