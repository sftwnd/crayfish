package com.github.sftwnd.crayfish.common.resource;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 *
 *  Класс для ленивой инициализации доступа к ресурсу.
 *  Реализует провайдер соединения, который предоставляет ресурс по запросу
 *
 *  @author Andrey D. Shindarev
 *  @ 2019.09.25
 */
@Slf4j
public class LazyResourceProvider<P,R> implements IResourceProvider<R>, Closeable {

    private static final Closeable EMPTY_CLOSEABLE = () -> {};

    public interface Constructor<P> {
        P construct() throws IOException;
    }

    public interface Provider<P,R> {
        R provide(@Nonnull P client) throws IOException;
    }

    // Комбинация provided + resource & provider == null рассматривается как closed() и невозможно будет оформить connect пока не будет вызван медод reset()
    private boolean                               provided = false;
    private final Set<Class<? extends Throwable>> defaultAbsorbedThrows;
    private Set<Class<? extends Throwable>> absorbedThrows = new LinkedHashSet<>();
    @SuppressWarnings("squid:S3077")
    @Nullable
    private volatile P               provider;
    @SuppressWarnings("squid:S3077")
    @Nullable
    private volatile R               resource;
    @Nonnull
    private          Constructor<P>  providerConstructor;
    @Nonnull
    private          Provider<P,R>   resourceProvider;
    @SuppressWarnings("squid:S3077")
    @Nullable
    private volatile BiConsumer<P,R> onClose = null;
    @SuppressWarnings("squid:S3077")
    @Getter
    @Nullable
    private volatile Throwable error;

    protected LazyResourceProvider(@Nonnull Constructor<P> providerConstructor, @Nonnull Provider<P,R> resourceProvider, Set<Class<? extends Throwable>> baseAbsorbedThrows) {
        this.resourceProvider = Objects.requireNonNull(resourceProvider,"LazyResourceProvider::new - connector is null");
        this.providerConstructor = Objects.requireNonNull(providerConstructor,"LazyResourceProvider::new - constructor is null");
        this.defaultAbsorbedThrows = Optional.ofNullable(baseAbsorbedThrows).filter(s -> !s.isEmpty()).map(Set::stream).map(s -> s.collect(Collectors.toSet())).orElse(Collections.emptySet());
        resetAbsorbedThrows();
    }

    protected LazyResourceProvider(@Nonnull Constructor<P> providerConstructor, @Nonnull Provider<P,R> resourceProvider) {
        this(providerConstructor, resourceProvider, null);
    }

    public final synchronized P construct() {
        this.error = null;
        return !this.provided && this.provider == null
             ? doConstruct()
             : this.provider;
    }

    protected synchronized void setProvided(P provider) {
        this.provider = provider;
    }

    @Override
    public final synchronized R provide() {
        this.error = null;
        return !provided ? provide(provider) : resource;
    }

    public final void clearAbsorbedThrows() {
        synchronized (absorbedThrows) {
            absorbedThrows.clear();
        }
    }

    public final void resetAbsorbedThrows() {
        synchronized (absorbedThrows) {
            clearAbsorbedThrows();
            absorbedThrows.addAll(defaultAbsorbedThrows);
        }
    }

    public Set<Class<? extends Throwable>> getAbsorbedThrows() {
        synchronized (absorbedThrows) {
            return absorbedThrows.stream().collect(Collectors.toUnmodifiableSet());
        }
    }
    
    public final void addAbsorbedThrow(Class<? extends Throwable> clazz) {
        if (clazz != null) {
            synchronized (absorbedThrows) {
                absorbedThrows.add(clazz);
            }
        }
    }

    public final void removeAbsorbedThrow(Class<? extends Throwable> clazz) {
        synchronized (absorbedThrows) {
            if (clazz != null && absorbedThrows.contains(clazz)) {
                absorbedThrows.remove(clazz);
            }
        }
    }

    /**
     * Если данный метод не гасится, то пробрасываем исключение далее
     */
    @SneakyThrows
    private final void rethrow() {
        synchronized (absorbedThrows) {
            Optional.ofNullable(this.error)
                    .filter(e -> !this.absorbedThrows.contains(e.getClass())).map(Throwable.class::cast)
                    .ifPresent(ExceptionUtils::uncheckExceptions);
        }
    }
    
    protected synchronized void setResource(R resource) {
        this.error = null;
        this.resource = resource;
        this.provided = false;
    }

    public synchronized void setOnClose(BiConsumer<P, R> onClose) {
        this.onClose = onClose;
    }

    @SneakyThrows
    @SuppressWarnings("try")
    private synchronized void onClose(@Nonnull P provider, @Nonnull R client) {
        try(Closeable prv = Optional.ofNullable(provider).filter(Objects::nonNull).filter(Closeable.class::isInstance).map(Closeable.class::cast).orElse(EMPTY_CLOSEABLE);
            Closeable res = Optional.ofNullable(resource).filter(Objects::nonNull).filter(Closeable.class::isInstance).map(Closeable.class::cast).orElse(EMPTY_CLOSEABLE) )
        {
            Optional.ofNullable(onClose).ifPresent(onclose -> onclose.accept(provider, client));
        }
    }

    @Override
    @SuppressWarnings("squid:S1181")
    public synchronized void close() {
        this.error = null;
        try {
            if (isProvided()) {
                onClose(this.provider, this.resource);
            }
        } catch (Throwable ioex) {
            processError("Exception in close of "+this.getClass().getSimpleName()+ ".close(): {}", ioex);
        } finally {
            this.resource = null;
            this.provider = null;
            this.provided = true;
        }
        rethrow();
    }

    private void processError(String text, @Nullable Throwable throwable) {
        logger.error(text, Optional.ofNullable(throwable).map(Throwable::getLocalizedMessage).filter(s -> !s.isBlank()).orElse(""), logger.isDebugEnabled() ? throwable : null);
        this.error = throwable;
    }

    // Медот даст возможность заново открыть closed объект
    public final synchronized void reset() {
        this.error = null;
        this.provided = false;
    }

    public synchronized boolean isProvided() {
        return provided && resource != null;
    }

    // Вызов идёт в synchronized секции и на момент вызова provided = false
    @SuppressWarnings("squid:S1181")
    private synchronized R provide(@Nullable P provider) {
        try {
            this.resource = Optional.ofNullable( Optional.ofNullable(provider).orElse(doConstruct()) )
                               .map(this::doProvide)
                               .orElse(null);
        } catch (Throwable throwable) {
            processError("Unable to provide client connection: {}", throwable);
            this.resource = null;
        } finally {
            this.provided = this.resource != null;
        }
        rethrow();
        return this.resource;
    }

    @SuppressWarnings("squid:S1181")
    private final synchronized R doProvide(@Nullable P provider) {
        try {
            this.resource = this.provided || provider == null ? this.resource : resourceProvider.provide(provider);
        } catch (Throwable throwable) {
            processError("Unable to provide resource: {}", throwable);
            this.resource = null;
        }
        rethrow();
        return this.resource;
    }

    @SuppressWarnings("squid:S1181")
    private final synchronized P doConstruct() {
        try {
            this.provider = providerConstructor.construct();
        } catch (Throwable throwable) {
            processError("Unable to construct provider: {}", throwable);
            this.provider = null;
        }
        rethrow();
        return this.provider;
    }

}
