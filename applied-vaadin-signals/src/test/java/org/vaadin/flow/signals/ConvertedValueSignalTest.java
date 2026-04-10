package org.vaadin.flow.signals;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConvertedValueSignal")
public class ConvertedValueSignalTest {

    public record Email(String value) {

        private static final Pattern EMAIL_PATTERN =
                Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

        public Email {
            if (value == null || !EMAIL_PATTERN.matcher(value).matches()) {
                throw new IllegalArgumentException("Invalid email: " + value);
            }
        }
    }

    public static class EmailConverter implements Converter<String, Email> {

        @Override
        public Result<Email> convertToModel(@Nullable String value, ValueContext context) {
            try {
                return value == null || value.isBlank() ? Result.ok(null) : Result.ok(new Email(value));
            } catch (IllegalArgumentException e) {
                return Result.error(e.getMessage());
            }
        }

        @Override
        public String convertToPresentation(@Nullable Email value, ValueContext context) {
            return value == null ? "" : value.value();
        }
    }

    private ValueSignal<Email> modelSignal;
    private ConvertedValueSignal<Email, String> convertedSignal;

    @BeforeEach
    void setUp() {
        modelSignal = new ValueSignal<>(null);
        convertedSignal = new ConvertedValueSignal<>(modelSignal, modelSignal::set, new EmailConverter());
    }

    @Nested
    @DisplayName("before any presentation value is set")
    class InitialState {

        @Test
        @DisplayName("model signal contains the initial value")
        void model_signal_contains_initial_value() {
            assertThat(convertedSignal.model().peek()).isNull();
        }

        @Test
        @DisplayName("presentation signal returns the converter's representation of the initial model")
        void presentation_signal_returns_converted_initial_model() {
            assertThat(convertedSignal.presentation().peek()).isEmpty();
        }

        @Test
        @DisplayName("invalid signal is false")
        void invalid_signal_is_false() {
            assertThat(convertedSignal.invalid().peek()).isFalse();
        }

        @Test
        @DisplayName("error message signal is null")
        void error_message_signal_is_null() {
            assertThat(convertedSignal.errorMessage().peek()).isNull();
        }
    }

    @Nested
    @DisplayName("when a valid presentation value is set")
    class ValidConversion {

        @BeforeEach
        void setValidPresentation() {
            convertedSignal.setPresentation("foo@bar.com");
        }

        @Test
        @DisplayName("model is updated with the converted value")
        void model_is_updated_with_converted_value() {
            assertThat(convertedSignal.model().peek()).isEqualTo(new Email("foo@bar.com"));
        }

        @Test
        @DisplayName("presentation signal returns the same value")
        void presentation_signal_returns_the_set_value() {
            assertThat(convertedSignal.presentation().peek()).isEqualTo("foo@bar.com");
        }

        @Test
        @DisplayName("invalid signal is false")
        void invalid_signal_is_false() {
            assertThat(convertedSignal.invalid().peek()).isFalse();
        }

        @Test
        @DisplayName("error message signal is null")
        void error_message_signal_is_null() {
            assertThat(convertedSignal.errorMessage().peek()).isNull();
        }

        @Test
        @DisplayName("a second valid value updates the model again")
        void second_valid_value_updates_model() {
            convertedSignal.setPresentation("other@example.org");
            assertThat(convertedSignal.model().peek()).isEqualTo(new Email("other@example.org"));
            assertThat(convertedSignal.presentation().peek()).isEqualTo("other@example.org");
        }
    }

    @Nested
    @DisplayName("when an invalid presentation value is set")
    class InvalidConversion {

        @BeforeEach
        void setInvalidPresentation() {
            convertedSignal.setPresentation("not-an-email");
        }

        @Test
        @DisplayName("model signal is not changed")
        void model_signal_is_not_changed() {
            assertThat(convertedSignal.model().peek()).isNull();
        }

        @Test
        @DisplayName("presentation signal returns the raw invalid input")
        void presentation_signal_returns_raw_invalid_input() {
            assertThat(convertedSignal.presentation().peek()).isEqualTo("not-an-email");
        }

        @Test
        @DisplayName("invalid signal is true")
        void invalid_signal_is_true() {
            assertThat(convertedSignal.invalid().peek()).isTrue();
        }

