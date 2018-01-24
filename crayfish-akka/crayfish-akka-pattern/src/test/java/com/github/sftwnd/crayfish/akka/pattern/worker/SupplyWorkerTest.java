package com.github.sftwnd.crayfish.akka.pattern.worker;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status.Failure;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class SupplyWorkerTest {

    private static ActorSystem actorSystem;
    private static ActorRef    supplyWorker;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
        supplyWorker = actorSystem.actorOf(Props.create(SupplyWorker.class), "worker");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

    @Test
    public void successSupplyTest() throws InterruptedException {
        new TestKit(actorSystem) {{
            final TestKit probe = new TestKit(actorSystem);
            try {
                final String str = "OOPS!!!";
                Supplier<String> supplier = new Supplier<String>() {
                    @Override
                    public String get() {
                        return str;
                    }
                };
                supplyWorker.tell(supplier, probe.getRef());
                final String msg = probe.expectMsgClass(String.class);
                assertEquals("Reply result is wrong", str, msg);
            } finally{
                actorSystem.stop(probe.getRef());
            }
        }};
    }

    @Test
    public void throwedSupplyTest() throws InterruptedException {
        new TestKit(actorSystem) {{
            final TestKit probe = new TestKit(actorSystem);
            try {
                final RuntimeException rex = new RuntimeException("OOPS!!!");
                Supplier<String> threader = new Supplier<String>() {
                    @Override
                    public String get() {
                        throw rex;
                    }
                };
                supplyWorker.tell(threader, probe.getRef());
                final Failure msg = probe.expectMsgClass(Failure.class);
                assertEquals("Reply result is wrong", rex, msg.cause().getCause());
            } finally{
                actorSystem.stop(probe.getRef());
            }
        }};
    }

}