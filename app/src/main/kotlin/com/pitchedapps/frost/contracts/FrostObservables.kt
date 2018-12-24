package com.pitchedapps.frost.contracts

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

/**
 * Created by Allan Wang on 2017-11-07.
 */
interface FrostObservables {
    /**
     * Observable to get data on whether view is refreshing or not
     */
    var refreshObservable: PublishSubject<Boolean>

    /**
     * Observable to get data on refresh progress, with range [0, 100]
     */
    var progressObservable: PublishSubject<Int>

    /**
     * Observable to get new title data (unique values only)
     */
    var titleObservable: BehaviorSubject<String>

    fun passObservablesTo(other: FrostObservables) {
        other.refreshObservable = refreshObservable
        other.progressObservable = progressObservable
        other.titleObservable = titleObservable
    }
}