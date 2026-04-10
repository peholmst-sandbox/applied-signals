package com.example.application.converters;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import org.vaadin.flow.signals.ConvertedValueSignal;

import java.util.Objects;

@Route("converter")
class ConverterView extends VerticalLayout {

    ConverterView() {
        var emailSignal = new ValueSignal<Email>(null);
        var convertedSignal = new ConvertedValueSignal<>(emailSignal, emailSignal::set, new EmailConverter());

        var emailField = new EmailField("E-mail");
        emailField.setValueChangeMode(ValueChangeMode.LAZY);
        emailField.bindValue(convertedSignal.presentation(), convertedSignal::setPresentation);

        Signal.effect(this, () -> {
            emailField.setInvalid(convertedSignal.invalid().get());
            emailField.setErrorMessage(convertedSignal.errorMessage().get());
        });

        var modelValueSpan = new Span();
        modelValueSpan.bindText(emailSignal.map(Objects::toString));

        var presentationValueSpan = new Span();
        presentationValueSpan.bindText(convertedSignal.presentation());

        add(emailField);
        add(modelValueSpan);
        add(presentationValueSpan);
    }
}
