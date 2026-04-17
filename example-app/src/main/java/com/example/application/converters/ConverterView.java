package com.example.application.converters;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.vaadin.flow.signals.SignalUtil;

import java.util.Objects;

@Route("converter")
@NullMarked
class ConverterView extends VerticalLayout {

    ConverterView() {
        // TODO Is it possible to compact these into a single line using a utility method?
        var emailSignal = new ValueSignal<Result<@Nullable Email>>(Result.ok(null));
        var emailDemux = SignalUtil.demuxResult(emailSignal);
        var emailPresentation = SignalUtil.modelBacked(new EmailConverter(), emailSignal, emailSignal::set);

        var emailField = new EmailField("E-mail");
        emailField.setValueChangeMode(ValueChangeMode.LAZY);
        emailField.bindValue(emailPresentation, emailPresentation::setPresentation);
        SignalUtil.bindValidation(emailField, emailDemux);

        var modelValueSpan = new Span();
        modelValueSpan.bindText(emailDemux.valueOrElse(null).map(Objects::toString));

        var presentationValueSpan = new Span();
        presentationValueSpan.bindText(emailPresentation);

        add(emailField);
        add(modelValueSpan);
        add(presentationValueSpan);
    }
}
