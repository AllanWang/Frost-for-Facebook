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
package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.contracts.MainFabContract
import com.pitchedapps.frost.views.FrostRecyclerView
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Allan Wang on 2017-11-07.
 */

interface FragmentContract : FrostContentContainer {

    val content: FrostContentParent?

    /**
     * Defines whether the fragment is valid in the viewpager
     * or if it needs to be recreated
     * May be called from any thread to toggle status.
     * Note that calls beyond the fragment lifecycle will be ignored
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

    fun updateFab(contract: MainFabContract)

    /**
     * Single callable action to be executed upon creation
     * Note that this call is not guaranteed
     */
    fun post(action: (fragment: FragmentContract) -> Unit)

    /**
     * Call whenever a fragment is attached so that it may listen
     * to activity emissions.
     * Returns a means of closing the listener, which can be called from [detachMainObservable]
     */
    fun attachMainObservable(contract: MainActivityContract): ReceiveChannel<Int>

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
     * Completely handle data reloading, within a non-ui thread
     * The progress function allows optional emission of progress values (between 0 and 100)
     * and can be called from any thread.
     * Returns [true] for success, [false] otherwise
     */
    suspend fun reload(progress: (Int) -> Unit): Boolean
}
