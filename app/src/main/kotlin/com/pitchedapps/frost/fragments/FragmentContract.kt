package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.parsers.FrostParser
import com.pitchedapps.frost.web.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewCore
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Created by Allan Wang on 2017-11-07.
 */
object FragmentRequest {
    const val REFRESH = 99
    const val TEXT_ZOOM = 17
}

interface ActivityContract {
    fun setTitle(titleRes: Int)
    fun setTitle(title: String)
    val subject: PublishSubject<Int>
}

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
     * Reload [url] with or without animation
     */
    fun reload(animate: Boolean)

    /**
     * Should be called when a back press is triggered
     * Return [true] if consumed, [false] otherwise
     */
    fun onBackPressed():Boolean

    /**
     * Triggered when viewpager scrolls to current fragment
     */
    fun onScrollTo()

    /**
     * Triggered when viewpager scrolls from current fragment
     */
    fun onScrollFrom()

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
     * Call when fragment is detached so that any existing
     * observable is disposeed
     */
    fun detachMainObservable()

    fun reloadTextSize()

    fun reloadTheme()
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
    var frostWebView: FrostWebView
    val web: FrostWebViewCore
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