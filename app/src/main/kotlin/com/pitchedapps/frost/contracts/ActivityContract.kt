package com.pitchedapps.frost.contracts

import io.reactivex.subjects.PublishSubject

/**
 * All the contracts for [MainActivity]
 */
interface ActivityContract : FileChooserActivityContract {
    fun setTitle(titleRes: Int)
    fun setTitle(title: String)
    val fragmentSubject: PublishSubject<Int>
}