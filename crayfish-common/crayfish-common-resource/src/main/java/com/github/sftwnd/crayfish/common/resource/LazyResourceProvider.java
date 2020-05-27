package com.github.sftwnd.crayfish.common.resource;

import com.github.sftwnd.crayfish.common.exception.ExceptionUtils;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.PropertyVetoException;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *  Класс для ленивой инициализации доступа к ресурсу.
 *  Реализует провайдер соединения, который предоставляет ресурс по запросу
 *
 *  @author Andrey D. Shindarev
 *  2019.09.25
 */
@Slf4j
@SuppressWarnings({
        // Non-primitive fields should not be "volatile"
        // We have control volatile object carefully
        "squid:S3077"
})
public class LazyResourceProvider<P,R> implements ICloseableResourceProvider<R> {

    private static final Closeable EMPTY_CLOSEABLE = () -> {};
    @Getter @Setter
    private volatile boolean errorStack = false;

    public interface Constructor<P> {
        P construct() throws IOException;
    }

    public interface Provider<P,R> {
        R provide(@Nonnull P client) throws IOException;
    }

    // Комбинация provided + resource & provider == null рассматривается как destroyed и невозможно будет оформить connect пока не будет вызван медод reset()
    private boolean                               provided = false;
    private final Set<Class<? extends Throwable>> defaultAbsorbedThrows;
    private Set<Class<? extends Throwable>> absorbedThrows = new HashSet<>();
    @Nullable
    private volatile P               provider;
    @Nullable
    private volatile R               resource;
    @Nonnull
    private          Constructor<P>  providerConstructor;
    @Nonnull
    private          Provider<P,R>   resourceProvider;
    @Nullable
    private volatile BiConsumer<P,R> onDestroy = null;
    @Getter
    @Nullable
    private volatile Throwable error;
    private Function<R, Boolean> checkResource;

    private void setResource(final R resource) {
        boolean changeFlag = Optional.ofNullable(this.resource)
                .map(currentValue -> !currentValue.equals(resource))
                .orElseGet(() -> this.resource != resource);
        R oldResource = this.resource;
        try {
            if (changeFlag) {
                resourceChange(oldResource, resource);
            }
            this.resource = resource;
            if (changeFlag) {
                resourceChanged(oldResource, resource);
            }
        } catch (PropertyVetoException pvex) {
            this.error = pvex;
        }
    }

    private void setProvider(final P provider) {
        boolean changeFlag = Optional.ofNullable(this.provider)
                .map(currentValue -> !currentValue.equals(provider))
                .orElseGet(() -> this.provider != provider);
        P oldProvider = this.provider;
        try {
            if (changeFlag) {
                providerChange(oldProvider, provider);
            }
            this.provider = provider;
            if (changeFlag) {
                providerChanged(oldProvider, provider);
            }
        } catch (PropertyVetoException pvex) {
            this.error = pvex;
        }
    }

    public LazyResourceProvider(@Nonnull Constructor<P> providerConstructor,
                                @Nonnull Provider<P,R> resourceProvider,
                                @Nullable Set<Class<? extends Throwable>> baseAbsorbedThrows,
                                @Nullable Function<R, Boolean> checkResource) {
        this.resourceProvider = Objects.requireNonNull(resourceProvider,"LazyResourceProvider::new - resourceProvider is null");
        this.providerConstructor = Objects.requireNonNull(providerConstructor,"LazyResourceProvider::new - providerConstructor is null");
        this.defaultAbsorbedThrows = Optional.ofNullable(baseAbsorbedThrows).filter(s -> !s.isEmpty()).map(Set::stream).map(s -> s.collect(Collectors.toSet())).orElse(Collections.emptySet());
        resetAbsorbedThrows();
        setCheckResource(checkResource);
    }

    @Generated
    public LazyResourceProvider(@Nonnull Constructor<P> providerConstructor, @Nonnull Provider<P,R> resourceProvider, @Nullable Set<Class<? extends Throwable>> baseAbsorbedThrows) {
        this(providerConstructor, resourceProvider, baseAbsorbedThrows, null);
    }

    @Generated
    public LazyResourceProvider(@Nonnull Constructor<P> providerConstructor, @Nonnull Provider<P,R> resourceProvider, @Nullable Function<R, Boolean> checkResource) {
        this(providerConstructor, resourceProvider, null, checkResource);
    }

    @Generated
    public LazyResourceProvider(@Nonnull Constructor<P> providerConstructor, @Nonnull Provider<P,R> resourceProvider) {
        this(providerConstructor, resourceProvider, null, null);
    }

    public final synchronized P construct() {
        this.error = null;
        return !this.provided && this.provider == null
             ? doConstruct()
             : this.provider;
    }

    @Override
    @SuppressWarnings({
            // Throwable and Error should not be caught
            // We have rethrow the throwable on post process if the exception is not absorbed
            "squid:S1181"
    })
    public synchronized R provide() {
        this.error = null;
        R result = !provided ? provide(provider) : resource;
        try {
            if (result == null || checkResource.apply(result)) {
                return result;
            }

        }  catch (Throwable throwable) {
            processError("Unable to check client connection: {}", throwable);
        }
        this.provided = false;
        setResource(null);
        rethrow();
        return null;
    }

