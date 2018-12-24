/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
