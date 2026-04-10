package com.example.application.converters;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class EmailConverter implements Converter<String, Email> {

    @Override
    public Result<@Nullable Email> convertToModel(@Nullable String value, ValueContext context) {
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