        @Test
        @DisplayName("error message signal contains the conversion error")
        void error_message_contains_conversion_error() {
            assertThat(convertedSignal.errorMessage().peek()).isEqualTo("Invalid email: not-an-email");
        }

        @Test
        @DisplayName("a second invalid value updates the error message")
        void second_invalid_value_updates_error_message() {
            convertedSignal.setPresentation("also@@bad");
            assertThat(convertedSignal.errorMessage().peek()).isEqualTo("Invalid email: also@@bad");
            assertThat(convertedSignal.presentation().peek()).isEqualTo("also@@bad");
        }
    }

    @Nested
    @DisplayName("when recovering from an invalid state by setting a valid value")
    class RecoveryFromInvalid {

        @BeforeEach
        void transitionFromInvalidToValid() {
            convertedSignal.setPresentation("bad-input");
            convertedSignal.setPresentation("recovered@example.com");
        }

        @Test
        @DisplayName("model is updated with the valid value")
        void model_is_updated() {
            assertThat(convertedSignal.model().peek()).isEqualTo(new Email("recovered@example.com"));
        }

        @Test
        @DisplayName("invalid signal returns to false")
        void invalid_returns_to_false() {
            assertThat(convertedSignal.invalid().peek()).isFalse();
        }

        @Test
        @DisplayName("error message is cleared")
        void error_message_is_cleared() {
            assertThat(convertedSignal.errorMessage().peek()).isNull();
        }

        @Test
        @DisplayName("presentation signal returns the valid value")
        void presentation_returns_valid_value() {
            assertThat(convertedSignal.presentation().peek()).isEqualTo("recovered@example.com");
        }
    }

    @Nested
    @DisplayName("when transitioning from a valid state to an invalid state")
    class TransitionToInvalid {

        @BeforeEach
        void transitionFromValidToInvalid() {
            convertedSignal.setPresentation("good@example.com");
            convertedSignal.setPresentation("now-broken");
        }

        @Test
        @DisplayName("model retains the last successfully converted value")
        void model_retains_last_valid_value() {
            assertThat(convertedSignal.model().peek()).isEqualTo(new Email("good@example.com"));
        }

        @Test
        @DisplayName("invalid signal becomes true")
        void invalid_becomes_true() {
            assertThat(convertedSignal.invalid().peek()).isTrue();
        }

        @Test
        @DisplayName("error message is set")
        void error_message_is_set() {
            assertThat(convertedSignal.errorMessage().peek()).isNotNull();
        }

        @Test
        @DisplayName("presentation signal returns the raw invalid input, not the model-derived value")
        void presentation_returns_raw_input() {
            assertThat(convertedSignal.presentation().peek()).isEqualTo("now-broken");
        }
    }

    @Nested
    @DisplayName("when the model signal is changed externally")
    class ExternalModelChange {

        @Test
        @DisplayName("presentation signal reflects the new model value")
        void presentation_reflects_external_model_change() {
            modelSignal.set(new Email("external@example.com"));
            assertThat(convertedSignal.presentation().peek()).isEqualTo("external@example.com");
        }

        @Test
        @DisplayName("invalid and error message remain unaffected")
        void validation_state_unchanged() {
            modelSignal.set(new Email("external@example.com"));
            assertThat(convertedSignal.invalid().peek()).isFalse();
            assertThat(convertedSignal.errorMessage().peek()).isNull();
        }
    }

    @Nested
    @DisplayName("when a blank presentation value is set")
    class BlankPresentation {

        @BeforeEach
        void setBlankPresentation() {
            convertedSignal.setPresentation("valid@example.com");
            convertedSignal.setPresentation("");
        }

        @Test
        @DisplayName("model is set to null")
        void model_is_null() {
            assertThat(convertedSignal.model().peek()).isNull();
        }

        @Test
        @DisplayName("conversion is considered valid")
        void conversion_is_valid() {
            assertThat(convertedSignal.invalid().peek()).isFalse();
            assertThat(convertedSignal.errorMessage().peek()).isNull();
        }

        @Test
        @DisplayName("presentation signal returns the converter's representation of null")
        void presentation_returns_empty_string() {
            assertThat(convertedSignal.presentation().peek()).isEmpty();
        }
    }
}
