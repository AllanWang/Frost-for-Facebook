package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.views.FrostRecyclerView
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-11-07.
 */

interface FragmentContract : FrostContentContainer {

    val content: FrostContentParent?

    /**
     * Defines whether the fragment is valid in the viewpager
     * Or if it needs to be recreated
     */
    var valid: Boolean

    /**
     * Helper to retrieve the core from [content]
     */
    val core: FrostContentCore?
        get() = content?.core

    /**
     * Specifies position in Activity's viewpager
     */
    val position: Int

    /**
     * Specifies whether if current load
     * will be fragment's first load
     *
     * Defaults to true
     */
    var firstLoad: Boolean

    /**
     * Called when the fragment is first visible
     * Typically, if [firstLoad] is true,
     * the fragment should call [reload] and make [firstLoad] false
     */
    fun firstLoadRequest()

    /**
     * Single callable action to be executed upon creation
     * Note that this call is not guaranteed
     */
    fun post(action: (fragment: FragmentContract) -> Unit)

    /**
     * Call whenever a fragment is attached so that it may listen
     * to activity emissions
     */
    fun attachMainObservable(contract: MainActivityContract): Disposable

    /**
     * Call when fragment is detached so that any existing
     * observable is disposed
     */
    fun detachMainObservable()

    /*
     * -----------------------------------------
     * Delegates
     * -----------------------------------------
     */

    fun onBackPressed(): Boolean

    fun onTabClick()


}

interface RecyclerContentContract {

    fun bind(recyclerView: FrostRecyclerView)

    /**
     * Completely handle data reloading
     * Optional progress emission update
     * Callback returns [true] for success, [false] otherwise
     */
    fun reload(progress: (Int) -> Unit, callback: (Boolean) -> Unit)

}