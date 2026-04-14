package org.vaadin.flow.signals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.vaadin.flow.data.Loadable;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoadableSignal")
public class LoadableSignalTest {

    private LoadableSignal<String> signal;

    @Nested
    @DisplayName("when created in the Loading state")
    class InitialLoading {

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.loading());
        }

        @Test
        @DisplayName("loading signal is true")
        void loading_signal_is_true() {
            assertThat(signal.loading().peek()).isTrue();
        }

        @Test
        @DisplayName("failed signal is false")
        void failed_signal_is_false() {
            assertThat(signal.failed().peek()).isFalse();
        }

        @Test
        @DisplayName("finished signal is null")
        void finished_signal_is_null() {
            assertThat(signal.finished().peek()).isNull();
        }

        @Test
        @DisplayName("error signal is null")
        void error_signal_is_null() {
            assertThat(signal.error().peek()).isNull();
        }
    }

    @Nested
    @DisplayName("when created in the Ready state")
    class InitialReady {

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.ready("hello"));
        }

        @Test
        @DisplayName("loading signal is false")
        void loading_signal_is_false() {
            assertThat(signal.loading().peek()).isFalse();
        }

        @Test
        @DisplayName("failed signal is false")
        void failed_signal_is_false() {
            assertThat(signal.failed().peek()).isFalse();
        }

        @Test
        @DisplayName("finished signal contains the result")
        void finished_signal_contains_the_result() {
            assertThat(signal.finished().peek()).isEqualTo("hello");
        }

        @Test
        @DisplayName("error signal is null")
        void error_signal_is_null() {
            assertThat(signal.error().peek()).isNull();
        }
    }

    @Nested
    @DisplayName("when created in the Failed state")
    class InitialFailed {

        private final Exception cause = new RuntimeException("something went wrong");

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.failed(cause));
        }

        @Test
        @DisplayName("loading signal is false")
        void loading_signal_is_false() {
            assertThat(signal.loading().peek()).isFalse();
        }

        @Test
        @DisplayName("failed signal is true")
        void failed_signal_is_true() {
            assertThat(signal.failed().peek()).isTrue();
        }

        @Test
        @DisplayName("finished signal is null")
        void finished_signal_is_null() {
            assertThat(signal.finished().peek()).isNull();
        }

        @Test
        @DisplayName("error signal contains the exception")
        void error_signal_contains_the_exception() {
            assertThat(signal.error().peek()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("when transitioning from Loading to Ready")
    class LoadingToReady {

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.loading());
            signal.set(Loadable.ready("result"));
        }

        @Test
        @DisplayName("loading signal becomes false")
        void loading_becomes_false() {
            assertThat(signal.loading().peek()).isFalse();
        }

        @Test
        @DisplayName("finished signal contains the result")
        void finished_contains_result() {
            assertThat(signal.finished().peek()).isEqualTo("result");
        }

        @Test
        @DisplayName("failed signal remains false")
        void failed_remains_false() {
            assertThat(signal.failed().peek()).isFalse();
        }

        @Test
        @DisplayName("error signal remains null")
        void error_remains_null() {
            assertThat(signal.error().peek()).isNull();
        }
    }

    @Nested
    @DisplayName("when transitioning from Loading to Failed")
    class LoadingToFailed {

        private final Exception cause = new IllegalStateException("timeout");

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.loading());
            signal.set(Loadable.failed(cause));
        }

        @Test
        @DisplayName("loading signal becomes false")
        void loading_becomes_false() {
            assertThat(signal.loading().peek()).isFalse();
        }

        @Test
        @DisplayName("failed signal becomes true")
        void failed_becomes_true() {
            assertThat(signal.failed().peek()).isTrue();
        }

        @Test
        @DisplayName("finished signal remains null")
        void finished_remains_null() {
            assertThat(signal.finished().peek()).isNull();
        }

        @Test
        @DisplayName("error signal contains the exception")
        void error_contains_exception() {
            assertThat(signal.error().peek()).isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("when recovering from Failed to Ready")
    class FailedToReady {

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.failed(new RuntimeException("oops")));
            signal.set(Loadable.ready("recovered"));
        }

        @Test
        @DisplayName("failed signal returns to false")
        void failed_returns_to_false() {
            assertThat(signal.failed().peek()).isFalse();
        }

        @Test
        @DisplayName("error signal is cleared")
        void error_is_cleared() {
            assertThat(signal.error().peek()).isNull();
        }

        @Test
        @DisplayName("finished signal contains the new result")
        void finished_contains_new_result() {
            assertThat(signal.finished().peek()).isEqualTo("recovered");
        }

        @Test
        @DisplayName("loading signal is false")
        void loading_is_false() {
            assertThat(signal.loading().peek()).isFalse();
        }
    }

    @Nested
    @DisplayName("when transitioning from Ready to Loading (reload)")
    class ReadyToLoading {

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.ready("old"));
            signal.set(Loadable.loading());
        }

        @Test
        @DisplayName("loading signal becomes true")
        void loading_becomes_true() {
            assertThat(signal.loading().peek()).isTrue();
        }

        @Test
        @DisplayName("finished signal is cleared")
        void finished_is_cleared() {
            assertThat(signal.finished().peek()).isNull();
        }

        @Test
        @DisplayName("failed signal remains false")
        void failed_remains_false() {
            assertThat(signal.failed().peek()).isFalse();
        }

        @Test
        @DisplayName("error signal remains null")
        void error_remains_null() {
            assertThat(signal.error().peek()).isNull();
        }
    }

    @Nested
    @DisplayName("when transitioning from Ready to Failed")
    class ReadyToFailed {

        private final Exception cause = new RuntimeException("crash");

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.ready("was fine"));
            signal.set(Loadable.failed(cause));
        }

        @Test
        @DisplayName("finished signal is cleared")
        void finished_is_cleared() {
            assertThat(signal.finished().peek()).isNull();
        }

        @Test
        @DisplayName("failed signal becomes true")
        void failed_becomes_true() {
            assertThat(signal.failed().peek()).isTrue();
        }

        @Test
        @DisplayName("error signal contains the exception")
        void error_contains_exception() {
            assertThat(signal.error().peek()).isSameAs(cause);
        }

        @Test
        @DisplayName("loading signal is false")
        void loading_is_false() {
            assertThat(signal.loading().peek()).isFalse();
        }
    }

    @Nested
    @DisplayName("when replacing the result with a new Ready value")
    class ReadyToReady {

        @BeforeEach
        void setUp() {
            signal = new LoadableSignal<>(Loadable.ready("first"));
            signal.set(Loadable.ready("second"));
        }

        @Test
        @DisplayName("finished signal contains the new result")
        void finished_contains_new_result() {
            assertThat(signal.finished().peek()).isEqualTo("second");
        }

        @Test
        @DisplayName("loading signal remains false")
        void loading_remains_false() {
            assertThat(signal.loading().peek()).isFalse();
        }

        @Test
        @DisplayName("failed signal remains false")
        void failed_remains_false() {
            assertThat(signal.failed().peek()).isFalse();
        }

        @Test
        @DisplayName("error signal remains null")
        void error_remains_null() {
            assertThat(signal.error().peek()).isNull();
        }
    }
}
