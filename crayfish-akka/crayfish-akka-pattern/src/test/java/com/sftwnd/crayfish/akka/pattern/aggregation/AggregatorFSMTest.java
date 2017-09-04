package com.sftwnd.crayfish.akka.pattern.aggregation;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by ashindarev on 10.03.17.
 */
public class AggregatorFSMTest {

    private static ActorSystem actorSystem;
    private static ActorRef checkActor;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @Test
    public void terminationTest() throws InterruptedException {
        new TestKit(actorSystem) {{
            TestKit fooActor = new TestKit(actorSystem);
            ActorRef aggregateActor = actorSystem.actorOf(Props.create(AggregatorFSM.class, fooActor.getRef(), 5, null), "aggregator");
            final TestKit probe = new TestKit(actorSystem);
            probe.watch(aggregateActor);
            actorSystem.stop(fooActor.getRef());
            final Terminated msg = probe.expectMsgClass(Terminated.class);
            assertEquals("aggregateActor have to be terminated.", msg.getActor(), aggregateActor);
        }};
    }

    @Test
    public void aggregateSizeTest() throws InterruptedException {
        new TestKit(actorSystem) {{
            TestKit fooActor = new TestKit(actorSystem);
            try {
                List<Integer> elements = Arrays.asList(new Integer[]{10, 9, 8, 7, 6});
                ActorRef aggregateActor = actorSystem.actorOf(Props.create(AggregatorFSM.class, fooActor.getRef(), elements.size()), "aggregateSizeTest");
                elements.forEach(
                        (element) -> aggregateActor.tell(AggregatorFSM.constructEvent(element), ActorRef.noSender())
                );
                assertEquals(elements, fooActor.expectMsgClass(List.class));
            } finally {
                actorSystem.stop(fooActor.getRef());
            }
        }};
    }

    @Test
    public void aggregateActorTimeoutTest() throws InterruptedException {
        new TestKit(actorSystem) {{
            final int cnt = 5;
            TestKit fooActor = new TestKit(actorSystem);
            try {
                ActorRef aggregateActor = actorSystem.actorOf(Props.create(AggregatorFSM.class, fooActor.getRef(), cnt, Duration.ofMillis(50)), "aggregateActorTimeoutTest");
                for (int i = 0; i < cnt; i++) {
                    aggregateActor.tell(AggregatorFSM.constructEvent(i), ActorRef.noSender());
                }
                List<Integer> elements = Arrays.asList(new Integer[]{10, 9, 8});
                for (Integer element : elements) {
                    aggregateActor.tell(AggregatorFSM.constructEvent(element), ActorRef.noSender());
                }
                Object[] results = fooActor.receiveN(2, FiniteDuration.apply(1, TimeUnit.SECONDS)).toArray();
                assertEquals(elements, results[1]);
            } finally {
                actorSystem.stop(fooActor.getRef());
            }
        }};
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(actorSystem);
        actorSystem = null;
    }

}