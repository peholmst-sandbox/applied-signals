package org.vaadin.flow.signals;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void initial_state_is_empty() {
        assertThat(convertedSignal.errorMessage().peek()).isNull();
        assertThat(convertedSignal.invalid().peek()).isFalse();
        assertThat(convertedSignal.model().peek()).isNull();
        assertThat(convertedSignal.presentation().peek()).isEmpty();
    }

    @Test
    void valid_presentation_converts_to_valid_model() {
        convertedSignal.setPresentation("foo@bar.com");
        assertThat(convertedSignal.errorMessage().peek()).isNull();
        assertThat(convertedSignal.invalid().peek()).isFalse();
        assertThat(convertedSignal.model().peek()).isEqualTo(new Email("foo@bar.com"));
        assertThat(convertedSignal.presentation().peek()).isEqualTo("foo@bar.com");
    }

    @Test
    void invalid_presentation_sets_the_error_message_and_leaves_model_signal_unchanged() {
        convertedSignal.setPresentation("invalid@@");
        assertThat(convertedSignal.errorMessage().peek()).isEqualTo("Invalid email: invalid@@");
        assertThat(convertedSignal.invalid().peek()).isTrue();
        assertThat(convertedSignal.model().peek()).isNull();
        assertThat(convertedSignal.presentation().peek()).isEqualTo("invalid@@");
    }

    @Test
    void setting_model_signal_updates_presentation_signal() {
        modelSignal.set(new Email("foo@bar.com"));
        assertThat(convertedSignal.errorMessage().peek()).isNull();
        assertThat(convertedSignal.invalid().peek()).isFalse();
        assertThat(convertedSignal.presentation().peek()).isEqualTo("foo@bar.com");
    }
}
