package org.vaadin.flow.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Loader")
public class LoaderTest {

    private static final Duration LOADING_DELAY = Duration.ofMillis(50);
    private static final Duration SLOW_OPERATION = Duration.ofMillis(200);
    /** Time to wait after load() returns so the timer thread can settle. */
    private static final Duration SETTLE_TIME = Duration.ofMillis(150);

    @Nested
    @DisplayName("when created with NotLoaded initial state")
    class InitialNotLoaded {

        private final AtomicBoolean notLoaded = new AtomicBoolean();
        private final AtomicBoolean loading = new AtomicBoolean();
        private final AtomicReference<String> ready = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean failed = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            new Loader.Builder<String>()
                    .bindNotLoaded(notLoaded::set)
                    .bindLoading(loading::set)
                    .bindReady(ready::set)
                    .bindError(error::set)
                    .bindFailed(failed::set)
                    .build();
        }

        @Test
        @DisplayName("notLoaded callback receives true")
        void notLoaded_callback_receives_true() {
            assertThat(notLoaded).isTrue();
        }

        @Test
        @DisplayName("loading callback receives false")
        void loading_callback_receives_false() {
            assertThat(loading).isFalse();
        }

        @Test
        @DisplayName("ready callback receives null")
        void ready_callback_receives_null() {
            assertThat(ready.get()).isNull();
        }

        @Test
        @DisplayName("error callback receives null")
        void error_callback_receives_null() {
            assertThat(error.get()).isNull();
        }

