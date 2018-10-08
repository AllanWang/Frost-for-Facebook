package com.pitchedapps.frost.contracts

import com.mikepenz.iconics.typeface.IIcon
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.fragments.BaseFragment
import io.reactivex.subjects.PublishSubject

/**
 * All the contracts for [MainActivity]
 */
interface ActivityContract : FileChooserActivityContract

interface MainActivityContract : ActivityContract, MainFabContract {
    val fragmentSubject: PublishSubject<Int>
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