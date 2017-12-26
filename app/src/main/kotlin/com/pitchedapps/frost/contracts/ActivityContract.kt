package com.pitchedapps.frost.contracts

import com.pitchedapps.frost.dbflow.CookieModel
import io.reactivex.subjects.PublishSubject

/**
 * All the contracts for [MainActivity]
 */
interface ActivityContract : FileChooserActivityContract

interface MainActivityContract : ActivityContract {
    val fragmentSubject: PublishSubject<Int>
    fun setTitle(res: Int)
    fun setTitle(text: CharSequence)
    fun collapseAppBar()
}