package com.pitchedapps.frost.contracts

import android.view.View
import android.view.ViewGroup
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.views.FrostRefreshView

interface FrostViewContract : FrostObservables, DynamicUiContract, FrostUrlData {

    /**
     * Attaches view to given [container]
     * This must be called!
     */
    fun bind(container: FrostRefreshView) {
        if (container.refresh.childCount > 0) {
            L.d("Clean up refresh view for ${javaClass.simpleName}")
            container.refresh.removeAllViews()
        }
        container.inner = this
        container.passObservablesTo(this)
        container.passUrlDataTo(this)
        container.refresh.addView(view)
        init(container)
    }

    /**
     * The underlying view
     */
    val view: View

    /**
     * Call to reload wrapped data
     */
    fun reload(animate: Boolean)

    fun reloadBase(animate: Boolean)

    /**
     * If possible, remove anything in the view stack
     * Applies namely to webviews
     */
    fun clearHistory()

    /**
     * Should be called when a back press is triggered
     * Return [true] if consumed, [false] otherwise
     */
    fun onBackPressed(): Boolean

    /**
     * Triggered when viewpager scrolls to current fragment
     */
    fun onScrollTo()

    /**
     * Triggered when viewpager scrolls from current fragment
     */
    fun onScrollFrom()

    fun onPause()

    fun onResume()

    /**
     * Triggered when view is within viewpager
     * and tab is clicked
     */
    fun scrollOrRefresh()

    /**
     * Signal destruction to release some content manually
     */
    fun destroy()

    fun init(dataContract: FrostUrlData)

}

/**
 * Functions that will modify the current ui
 */
interface DynamicUiContract {

    /**
     * Change all necessary view components to the new theme
     * and call whatever other children that also implement [FrostThemable]
     */
    fun reloadTheme()

    fun reloadTextSize()

}