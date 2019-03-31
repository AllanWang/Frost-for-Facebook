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
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.allanwang.kau.utils.ContextHelper
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.fadeIn
import ca.allanwang.kau.utils.fadeOut
import ca.allanwang.kau.utils.invisibleIf
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.WEB_LOAD_DELAY
import com.pitchedapps.frost.kotlin.subscribeDuringJob
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel

class FrostContentWeb @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrostContentView<FrostWebView>(context, attrs, defStyleAttr, defStyleRes) {

    override val layoutRes: Int = R.layout.view_content_base_web
}

class FrostContentRecycler @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrostContentView<FrostRecyclerView>(context, attrs, defStyleAttr, defStyleRes) {

    override val layoutRes: Int = R.layout.view_content_base_recycler
}

@UseExperimental(ExperimentalCoroutinesApi::class)
abstract class FrostContentView<out T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
    FrostContentParent where T : View, T : FrostContentCore {

    private val refresh: SwipeRefreshLayout by bindView(R.id.content_refresh)
    private val progress: ProgressBar by bindView(R.id.content_progress)
    val coreView: T by bindView(R.id.content_core)

    override val core: FrostContentCore
        get() = coreView

    /**
     * While this can be conflated, there exist situations where we wish to watch refresh cycles.
     * Here, we'd need to make sure we don't skip events
     */
    override val refreshChannel: BroadcastChannel<Boolean> = BroadcastChannel(10)
    override val progressChannel: BroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val titleChannel: BroadcastChannel<String> = ConflatedBroadcastChannel()

    override lateinit var scope: CoroutineScope

    override lateinit var baseUrl: String
    override var baseEnum: FbItem? = null

    protected abstract val layoutRes: Int

    override var swipeEnabled = true
        set(value) {
            if (field == value)
                return
            field = value
            refresh.post { refresh.isEnabled = value }
        }

    /**
     * Sets up everything
     * Called by [bind]
     */
    protected fun init() {
        inflate(context, layoutRes, this)
        coreView.parent = this
        reloadThemeSelf()
    }

    override fun bind(container: FrostContentContainer) {
        baseUrl = container.baseUrl
        baseEnum = container.baseEnum
        init()
        scope = container
        core.bind(container)
        refresh.setOnRefreshListener {
            with(coreView) {
                reload(true)
            }
        }

        refreshChannel.subscribeDuringJob(scope, ContextHelper.coroutineContext) { r ->
            refresh.isRefreshing = r
            refresh.isEnabled = true
        }

        progressChannel.subscribeDuringJob(scope, ContextHelper.coroutineContext) { p ->
            progress.invisibleIf(p == 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                progress.setProgress(p, true)
            else
                progress.progress = p
        }
    }

    override fun reloadTheme() {
        reloadThemeSelf()
        coreView.reloadTheme()
    }

    override fun reloadTextSize() {
        coreView.reloadTextSize()
    }

    override fun reloadThemeSelf() {
        progress.tint(Prefs.textColor.withAlpha(180))
        refresh.setColorSchemeColors(Prefs.iconColor)
        refresh.setProgressBackgroundColorSchemeColor(Prefs.headerColor.withAlpha(255))
    }

    override fun reloadTextSizeSelf() {
        // intentionally blank
    }

    override fun destroy() {
        core.destroy()
    }

    private var transitionStart: Long = -1
    private var refreshReceiver: ReceiveChannel<Boolean>? = null

    /**
     * Hook onto the refresh observable for one cycle
     * Animate toggles between the fancy ripple and the basic fade
     * The cycle only starts on the first load since there may have been another process when this is registered
     */
    override fun registerTransition(urlChanged: Boolean, animate: Boolean): Boolean {
        if (!urlChanged && refreshReceiver != null) {
            L.v { "Consuming url load" }
            return false // still in progress; do not bother with load
        }
        L.v { "Registered transition" }
        with(coreView) {
            refreshReceiver = refreshChannel.openSubscription().also { receiver ->
                scope.launchMain {
                    var loading = false
                    for (r in receiver) {
                        if (r) {
                            loading = true
                            transitionStart = System.currentTimeMillis()
                            clearAnimation()
                            if (isVisible)
                                fadeOut(duration = 200L)
                        } else if (loading) {
                            if (animate && Prefs.animate) circularReveal(offset = WEB_LOAD_DELAY)
                            else fadeIn(duration = 200L, offset = WEB_LOAD_DELAY)
                            L.v { "Transition loaded in ${System.currentTimeMillis() - transitionStart} ms" }
                            receiver.cancel()
                            refreshReceiver = null
                        }
                    }
                }
            }
        }
        return true
    }
}
