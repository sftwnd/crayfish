package com.github.sftwnd.crayfish.zookeeper.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static org.apache.curator.framework.imps.CuratorFrameworkState.LATENT;
import static org.apache.curator.framework.imps.CuratorFrameworkState.STARTED;
import static org.apache.curator.framework.imps.CuratorFrameworkState.STOPPED;
import static org.apache.curator.framework.state.ConnectionState.CONNECTED;
import static org.apache.curator.framework.state.ConnectionState.LOST;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class ZookeeperHelperTest {

    @SuppressWarnings("try")
    static final InstanceSpec instanceSpec = InstanceSpec.newInstanceSpec();
    static TestingServer zkServer;

    void testZookeeperHelper(Duration duration, CuratorFrameworkState state) {
        String text = Optional.ofNullable(duration)
                .map(d -> d.toMillis() == 0 ? "zero" : "defined")
                .orElse("null");
        ZookeeperService zooService = new ZookeeperHelper(
                ZookeeperService.builder(connectString())
                , duration
                , ZookeeperHelper::checkCuratorFramework
        );
        AtomicReference<CuratorFramework> ref = new AtomicReference<>();
        assertDoesNotThrow(
                () -> ref.set(zooService.provide())
                , "ZookeeperHelper::provide(" + text + " duration) has to connect to Zookeeper without throws"
        );
        assertNotNull(ref.get(), "ZookeeperHelper::provide(" + text + " duration) has to return nonnull connection");
        assertEquals(state, ref.get().getState(), "curatorFramework state has to be " + state);
        assertSame(ref.get(), zooService.provide(), "ZookeeperHelper::provide(" + text + " duration) has to return the same result on the next call");
    }

    @Test
    void testZookeeperHelperNullDuration() {
        testZookeeperHelper(null, LATENT);
    }

    @Test
    void testZookeeperHelperZeroDuration() {
        testZookeeperHelper(Duration.ZERO, STARTED);
    }

    @Test
    void testZookeeperHelperDuration() {
        testZookeeperHelper(Duration.of(1, ChronoUnit.SECONDS), STARTED);
    }

    @Test
    void testReconnect() {
        ZookeeperService zooService = new ZookeeperHelper(
                  ZookeeperService.builder(connectString())
                , Duration.of(1, ChronoUnit.SECONDS)
                , ZookeeperHelper::checkCuratorFramework
        );
        CuratorFramework curatorFramework = zooService.provide();
        curatorFramework.close();
        restartTestServer(curatorFramework, Duration.of(3, ChronoUnit.SECONDS));
        assertNotSame(curatorFramework, zooService.provide(), "After restart zookeeper ZookeeperService has to return other connection");
        assertNotNull(zooService.provide(), "After restart zookeeper ZookeeperService has to return non null connection");
    }

    @Test
    void testClosedConnection() throws InterruptedException {
        ZookeeperHelper zooService = new ZookeeperHelper(
                ZookeeperService.builder(connectString())
                , Duration.of(1, ChronoUnit.SECONDS)
                , ZookeeperHelper::checkCuratorFramework
        );
        CuratorFramework curatorFramework = zooService.provide();
        assertNotNull(curatorFramework);
        if (curatorFramework != null) {
            assertTrue(zooService.isProvided(), "After ZookeeperHelper::provide helper state has to be provided");
            curatorFramework.close();
            Instant limit = Instant.now().plus(1, ChronoUnit.SECONDS);
            while(curatorFramework.getZookeeperClient().isConnected() && limit.isBefore(Instant.now())) {
                TimeUnit.MICROSECONDS.sleep(20);
            }
            assertTrue(!zooService.isProvided(), "After curatorFramework::close helper state has to be closed");
            assertNotSame(curatorFramework, zooService.provide(), "After close CurratorFramework connection ZookeeperService has to provide other connection");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCheckCuratorFramework() throws InterruptedException {
        CuratorFramework curatorFramework = mock(CuratorFramework.class);
        // STOPPED
        when(curatorFramework.getState()).thenReturn(STOPPED);
        assertFalse(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for stopped framework in has to be false");
        // LATENT
        when(curatorFramework.getState()).thenReturn(LATENT);
        assertTrue(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for LATENT state has to be true");
        // STARTED and Connected
        when(curatorFramework.getState()).thenReturn(STARTED);
        when(curatorFramework.getZookeeperClient()).thenAnswer((Answer<CuratorZookeeperClient>) invocation -> {
            CuratorZookeeperClient zookeeperClient = mock(CuratorZookeeperClient.class);
            when(zookeeperClient.isConnected()).thenReturn(true);
            return zookeeperClient;
        });
        assertTrue(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for started framework in connect state has to return true");
        // STARTED and Disconnected
        when(curatorFramework.getZookeeperClient()).thenAnswer((Answer<CuratorZookeeperClient>) invocation -> {
            CuratorZookeeperClient zookeeperClient = mock(CuratorZookeeperClient.class);
            when(zookeeperClient.isConnected()).thenReturn(false);
            return zookeeperClient;
        });
        assertFalse(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for started framework in disconnect state has to return false");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCheckCuratorFramework1() throws InterruptedException {
        ZookeeperService zooService = new ZookeeperHelper(
                ZookeeperService.builder(connectString())
                ,(Duration) null
                ,ZookeeperHelper::checkCuratorFramework
        );
        CuratorFramework curatorFramework = zooService.provide();
        assertEquals(LATENT, curatorFramework.getState(), "curatorFramework for Provider(null duration) has to be LATENT");
        assertTrue(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for LATENT state has to be true");
        curatorFramework.start();
        curatorFramework.blockUntilConnected();
        assertTrue(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for started framework in connect state has to be true");
        curatorFramework.close();
        assertFalse(ZookeeperHelper.checkCuratorFramework(curatorFramework), "checkCurator for closed framework in has to be false");
    }

    @Test
    void testStateChangedLost() {
        try {
            ZookeeperHelper zooService = new ZookeeperHelper(
                    ZookeeperService.builder(connectString()).sessionTimeoutMs(50)
                            .retryPolicy(new ExponentialBackoffRetry(5, 1, 50))
                    ,Duration.of(1, ChronoUnit.SECONDS)
                    ,ZookeeperHelper::checkCuratorFramework
            );
            CuratorFramework curatorFramework = zooService.provide();
            assertTrue(zooService.isProvided(), "ZookeeperHelper has to be provided to use this test");
            shutdownTestServer(curatorFramework, Duration.of(15, ChronoUnit.SECONDS));
            assertFalse(zooService.isProvided(), "ZookeeperHelper hasn't got to be provided after zookeeper is down and curator connection is lost");
        } finally {
            startZkServer();
        }
    }

    @Test
    void testResourceChanged() throws NoSuchMethodException, InterruptedException {
        Method method = ZookeeperHelper.class.getDeclaredMethod("resourceChanged", CuratorFramework.class, CuratorFramework.class);
        method.setAccessible(true);
        ZookeeperService zooService = new ZookeeperHelper(
                ZookeeperService.builder(connectString()).sessionTimeoutMs(50)
                        .retryPolicy(new ExponentialBackoffRetry(5, 1, 50))
                ,Duration.of(1, ChronoUnit.SECONDS)
                ,ZookeeperHelper::checkCuratorFramework
        );
        BiConsumer<CuratorFramework, CuratorFramework> resourceChanged =
                (oldValue, newValue) -> wrapUncheckedExceptions(() -> method.invoke(zooService, oldValue, newValue));
        CuratorFramework curatorFramework = spy(ZookeeperService.builder(connectString()).build());
        reset(curatorFramework);
        resourceChanged.accept(null, curatorFramework);
        assertDoesNotThrow(
                () -> verify(curatorFramework, times(1)).getCuratorListenable(),
                "ZookeeperHelper::resourceChanged has to register listener for not stopped curatorFramework"
        );
        curatorFramework.start();
        curatorFramework.close();
        Instant tick = Instant.now().plus(3, ChronoUnit.SECONDS);
        while(!STOPPED.equals(curatorFramework.getState()) && Instant.now().isBefore(tick)) {
            TimeUnit.MICROSECONDS.sleep(20);
        }
        reset(curatorFramework);
        resourceChanged.accept(null, curatorFramework);
        assertDoesNotThrow(
                () -> verify(curatorFramework, never()).getConnectionStateListenable(),
                "ZookeeperHelper::resourceChanged hasn't got to register listener for stopped curatorFramework"
        );
    }

    @Test
    void testStartCuratorFramework() throws NoSuchMethodException {
        Method method = ZookeeperHelper.class.getDeclaredMethod("startCuratorFramework", CuratorFramework.class, Duration.class);
        method.setAccessible(true);
        BiFunction<CuratorFramework, Duration, CuratorFramework> startCuratorFramework =
                (framework, duration) -> (CuratorFramework) wrapUncheckedExceptions(() -> method.invoke(null, framework, duration));
        Stream.of(Duration.ZERO, Duration.of(2, ChronoUnit.SECONDS)).forEach(
                duration -> {
                    CuratorFramework curatorFramework = ZookeeperService.builder(connectString()).build();
                    CuratorFramework result = startCuratorFramework.apply(curatorFramework, duration);
                    assertNotNull(result, "ZookeeperHelper::startCuratorFramework(Duration="+duration.toMillis()+"ms) has to ");
                    assertEquals(STARTED, result.getState(), "ZookeeperHelper::startCuratorFramework(Duration="+duration.toMillis()+"ms) has to return started curatorFramework");
                    curatorFramework = spy(ZookeeperService.builder(connectString()).build());
                    when(curatorFramework.getState()).thenReturn(STOPPED);
                    assertNull(startCuratorFramework.apply(curatorFramework, duration), "ZookeeperHelper::startCuratorFramework(Duration="+duration.toMillis()+"ms) has to return null for stopped curatorFramework");
                }
        );
    }

    @Test
    void testBlockUntilConnectedWithFalseBlock() throws NoSuchMethodException, InterruptedException {
        Method method = ZookeeperHelper.class.getDeclaredMethod("blockUntilConnected", CuratorFramework.class, Duration.class);
        method.setAccessible(true);
        BiFunction<CuratorFramework, Duration, CuratorFramework> blockUntilConnected =
                (framework, duration) -> (CuratorFramework) wrapUncheckedExceptions(() -> method.invoke(null, framework, duration));
        CuratorFramework curatorFramework = mock(CuratorFramework.class);
        when(curatorFramework.blockUntilConnected(anyInt(), any())).thenReturn(false);
        assertNull(
                blockUntilConnected.apply(curatorFramework, Duration.ofSeconds(1)),
                "ZookeeperHelper::blockUntilConnected has to return null for stopped curatorFramework"
        );
        reset(curatorFramework);
        when(curatorFramework.blockUntilConnected(anyInt(), any())).thenReturn(true);
        assertSame(
                curatorFramework,
                blockUntilConnected.apply(curatorFramework, Duration.ofSeconds(1)),
                "ZookeeperHelper::blockUntilConnected has to return curatorFrameworkif it's not stopped"
        );
    }

    @SneakyThrows
    static void shutdownTestServer(CuratorFramework curatorFramework, Duration duration) {
        AtomicReference<ConnectionState> connectionState = new AtomicReference<>(
                curatorFramework == null || !curatorFramework.getZookeeperClient().isConnected() ? LOST : CONNECTED
        );
        Runnable finalyze = () -> {};
        if (curatorFramework != null) {
            ConnectionStateListener listener = (client, newState) -> {
                if (LOST.equals(newState)) {
                    synchronized (connectionState) {
                        connectionState.set(LOST);
                        connectionState.notify();
                    }
                }
            };
            Listenable<ConnectionStateListener> listenable = curatorFramework.getConnectionStateListenable();
            listenable.addListener(listener);
            finalyze = () -> listenable.removeListener(listener);
        }
        try {
            if (zkServer != null) {
                zkServer.stop();
                zkServer.close();
            }
        } finally {
            if (curatorFramework != null) {
                Instant limit = Instant.now().plus(duration);
                while (Instant.now().isBefore(limit)) {
                    synchronized (connectionState) {
                        if (LOST.equals(connectionState.get())) {
                            break;
                        } else {
                            connectionState.wait(50);
                        }
                    }
                }
            }
            finalyze.run();
            zkServer = null;
        }
    }

    @SneakyThrows
    static void restartTestServer(CuratorFramework curatorFramework, Duration duration) {
        try {
            shutdownTestServer(curatorFramework, duration);
        } finally {
            startZkServer();
        }
    }

    @SneakyThrows
    static void startZkServer() {
        zkServer = Stream.of(wrapUncheckedExceptions(() -> new TestingServer(instanceSpec, true)))
                .peek(server -> wrapUncheckedExceptions(() -> server.start()))
                .findFirst()
                .orElseThrow(() -> new Exception("Unable to start Zookeeper TestingServer"));
    }

    @BeforeEach
    void preCheck() {
        if (zkServer == null) {
            startZkServer();
        }
    }

    @BeforeAll
    static void startUp() {
        wrapUncheckedExceptions(ZookeeperHelperTest::startZkServer);
    }

    @AfterAll
    static void tearDown() throws IOException {
        try {
            if (zkServer != null) {
                zkServer.stop();
                zkServer.close();
            }
        } catch (NullPointerException ex) {
        } finally {
            zkServer = null;
        }
    }

    private static String connectString() {
        return Optional.ofNullable(zkServer).map(TestingServer::getConnectString).orElse(null);
    }

}