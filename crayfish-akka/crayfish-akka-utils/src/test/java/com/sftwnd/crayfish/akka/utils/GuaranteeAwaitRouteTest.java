package com.sftwnd.crayfish.akka.utils;

import akka.actor.ActorPaths;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ashindarev on 02.03.17.
 */
public class GuaranteeAwaitRouteTest {

    private static final String ACTOR_SYSTEM_NAME = "actor-system";
    private static final String ACTOR_NAME = "testActor";
    private static ActorSystem actorSystem;

    @BeforeClass
    public static void startUp() {
        actorSystem = ActorSystem.create(ACTOR_SYSTEM_NAME, ConfigFactory.load("akka.conf"));
    }

    @AfterClass
    public static void tearDown() throws InterruptedException {
        ActorSystemTermination.terminateAndAwait(actorSystem);
    }

    @Test
    public void guaranteeAwaitRouteTest() throws InterruptedException {
        GuaranteeAwaitRoute.guarantee(
                 actorSystem
                ,MESSAGE
                ,ActorPaths.fromString("akka://"+ACTOR_SYSTEM_NAME+"/user/"+ACTOR_NAME)
                ,ActorRef.noSender()
        );
        countDownLatch.await(100, TimeUnit.MILLISECONDS);
        ActorRef myActor = actorSystem.actorOf(Props.create(TestActor.class), ACTOR_NAME);
        countDownLatch.await(3, TimeUnit.SECONDS);
        Assert.assertEquals("Lath has to be countdowned", 0, countDownLatch.getCount());
    }

    static final String MESSAGE = "OOPS!!!";

    static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static class TestActor extends UntypedAbstractActor {

        @Override
        public void onReceive(Object message) throws Throwable {
            if (MESSAGE.equals(message)) {
                countDownLatch.countDown();
            } else {
                unhandled(message);
            }
        }
    }

}