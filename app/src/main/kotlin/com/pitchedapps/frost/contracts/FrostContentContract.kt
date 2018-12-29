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

import android.view.View
import com.pitchedapps.frost.facebook.FbItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Allan Wang on 20/12/17.
 */

/**
 * Contract for the underlying parent,
 * binds to activities & fragments
 */
interface FrostContentContainer : CoroutineScope {

    val baseUrl: String

    val baseEnum: FbItem?

    /**
     * Update toolbar title
     */
    fun setTitle(title: String)
}

/**
 * Contract for components shared among
 * all content providers
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
interface FrostContentParent : DynamicUiContract {

    val scope: CoroutineScope

    val core: FrostContentCore

    /**
     * Observable to get data on whether view is refreshing or not
     */
    val refreshChannel: BroadcastChannel<Boolean>

    /**
     * Observable to get data on refresh progress, with range [0, 100]
     */
    val progressChannel: BroadcastChannel<Int>

    /**
     * Observable to get new title data (unique values only)
     */
    // todo note that this should be like a behavior subject vs publish subject
    val titleChannel: BroadcastChannel<String>

    var baseUrl: String

    var baseEnum: FbItem?

    /**
     * Toggle state for allowing swipes
     * Allowed on any thread
     */
    var swipeEnabled: Boolean

    /**
     * Binds the container to self
     * this will also handle all future bindings
     * Must be called by container!
     */
    fun bind(container: FrostContentContainer)

    /**
     * Signal that the contract will not be used again
     * Clean up resources where applicable
     */
    fun destroy()

    /**
     * Hook onto the refresh observable for one cycle
     * Animate toggles between the fancy ripple and the basic fade
     * The cycle only starts on the first load since
     * there may have been another process when this is registered
     *
     * Returns true to proceed with load
     * In some cases when the url has not changed,
     * it may not be advisable to proceed with the load
     * For those cases, we will return false to stop it
     */
    fun registerTransition(urlChanged: Boolean, animate: Boolean): Boolean
}

/**
 * Underlying contract for the content itself
 */
interface FrostContentCore : DynamicUiContract {

    val scope: CoroutineScope
        get() = parent.scope

    /**
     * Reference to parent
     * Bound through calling [FrostContentParent.bind]
     */
    var parent: FrostContentParent

    /**
     * Initializes view through given [container]
     *
     * The content may be free to extract other data from
     * the container if necessary
     *
     * [parent] must be bounded before calling this!
     */
    fun bind(container: FrostContentContainer): View

    /**
     * Call to reload wrapped data
     */
    fun reload(animate: Boolean)

    /**
     * Call to reload base data
     */
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

    val currentUrl: String

    /**
     * Condition to help pause certain background resources
     */
    var active: Boolean

    /**
     * Triggered when view is within viewpager
     * and tab is clicked
     */
    fun onTabClicked()

    /**
     * Signal destruction to release some content manually
     */
    fun destroy()
}
