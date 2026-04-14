package org.vaadin.flow.data;

import com.vaadin.flow.function.SerializableConsumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

@NullMarked
public class Loader<T extends @Nullable Object> implements Serializable {

    // TODO Write JavaDocs, tests, consider a better name.

    private final @Nullable SerializableConsumer<Boolean> setNotLoaded;
    private final @Nullable SerializableConsumer<Boolean> setLoading;
    private final @Nullable SerializableConsumer<@Nullable T> setReady;
    private final @Nullable SerializableConsumer<@Nullable Exception> setError;
    private final @Nullable SerializableConsumer<Boolean> setFailed;

    private Loadable<T> value;

    public Loader(Loadable<T> initialValue,
                  @Nullable SerializableConsumer<Boolean> setNotLoaded,
                  @Nullable SerializableConsumer<Boolean> setLoading,
                  @Nullable SerializableConsumer<@Nullable T> setReady,
                  @Nullable SerializableConsumer<@Nullable Exception> setError,
                  @Nullable SerializableConsumer<Boolean> setFailed) {
        this.setNotLoaded = setNotLoaded;
        this.setLoading = setLoading;
        this.setReady = setReady;
        this.setError = setError;
        this.value = initialValue;
        this.setFailed = setFailed;
        callback();
    }

    public void set(Loadable<T> value) {
        if (!Objects.equals(this.value, value)) {
            this.value = value;
            callback();
        }
    }

    public void load(Supplier<T> outcomeProvider) {
        load(outcomeProvider, Duration.ofMillis(300));
    }

    public void load(Supplier<T> outcomeProvider, Duration loadingDelay) {
        var lock = new Object();
        var done = new boolean[]{false};

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(loadingDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            synchronized (lock) {
                if (!done[0]) {
                    set(Loadable.loading());
                }
            }
        });

        try {
            T result = outcomeProvider.get();
            synchronized (lock) {
                done[0] = true;
                set(Loadable.ready(result));
            }
        } catch (Exception exception) {
            synchronized (lock) {
                done[0] = true;
                set(Loadable.failed(exception));
            }
        }
    }

    private void callback() {
        setNotLoaded(value instanceof Loadable.NotLoaded<T>);
        setLoading(value instanceof Loadable.Loading<T>);
        if (value instanceof Loadable.Ready(T result)) {
            setReady(result);
        } else {
            setReady(null);
        }
        if (value instanceof Loadable.Failed(Exception error)) {
            setError(error);
        } else {
            setError(null);
        }
    }

    private void setNotLoaded(boolean notLoaded) {
        if (setNotLoaded != null) {
            setNotLoaded.accept(notLoaded);
        }
    }

    private void setLoading(boolean loading) {
        if (setLoading != null) {
            setLoading.accept(loading);
        }
    }

    private void setReady(@Nullable T result) {
        if (setReady != null) {
            setReady.accept(result);
        }
    }

    private void setError(@Nullable Exception cause) {
        if (setError != null) {
            setError.accept(cause);
        }
        if (setFailed != null) {
            setFailed.accept(cause != null);
        }
    }

    public static class Builder<T extends @Nullable Object> {

        private @Nullable SerializableConsumer<Boolean> setNotLoaded;
        private @Nullable SerializableConsumer<Boolean> setLoading;
        private @Nullable SerializableConsumer<@Nullable T> setReady;
        private @Nullable SerializableConsumer<@Nullable Exception> setError;
        private @Nullable SerializableConsumer<Boolean> setFailed;

        public Builder<T> bindNotLoaded(SerializableConsumer<Boolean> setNotLoaded) {
            this.setNotLoaded = setNotLoaded;
            return this;
        }

        public Builder<T> bindLoading(SerializableConsumer<Boolean> setLoading) {
            this.setLoading = setLoading;
            return this;
        }

        public Builder<T> bindReady(SerializableConsumer<@Nullable T> setReady) {
            this.setReady = setReady;
            return this;
        }

        public Builder<T> bindError(SerializableConsumer<@Nullable Exception> setError) {
            this.setError = setError;
            return this;
        }

        public Builder<T> bindFailed(SerializableConsumer<Boolean> setFailed) {
            this.setFailed = setFailed;
            return this;
        }

        public Loader<T> build(Loadable<T> initialValue) {
            return new Loader<>(initialValue, setNotLoaded, setLoading, setReady, setError, setFailed);
        }

        public Loader<T> build() {
            return build(Loadable.notLoaded());
        }
    }
}