    @Override
    public synchronized void close() {
        if (!isDestroyed()) {
            setResource(null);
            this.provided = false;
            this.error = null;
        }
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
            // In Java 11 change to .toUnmodifiableSet
            return absorbedThrows.stream().collect(Collectors.toSet());
        }
    }
    
    public final void addAbsorbedThrow(Class<? extends Throwable> clazz) {
        Optional.ofNullable(clazz).ifPresent( c -> {
            synchronized (absorbedThrows) {
                absorbedThrows.add(clazz);
            }
        });
    }

    public final void removeAbsorbedThrow(Class<? extends Throwable> clazz) {
        synchronized (absorbedThrows) {
            Optional.ofNullable(clazz)
                    .filter(absorbedThrows::contains)
                    .ifPresent(absorbedThrows::remove);
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

    public synchronized void setOnDestroy(BiConsumer<P, R> onDestroy) {
        this.onDestroy = onDestroy;
    }

    public synchronized void setCheckResource(@Nullable Function<R, Boolean> checkResource) {
        this.checkResource = Optional.ofNullable(checkResource).orElse(r -> true);
    }

    @SneakyThrows
    @SuppressWarnings("try")
    private synchronized void onDestroy(@Nonnull P provider, @Nonnull R client) {
        try(Closeable prv = Optional.ofNullable(provider).filter(Objects::nonNull).filter(Closeable.class::isInstance).map(Closeable.class::cast).orElse(EMPTY_CLOSEABLE);
            Closeable res = Optional.ofNullable(resource).filter(Objects::nonNull).filter(Closeable.class::isInstance).map(Closeable.class::cast).orElse(EMPTY_CLOSEABLE) )
        {
            Optional.ofNullable(onDestroy).ifPresent(ondestroy -> ondestroy.accept(provider, client));
        }
    }

    @Generated
    public final synchronized boolean isDestroyed() {
        return this.provided && this.resource == null && this.provider == null;
    }

    @SuppressWarnings("squid:S1181")
    public synchronized void destroy() {
        this.error = null;
        try {
            if (!isDestroyed()) {
                onDestroy(this.provider, this.resource);
            }
        } catch (Throwable ioex) {
            processError("Exception on destroy of "+this.getClass().getSimpleName()+ ".destroy(): {}", ioex);
        } finally {
            setResource(null);
            setProvider(null);
            this.provided = true;
        }
        rethrow();
    }

    private void processError(String text, @Nullable Throwable throwable) {
        // In Java 11 change to isBlank
        String str = Optional.ofNullable(throwable).map(Throwable::getLocalizedMessage).filter(s -> !s.trim().isEmpty()).orElse("");
        if (throwable == null) {
            logger.warn(text, str);
        } else {
            logger.error(text, str, rethrowable(throwable));
            this.error = throwable;
        }
    }

    @Generated
    private Throwable rethrowable(Throwable throwable) {
        return logger.isDebugEnabled() || isErrorStack() ? throwable : null;
    }

    // Медот даст возможность заново открыть destroyed объект
    public final synchronized void reset() {
        if (!isDestroyed()) {
            destroy();
        }
        this.error = null;
        this.provided = false;
    }

    @Generated
    public synchronized boolean isProvided() {
        return provided && resource != null;
    }

    // Вызов идёт в synchronized секции и на момент вызова provided = false
    @SuppressWarnings("squid:S1181")
    private synchronized R provide(@Nullable P provider) {
        try {
            setResource(Optional.ofNullable( Optional.ofNullable(provider).orElse(doConstruct()) )
                               .map(this::doProvide)
                               .orElse(null));
            if (this.resource == null) {
                processError("Unable to provide resource", null);
            }
        } catch (Throwable throwable) {
            setResource(null);
        } finally {
            this.provided = this.resource != null;
        }
        rethrow();
        return this.resource;
    }

    @SuppressWarnings("squid:S1181")
    private final synchronized R doProvide(@Nonnull P provider) {
        try {
            return resourceProvider.provide(provider);
        } catch (Throwable throwable) {
            processError("Unable to provide resource: {}", throwable);
        }
        rethrow();
        return null;
    }

    @SuppressWarnings("squid:S1181")
    private final synchronized P doConstruct() {
        try {
            setProvider(providerConstructor.construct());
        } catch (Throwable throwable) {
            processError("Unable to construct provider: {}", throwable);
            setProvider(null);
        }
        rethrow();
        return this.provider;
    }

    @SuppressWarnings({
            /* Methods should not be empty
               Empty methots are defined for overriding in childs
             */
            "squid:S1186"
    })
    protected void providerChange(P oldValue, P newValue) throws PropertyVetoException {
    }

    @SuppressWarnings("squid:S1186")
    protected void providerChanged(P oldValue, P newValue) {
    }

    @SuppressWarnings("squid:S1186")
    protected void resourceChange(R oldValue, R newValue) throws PropertyVetoException {
    }

    @SuppressWarnings("squid:S1186")
    protected void resourceChanged(R oldValue, R newValue) {
    }

}