        @Test
        @DisplayName("failed callback receives false")
        void failed_callback_receives_false() {
            assertThat(failed).isFalse();
        }
    }

    @Nested
    @DisplayName("when created with Loading initial state")
    class InitialLoading {

        private final AtomicBoolean notLoaded = new AtomicBoolean(true);
        private final AtomicBoolean loading = new AtomicBoolean();
        private final AtomicReference<String> ready = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean failed = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            new Loader.Builder<String>()
                    .bindNotLoaded(notLoaded::set)
                    .bindLoading(loading::set)
                    .bindReady(ready::set)
                    .bindError(error::set)
                    .bindFailed(failed::set)
                    .build(Loadable.loading());
        }

        @Test
        @DisplayName("notLoaded callback receives false")
        void notLoaded_callback_receives_false() {
            assertThat(notLoaded).isFalse();
        }

        @Test
        @DisplayName("loading callback receives true")
        void loading_callback_receives_true() {
            assertThat(loading).isTrue();
        }

        @Test
        @DisplayName("ready callback receives null")
        void ready_callback_receives_null() {
            assertThat(ready.get()).isNull();
        }

        @Test
        @DisplayName("error callback receives null")
        void error_callback_receives_null() {
            assertThat(error.get()).isNull();
        }

        @Test
        @DisplayName("failed callback receives false")
        void failed_callback_receives_false() {
            assertThat(failed).isFalse();
        }
    }

    @Nested
    @DisplayName("when created with Ready initial state")
    class InitialReady {

        private final AtomicBoolean notLoaded = new AtomicBoolean(true);
        private final AtomicBoolean loading = new AtomicBoolean(true);
        private final AtomicReference<String> ready = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean failed = new AtomicBoolean(true);

        @BeforeEach
        void setUp() {
            new Loader.Builder<String>()
                    .bindNotLoaded(notLoaded::set)
                    .bindLoading(loading::set)
                    .bindReady(ready::set)
                    .bindError(error::set)
                    .bindFailed(failed::set)
                    .build(Loadable.ready("hello"));
        }

        @Test
        @DisplayName("notLoaded callback receives false")
        void notLoaded_callback_receives_false() {
            assertThat(notLoaded).isFalse();
        }

        @Test
        @DisplayName("loading callback receives false")
        void loading_callback_receives_false() {
            assertThat(loading).isFalse();
        }

        @Test
        @DisplayName("ready callback receives the result")
        void ready_callback_receives_the_result() {
            assertThat(ready.get()).isEqualTo("hello");
        }

        @Test
        @DisplayName("error callback receives null")
        void error_callback_receives_null() {
            assertThat(error.get()).isNull();
        }

        @Test
        @DisplayName("failed callback receives false")
        void failed_callback_receives_false() {
            assertThat(failed).isFalse();
        }
    }

    @Nested
    @DisplayName("when created with Failed initial state")
    class InitialFailed {

        private final Exception cause = new RuntimeException("boom");
        private final AtomicBoolean notLoaded = new AtomicBoolean(true);
        private final AtomicBoolean loading = new AtomicBoolean(true);
        private final AtomicReference<String> ready = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean failed = new AtomicBoolean();

        @BeforeEach
        void setUp() {
            new Loader.Builder<String>()
                    .bindNotLoaded(notLoaded::set)
                    .bindLoading(loading::set)
                    .bindReady(ready::set)
                    .bindError(error::set)
                    .bindFailed(failed::set)
                    .build(Loadable.failed(cause));
        }

        @Test
        @DisplayName("notLoaded callback receives false")
        void notLoaded_callback_receives_false() {
            assertThat(notLoaded).isFalse();
        }

        @Test
        @DisplayName("loading callback receives false")
        void loading_callback_receives_false() {
            assertThat(loading).isFalse();
        }

        @Test
        @DisplayName("ready callback receives null")
        void ready_callback_receives_null() {
            assertThat(ready.get()).isNull();
        }

        @Test
        @DisplayName("error callback receives the exception")
        void error_callback_receives_the_exception() {
            assertThat(error.get()).isSameAs(cause);
        }

        @Test
        @DisplayName("failed callback receives true")
        void failed_callback_receives_true() {
            assertThat(failed).isTrue();
        }
    }

    @Nested
    @DisplayName("set()")
    class Set {

        private final AtomicBoolean notLoaded = new AtomicBoolean();
        private final AtomicBoolean loading = new AtomicBoolean();
        private final AtomicReference<String> ready = new AtomicReference<>();
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean failed = new AtomicBoolean();
        private Loader<String> loader;

        @BeforeEach
        void setUp() {
            loader = new Loader.Builder<String>()
                    .bindNotLoaded(notLoaded::set)
                    .bindLoading(loading::set)
                    .bindReady(ready::set)
                    .bindError(error::set)
                    .bindFailed(failed::set)
                    .build();
        }

        @Test
        @DisplayName("transitions from NotLoaded to Ready")
        void transitions_from_not_loaded_to_ready() {
            loader.set(Loadable.ready("result"));

            assertThat(notLoaded).isFalse();
            assertThat(loading).isFalse();
            assertThat(ready.get()).isEqualTo("result");
            assertThat(error.get()).isNull();
            assertThat(failed).isFalse();
        }

        @Test
        @DisplayName("transitions from NotLoaded to Loading")
        void transitions_from_not_loaded_to_loading() {
            loader.set(Loadable.loading());

            assertThat(notLoaded).isFalse();
            assertThat(loading).isTrue();
            assertThat(ready.get()).isNull();
            assertThat(error.get()).isNull();
            assertThat(failed).isFalse();
        }

        @Test
        @DisplayName("transitions from NotLoaded to Failed")
        void transitions_from_not_loaded_to_failed() {
            var cause = new RuntimeException("oops");
            loader.set(Loadable.failed(cause));

            assertThat(notLoaded).isFalse();
            assertThat(loading).isFalse();
            assertThat(ready.get()).isNull();
            assertThat(error.get()).isSameAs(cause);
            assertThat(failed).isTrue();
        }

        @Test
        @DisplayName("does not fire callbacks when value is unchanged")
        void does_not_fire_callbacks_when_value_is_unchanged() {
            // Set to Ready first
            loader.set(Loadable.ready("value"));
            // Reset tracking
            notLoaded.set(true);
            loading.set(true);
            ready.set("marker");
            error.set(new RuntimeException("marker"));
            failed.set(true);

            // Set to the same Ready value
            loader.set(Loadable.ready("value"));

            // Callbacks should NOT have been invoked, so markers remain
            assertThat(notLoaded).isTrue();
            assertThat(loading).isTrue();
            assertThat(ready.get()).isEqualTo("marker");
            assertThat(error.get()).isNotNull();
            assertThat(failed).isTrue();
        }

        @Test
        @DisplayName("fires callbacks when value changes to a different Ready")
        void fires_callbacks_when_value_changes_to_different_ready() {
            loader.set(Loadable.ready("first"));
            loader.set(Loadable.ready("second"));

            assertThat(ready.get()).isEqualTo("second");
        }
    }

    @Nested
    @DisplayName("retainResult")
    class RetainResult {

        @Nested
        @DisplayName("when retainResult is true (default)")
        class Enabled {

            private final AtomicReference<String> ready = new AtomicReference<>();
            private Loader<String> loader;

            @BeforeEach
            void setUp() {
                loader = new Loader.Builder<String>()
                        .bindReady(ready::set)
                        .build(Loadable.ready("initial"));
            }

            @Test
            @DisplayName("ready callback is not cleared when transitioning to Loading")
            void ready_not_cleared_on_loading() {
                loader.set(Loadable.loading());
                assertThat(ready.get()).isEqualTo("initial");
            }

            @Test
            @DisplayName("ready callback is not cleared when transitioning to Failed")
            void ready_not_cleared_on_failed() {
                loader.set(Loadable.failed(new RuntimeException("err")));
                assertThat(ready.get()).isEqualTo("initial");
            }

            @Test
            @DisplayName("ready callback is not cleared when transitioning to NotLoaded")
            void ready_not_cleared_on_not_loaded() {
                loader.set(Loadable.notLoaded());
                assertThat(ready.get()).isEqualTo("initial");
            }
        }

        @Nested
        @DisplayName("when retainResult is false")
        class Disabled {

            private final AtomicReference<String> ready = new AtomicReference<>();
            private Loader<String> loader;

            @BeforeEach
            void setUp() {
                loader = new Loader.Builder<String>()
                        .bindReady(ready::set)
                        .setRetainResult(false)
                        .build(Loadable.ready("initial"));
            }

            @Test
            @DisplayName("ready callback is cleared when transitioning to Loading")
            void ready_cleared_on_loading() {
                loader.set(Loadable.loading());
                assertThat(ready.get()).isNull();
            }

            @Test
            @DisplayName("ready callback is cleared when transitioning to Failed")
            void ready_cleared_on_failed() {
                loader.set(Loadable.failed(new RuntimeException("err")));
                assertThat(ready.get()).isNull();
            }

            @Test
            @DisplayName("ready callback is cleared when transitioning to NotLoaded")
            void ready_cleared_on_not_loaded() {
                loader.set(Loadable.notLoaded());
                assertThat(ready.get()).isNull();
            }
        }
    }

    @Nested
    @DisplayName("load()")
    class Load {

        @Nested
        @DisplayName("when the supplier completes before the loading delay")
        class FastSuccess {

            private final AtomicBoolean loading = new AtomicBoolean();
            private final AtomicReference<String> ready = new AtomicReference<>();
            private final AtomicBoolean failed = new AtomicBoolean();

            @BeforeEach
            void setUp() throws InterruptedException {
                var loader = new Loader.Builder<String>()
                        .bindLoading(loading::set)
                        .bindReady(ready::set)
                        .bindFailed(failed::set)
                        .build();
                loader.load(() -> "fast", LOADING_DELAY);
                Thread.sleep(SETTLE_TIME.toMillis());
            }

            @Test
            @DisplayName("ready callback receives the result")
            void ready_callback_receives_the_result() {
                assertThat(ready.get()).isEqualTo("fast");
            }

            @Test
            @DisplayName("loading callback is false")
            void loading_callback_is_false() {
                assertThat(loading).isFalse();
            }

            @Test
            @DisplayName("failed callback is false")
            void failed_callback_is_false() {
                assertThat(failed).isFalse();
            }
        }

        @Nested
        @DisplayName("when the supplier takes longer than the loading delay")
        class SlowSuccess {

            private final AtomicBoolean loadingWasTrue = new AtomicBoolean();
            private final AtomicBoolean loading = new AtomicBoolean();
            private final AtomicReference<String> ready = new AtomicReference<>();

            @BeforeEach
            void setUp() {
                var loader = new Loader.Builder<String>()
                        .bindLoading(v -> {
                            loading.set(v);
                            if (v) loadingWasTrue.set(true);
                        })
                        .bindReady(ready::set)
                        .build();
                loader.load(() -> {
                    try {
                        Thread.sleep(SLOW_OPERATION.toMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                    return "slow";
                }, LOADING_DELAY);
            }

            @Test
            @DisplayName("loading callback was set to true during the operation")
            void loading_was_true_during_operation() {
                assertThat(loadingWasTrue).isTrue();
            }

            @Test
            @DisplayName("loading callback is false after completion")
            void loading_is_false_after_completion() {
                assertThat(loading).isFalse();
            }

            @Test
            @DisplayName("ready callback receives the result")
            void ready_callback_receives_the_result() {
                assertThat(ready.get()).isEqualTo("slow");
            }
        }

        @Nested
        @DisplayName("when the supplier fails before the loading delay")
        class FastFailure {

            private final RuntimeException cause = new RuntimeException("boom");
            private final AtomicBoolean loading = new AtomicBoolean();
            private final AtomicReference<String> ready = new AtomicReference<>();
            private final AtomicReference<Exception> error = new AtomicReference<>();
            private final AtomicBoolean failed = new AtomicBoolean();

            @BeforeEach
            void setUp() throws InterruptedException {
                var loader = new Loader.Builder<String>()
                        .bindLoading(loading::set)
                        .bindReady(ready::set)
                        .bindError(error::set)
                        .bindFailed(failed::set)
                        .build();
                loader.load(() -> { throw cause; }, LOADING_DELAY);
                Thread.sleep(SETTLE_TIME.toMillis());
            }

            @Test
            @DisplayName("error callback receives the exception")
            void error_callback_receives_the_exception() {
                assertThat(error.get()).isSameAs(cause);
            }

            @Test
            @DisplayName("failed callback receives true")
            void failed_callback_receives_true() {
                assertThat(failed).isTrue();
            }

            @Test
            @DisplayName("loading callback is false")
            void loading_callback_is_false() {
                assertThat(loading).isFalse();
            }

            @Test
            @DisplayName("ready callback receives null")
            void ready_callback_receives_null() {
                assertThat(ready.get()).isNull();
            }
        }

        @Nested
        @DisplayName("when the supplier fails after the loading delay")
        class SlowFailure {

            private final RuntimeException cause = new RuntimeException("timeout");
            private final AtomicBoolean loadingWasTrue = new AtomicBoolean();
            private final AtomicReference<Exception> error = new AtomicReference<>();
            private final AtomicBoolean failed = new AtomicBoolean();

            @BeforeEach
            void setUp() {
                var loader = new Loader.Builder<String>()
                        .bindLoading(v -> {
                            if (v) loadingWasTrue.set(true);
                        })
                        .bindError(error::set)
                        .bindFailed(failed::set)
                        .build();
                loader.load(() -> {
                    try {
                        Thread.sleep(SLOW_OPERATION.toMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    throw cause;
                }, LOADING_DELAY);
            }

            @Test
            @DisplayName("loading callback was set to true during the operation")
            void loading_was_true_during_operation() {
                assertThat(loadingWasTrue).isTrue();
            }

            @Test
            @DisplayName("error callback receives the exception")
            void error_callback_receives_the_exception() {
                assertThat(error.get()).isSameAs(cause);
            }

            @Test
            @DisplayName("failed callback receives true")
            void failed_callback_receives_true() {
                assertThat(failed).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Builder defaults")
    class BuilderDefaults {

        @Test
        @DisplayName("default initial state is NotLoaded")
        void default_initial_state_is_not_loaded() {
            var notLoaded = new AtomicBoolean();
            new Loader.Builder<String>()
                    .bindNotLoaded(notLoaded::set)
                    .build();
            assertThat(notLoaded).isTrue();
        }

        @Test
        @DisplayName("default retainResult is true")
        void default_retain_result_is_true() {
            var ready = new AtomicReference<String>();
            var loader = new Loader.Builder<String>()
                    .bindReady(ready::set)
                    .build(Loadable.ready("value"));

            loader.set(Loadable.loading());

            assertThat(ready.get()).isEqualTo("value");
        }

        @Test
        @DisplayName("works with no callbacks bound")
        void works_with_no_callbacks_bound() {
            var loader = new Loader.Builder<String>().build();
            loader.set(Loadable.ready("value"));
            loader.set(Loadable.loading());
            loader.set(Loadable.failed(new RuntimeException("err")));
            loader.set(Loadable.notLoaded());
            // No exception thrown
        }
    }
}
