package com.pitchedapps.frost.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.pitchedapps.frost.contracts.ActivityContract
import com.pitchedapps.frost.contracts.FrostViewContract
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.parsers.FrostParser
import com.pitchedapps.frost.views.FrostWebView
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-11-07.
 */

interface FragmentContract {

    /**
     * Specifies base url
     */
    val url: String

    /**
     * Specifies base url enum
     */
    val urlEnum: FbItem

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
    fun attachMainObservable(contract: ActivityContract): Disposable

    /**
     * Load custom layout to container
     */
    fun innerView(context: Context) : FrostViewContract

    /**
     * Call when fragment is detached so that any existing
     * observable is disposed
     */
    fun detachMainObservable()

    fun onBackPressed(): Boolean

    fun onTabClick()


}

interface NativeFragmentContract<T> : FragmentContract {
    /**
     * The parser to make this all happen
     */
    val parser: FrostParser<T>

    /**
     * Called when something goes wrong in the parser
     */
    fun revertToWeb()
}

interface WebFragmentContract : FragmentContract {
    var web: FrostWebView
    /**
     * Call refresh with animations and also clear history
     */
    fun reloadAndClear(animate: Boolean)

    /**
     * Boolean to help define when to stop loading new data
     * Typically this should be turned true when the fragment is out of view
     * and false once it is back into view
     */
    var pauseLoad: Boolean
}