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
package com.pitchedapps.frost.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.fadeOut
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.fragments.RecyclerContentContract
import com.pitchedapps.frost.utils.Prefs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

/**
 * Created by Allan Wang on 2017-05-29.
 *
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
class FrostRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr),
    FrostContentCore {

    override fun reload(animate: Boolean) = reloadBase(animate)

    override lateinit var parent: FrostContentParent

    override val currentUrl: String
        get() = parent.baseUrl

    lateinit var recyclerContract: RecyclerContentContract

    init {
        layoutManager = LinearLayoutManager(context)
    }

    override fun bind(container: FrostContentContainer): View {
        if (container !is RecyclerContentContract)
            throw IllegalStateException("FrostRecyclerView must bind to a container that is a RecyclerContentContract")
        this.recyclerContract = container
        container.bind(this)
        return this
    }

    init {
        isNestedScrollingEnabled = true
    }

    var onReloadClear: () -> Unit = {}

    override fun reloadBase(animate: Boolean) {
        if (Prefs.animate) fadeOut(onFinish = onReloadClear)
        scope.launch {
            parent.refreshChannel.offer(true)
            recyclerContract.reload { parent.progressChannel.offer(it) }
            parent.progressChannel.offer(100)
            parent.refreshChannel.offer(false)
            if (Prefs.animate) circularReveal()
        }
    }

    override fun clearHistory() {
        // intentionally blank
    }

    override fun destroy() {
        // todo see if any
    }

    override fun onBackPressed() = false

    /**
     * If recycler is already at the top, refresh
     * Otherwise scroll to top
     */
    override fun onTabClicked() {
        val firstPosition =
            (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (firstPosition == 0) reloadBase(true)
        else scrollToTop()
    }

    private fun scrollToTop() {
        stopScroll()
        smoothScrollToPosition(0)
    }

    // nothing running in background; no need to listen
    override var active: Boolean = true

    override fun reloadTheme() {
        reloadThemeSelf()
    }

    override fun reloadThemeSelf() {
        reload(false) // todo see if there's a better solution
    }

    override fun reloadTextSize() {
        reloadTextSizeSelf()
    }

    override fun reloadTextSizeSelf() {
        // todo
    }
}
