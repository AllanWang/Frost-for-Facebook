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

import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.fragments.BaseFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * All the contracts for [MainActivity]
 */
interface ActivityContract : FileChooserActivityContract

@UseExperimental(ExperimentalCoroutinesApi::class)
interface MainActivityContract : ActivityContract, MainFabContract {
    val fragmentChannel: BroadcastChannel<Int>
    val headerBadgeChannel: BroadcastChannel<String>
    fun setTitle(res: Int)
    fun setTitle(text: CharSequence)
    /**
     * Available on all threads
     */
    fun collapseAppBar()

    fun reloadFragment(fragment: BaseFragment)
}

interface MainFabContract {
    fun showFab(iicon: IIcon, clickEvent: () -> Unit)
    fun hideFab()
}
