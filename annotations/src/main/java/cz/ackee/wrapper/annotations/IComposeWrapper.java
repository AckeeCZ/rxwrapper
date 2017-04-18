package cz.ackee.wrapper.annotations;

import io.reactivex.ObservableTransformer;

/**
 * Wrapper that is composed with every method call in {@link WrappedService} annotated classes
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public interface IComposeWrapper {
    <T> ObservableTransformer<T, T> wrap();
}
