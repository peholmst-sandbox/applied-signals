package org.vaadin.flow.signals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Loadable.load")
public class LoadableLoadTest {

    private static final Duration LOADING_DELAY = Duration.ofMillis(50);
    private static final Duration SLOW_OPERATION = Duration.ofMillis(200);
    /** Time to wait after load() returns so the timer thread can settle. */
    private static final Duration SETTLE_TIME = Duration.ofMillis(150);

    @Nested
    @DisplayName("when the supplier completes before the loading delay")
    class FastSuccess {

        private final List<Loadable<String>> states = new CopyOnWriteArrayList<>();

        @BeforeEach
        void setUp() throws InterruptedException {
            Loadable.load(() -> "fast", states::add, LOADING_DELAY);
            Thread.sleep(SETTLE_TIME.toMillis());
        }

        @Test
        @DisplayName("emits only Ready")
        void emits_only_ready() {
            assertThat(states).containsExactly(Loadable.ready("fast"));
        }

        @Test
        @DisplayName("Loading is never emitted")
        void loading_is_never_emitted() {
            assertThat(states).noneMatch(s -> s instanceof Loadable.Loading);
        }
    }

    @Nested
    @DisplayName("when the supplier takes longer than the loading delay")
    class SlowSuccess {

        private final List<Loadable<String>> states = new CopyOnWriteArrayList<>();

        @BeforeEach
        void setUp() {
            Loadable.load(() -> {
                try {
                    Thread.sleep(SLOW_OPERATION.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return "slow";
            }, states::add, LOADING_DELAY);
        }

        @Test
        @DisplayName("emits Loading followed by Ready")
        void emits_loading_then_ready() {
            assertThat(states).containsExactly(Loadable.loading(), Loadable.ready("slow"));
        }

        @Test
        @DisplayName("Loading is emitted before Ready")
        void loading_precedes_ready() {
            assertThat(states.getFirst()).isInstanceOf(Loadable.Loading.class);
            assertThat(states.getLast()).isInstanceOf(Loadable.Ready.class);
        }
    }

    @Nested
    @DisplayName("when the supplier fails before the loading delay")
    class FastFailure {

        private final RuntimeException cause = new RuntimeException("boom");
        private final List<Loadable<String>> states = new CopyOnWriteArrayList<>();

        @BeforeEach
        void setUp() throws InterruptedException {
            Loadable.<String>load(() -> { throw cause; }, states::add, LOADING_DELAY);
            Thread.sleep(SETTLE_TIME.toMillis());
        }

        @Test
        @DisplayName("emits only Failed")
        void emits_only_failed() {
            assertThat(states).containsExactly(Loadable.failed(cause));
        }

        @Test
        @DisplayName("Loading is never emitted")
        void loading_is_never_emitted() {
            assertThat(states).noneMatch(s -> s instanceof Loadable.Loading);
        }

        @Test
        @DisplayName("Failed contains the thrown exception")
        void failed_contains_the_thrown_exception() {
            assertThat(states.getFirst())
                    .isInstanceOf(Loadable.Failed.class)
                    .extracting(s -> ((Loadable.Failed<String>) s).error())
                    .isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("when the supplier fails after the loading delay")
    class SlowFailure {

        private final RuntimeException cause = new RuntimeException("timeout");
        private final List<Loadable<String>> states = new CopyOnWriteArrayList<>();

        @BeforeEach
        void setUp() {
            Loadable.<String>load(() -> {
                try {
                    Thread.sleep(SLOW_OPERATION.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                throw cause;
            }, states::add, LOADING_DELAY);
        }

        @Test
        @DisplayName("emits Loading followed by Failed")
        void emits_loading_then_failed() {
            assertThat(states).containsExactly(Loadable.loading(), Loadable.failed(cause));
        }

        @Test
        @DisplayName("Failed contains the thrown exception")
        void failed_contains_the_thrown_exception() {
            assertThat(states.getLast())
                    .isInstanceOf(Loadable.Failed.class)
                    .extracting(s -> ((Loadable.Failed<String>) s).error())
                    .isSameAs(cause);
        }
    }

    @Nested
    @DisplayName("Loading singleton")
    class LoadingSingleton {

        @Test
        @DisplayName("loading() returns the same instance regardless of type parameter")
        void loading_returns_cached_instance() {
            Loadable.Loading<String> a = Loadable.loading();
            Loadable.Loading<Integer> b = Loadable.loading();
            assertThat(a).isSameAs(b);
        }

        @Test
        @DisplayName("Loading emitted by load() is the cached singleton")
        void load_emits_cached_singleton() {
            List<Loadable<String>> states = new CopyOnWriteArrayList<>();
            Loadable.load(() -> {
                try {
                    Thread.sleep(SLOW_OPERATION.toMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                return "value";
            }, states::add, LOADING_DELAY);

            assertThat(states.getFirst()).isSameAs(Loadable.loading());
        }
    }
}
