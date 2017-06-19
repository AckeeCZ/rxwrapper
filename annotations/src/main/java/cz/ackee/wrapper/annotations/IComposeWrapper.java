package cz.ackee.wrapper.annotations;

import io.reactivex.CompletableTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;

/**
 * Wrapper that is composed with every method call in {@link WrappedService} annotated classes. For each variant - Observable/Single/Completable is separate method
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public interface IComposeWrapper {
    <T> ObservableTransformer<T, T> wrapObservable();

    <T> SingleTransformer<T, T> wrapSingle();

    CompletableTransformer wrapCompletable();
}
