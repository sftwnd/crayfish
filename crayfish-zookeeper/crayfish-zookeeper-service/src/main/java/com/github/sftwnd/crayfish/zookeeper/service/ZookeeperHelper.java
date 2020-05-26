package com.github.sftwnd.crayfish.zookeeper.service;

import com.github.sftwnd.crayfish.common.resource.LazyResourceProvider;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.state.ConnectionState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.sftwnd.crayfish.common.exception.ExceptionUtils.wrapUncheckedExceptions;
import static java.time.Duration.ZERO;
import static org.apache.curator.framework.api.CuratorEventType.CLOSING;
import static org.apache.curator.framework.imps.CuratorFrameworkState.LATENT;
import static org.apache.curator.framework.imps.CuratorFrameworkState.STOPPED;
import static org.apache.curator.framework.state.ConnectionState.LOST;

@Slf4j
/**
 * ZookeeperService implementation as LazyResourceProvider extension
 */
public class ZookeeperHelper extends LazyResourceProvider<Builder, CuratorFramework> implements ZookeeperService {

    public static final Duration AUTOSTART_DURATION = Duration.of(5, ChronoUnit.SECONDS);

    public ZookeeperHelper(
            @Nonnull Builder builder,
            @Nullable Duration autostartDuration,
            @Nullable Set<Class<? extends Throwable>> baseAbsorbedThrows,
            @Nullable Function<CuratorFramework, Boolean> checkCuratorFramework) {
        super(builder(Objects.requireNonNull(builder, "ZookeeperHelper::new - builder is null")),
                provider(autostartDuration), baseAbsorbedThrows, checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(
            @Nonnull Builder builder,
            @Nullable Set<Class<? extends Throwable>> baseAbsorbedThrows,
            @Nullable Function<CuratorFramework, Boolean> checkCuratorFramework) {
        this(builder, null, baseAbsorbedThrows, checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(
            @Nonnull Builder builder,
            @Nullable Duration autostartDuration,
            @Nullable Function<CuratorFramework, Boolean> checkCuratorFramework) {
        this(builder, autostartDuration, null, checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(
            @Nonnull Builder builder,
            @Nullable Function<CuratorFramework, Boolean> checkCuratorFramework) {
        this(builder, null, null, checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(
            @Nonnull Builder builder,
            @Nullable Duration autostartDuration,
            @Nullable Set<Class<? extends Throwable>> baseAbsorbedThrows) {
        this(builder, autostartDuration, baseAbsorbedThrows, ZookeeperHelper::checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(
            @Nonnull Builder builder,
            @Nullable Set<Class<? extends Throwable>> baseAbsorbedThrows) {
        this(builder, null, baseAbsorbedThrows, ZookeeperHelper::checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(@Nonnull Builder builder,
                           @Nullable Duration autostartDuration) {
        this(builder, autostartDuration, null, ZookeeperHelper::checkCuratorFramework);
    }

    @Generated
    public ZookeeperHelper(@Nonnull Builder builder) {
        this(builder, null, null, ZookeeperHelper::checkCuratorFramework);
    }

    private static @Nonnull
    Constructor<Builder> builder(final @Nonnull Builder builder) {
        Objects.requireNonNull(builder, "ZookeeperHelper::new - builder is null");
        return () -> builder;
    }

    @Override
    protected void resourceChange(CuratorFramework oldValue, CuratorFramework newValue) {
        Optional.ofNullable(oldValue).ifPresent(this::unregisterCuratorFramework);
    }

    @Generated
    @Override
    protected void resourceChanged(CuratorFramework oldValue, CuratorFramework newValue) {
        Optional.ofNullable(newValue)
                .filter(curatorFramework -> curatorFramework.getState() != STOPPED)
                .ifPresent(this::registerCuratorFramework);
    }

    private void registerCuratorFramework(CuratorFramework client) {
        client.getCuratorListenable().addListener(this::eventReceived);
        client.getConnectionStateListenable().addListener(this::stateChanged);
    }

    private void unregisterCuratorFramework(CuratorFramework client) {
        client.getCuratorListenable().removeListener(this::eventReceived);
        client.getConnectionStateListenable().removeListener(this::stateChanged);
    }

    private void eventReceived(CuratorFramework client, CuratorEvent event) {
        Optional.ofNullable(event)
                .map(CuratorEvent::getType)
                .filter(CLOSING::equals)
                .ifPresent(type -> close());
    }

    private void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (newState == LOST) {
            this.close();
        }
    }

    private static CuratorFramework blockUntilConnected(final @Nonnull CuratorFramework curatorFramework, final @Nonnull Duration startDuration) {
        return Optional.of(startDuration)
                .filter(ZERO::equals)
                .map(duration -> wrapUncheckedExceptions( () -> {
                    curatorFramework.blockUntilConnected();
                    return curatorFramework;
                })).orElseGet(() -> wrapUncheckedExceptions(() ->
                        curatorFramework.blockUntilConnected((int) startDuration.toMillis(), TimeUnit.MILLISECONDS)
                                ? curatorFramework : null
                ));
    }

    @SuppressWarnings("unchecked")
    private static @Nonnull CuratorFramework startCuratorFramework(final @Nonnull CuratorFramework curatorFramework, final @Nonnull Duration startDuration) {
        curatorFramework.start();
        return Optional.of(blockUntilConnected(curatorFramework, startDuration))
                .filter(framework -> !STOPPED.equals(framework.getState()))
                .orElseGet(() -> {
                    logger.warn("Unable to start CuratorFramework connection for Zookeeper.");
                    return null;
                });
    }

    private static @Nonnull Provider<Builder, CuratorFramework> provider(final @Nullable Duration startDuration) {
        return startDuration == null ? Builder::build
             : builder -> Stream.of(builder)
                .map(Builder::build)
                .map(framework -> startCuratorFramework(framework, startDuration))
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Default CuratorFramework check service before return it as result of provide operation
     * @param curatorFramework checkable CuratorFramework
     * @return result of check operation
     */
    public static boolean checkCuratorFramework(CuratorFramework curatorFramework) {
        return Optional.ofNullable(curatorFramework)
                // Curator is not stopped
                .filter(curator -> curator.getState() != STOPPED)
                // Curator is not started or is connected
                .map(curator ->curatorFramework.getState() == LATENT || curatorFramework.getZookeeperClient().isConnected())
                .orElse(false);
    }

}
