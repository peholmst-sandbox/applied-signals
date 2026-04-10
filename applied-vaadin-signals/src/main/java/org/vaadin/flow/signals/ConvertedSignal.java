package org.vaadin.flow.signals;

import com.vaadin.flow.signals.Signal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 *
 * @param <M>
 * @param <P>
 */
@NullMarked
public interface ConvertedSignal<M extends @Nullable Object, P extends @Nullable Object> extends Serializable {

    /**
     *
     * @return
     */
    Signal<Boolean> invalid();

    /**
     *
     * @return
     */
    Signal<@Nullable String> errorMessage();

    /**
     *
     * @return
     */
    Signal<M> model();

    /**
     *
     * @return
     */
    Signal<P> presentation();
}
