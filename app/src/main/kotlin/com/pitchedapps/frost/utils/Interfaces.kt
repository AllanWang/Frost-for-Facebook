package com.pitchedapps.frost.utils

import io.reactivex.subjects.Subject

/**
 * Created by Allan Wang on 2017-05-29.
 */
interface ObservableContainer<T> {
    val observable: Subject<T>
}

interface KeyPairObservable : ObservableContainer<Pair<Int, Int>>