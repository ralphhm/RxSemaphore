package de.rhm.rxsemaphore

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

class RxSemaphoreTransformer : ObservableTransformer<Boolean, Boolean> {
    override fun apply(upstream: Observable<Boolean>): Observable<Boolean> = upstream
            .map { if (it) 1 else -1 }
            .scan(0) { previous, current -> previous + current }
            .skip(1)
            .map { it < 1 }
}
