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
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.web.FrostEmitter
import com.pitchedapps.frost.web.asFrostEmitter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.transformWhile
import javax.inject.Inject

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

abstract class FrostContentView<out T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrostContentViewBase(context, attrs, defStyleAttr, defStyleRes),
    FrostContentParent where T : View, T : FrostContentCore {

    val coreView: T by bindView(R.id.content_core)

    override val core: FrostContentCore
        get() = coreView
}

/**
 * Subsection of [FrostContentView] that is [AndroidEntryPoint] friendly (no generics)
 */
@UseExperimental(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
abstract class FrostContentViewBase(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
    FrostContentParent {

    // No JvmOverloads due to hilt
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    @Inject
    lateinit var prefs: Prefs

    @Inject
    lateinit var themeProvider: ThemeProvider

    private val refresh: SwipeRefreshLayout by bindView(R.id.content_refresh)
    private val progress: ProgressBar by bindView(R.id.content_progress)

    private val coreView: View by bindView(R.id.content_core)

    /**
     * While this can be conflated, there exist situations where we wish to watch refresh cycles.
     * Here, we'd need to make sure we don't skip events
     */
    private val refreshMutableFlow = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val refreshFlow: SharedFlow<Boolean> = refreshMutableFlow.asSharedFlow()

    override val refreshEmit: FrostEmitter<Boolean> = refreshMutableFlow.asFrostEmitter()

    private val progressMutableFlow = MutableStateFlow(0)

    override val progressFlow: SharedFlow<Int> = progressMutableFlow.asSharedFlow()

    override val progressEmit: FrostEmitter<Int> = progressMutableFlow.asFrostEmitter()

    private val titleMutableFlow = MutableStateFlow("")

    override val titleFlow: SharedFlow<String> = titleMutableFlow.asSharedFlow()

    override val titleEmit: FrostEmitter<String> = titleMutableFlow.asFrostEmitter()

    override lateinit var scope: CoroutineScope

    override lateinit var baseUrl: String
    override var baseEnum: FbItem? = null

    protected abstract val layoutRes: Int

    @Volatile
    override var swipeDisabledByAction = false
        set(value) {
            field = value
            updateSwipeEnabler()
        }

    @Volatile
    override var swipeAllowedByPage: Boolean = true
        set(value) {
            field = value
            updateSwipeEnabler()
        }

    private fun updateSwipeEnabler() {
        val swipeEnabled = swipeAllowedByPage && !swipeDisabledByAction
        if (refresh.isEnabled == swipeEnabled) return
        refresh.post { refresh.isEnabled = swipeEnabled }
    }

    /**
     * Sets up everything
     * Called by [bind]
     */
    protected fun init() {
        inflate(context, layoutRes, this)
        reloadThemeSelf()
    }

    override fun bind(container: FrostContentContainer) {
        baseUrl = container.baseUrl
        baseEnum = container.baseEnum
        init()
        scope = container
        core.bind(this, container)
        refresh.setOnRefreshListener {
            core.reload(true)
        }

        refreshFlow.distinctUntilChanged().onEach { r ->
            L.v { "Refreshing $r" }
            refresh.isRefreshing = r
        }.launchIn(scope)

        progressFlow.onEach { p ->
            progress.invisibleIf(p == 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                progress.setProgress(p, true)
            else
                progress.progress = p
        }.launchIn(scope)
    }

    override fun reloadTheme() {
        reloadThemeSelf()
        core.reloadTheme()
    }

    override fun reloadTextSize() {
        core.reloadTextSize()
    }

    override fun reloadThemeSelf() {
        progress.tint(themeProvider.textColor.withAlpha(180))
        refresh.setColorSchemeColors(themeProvider.iconColor)
        refresh.setProgressBackgroundColorSchemeColor(themeProvider.headerColor.withAlpha(255))
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
        if (!urlChanged && transitionStart != -1L) {
            L.v { "Consuming url load" }
            return false // still in progress; do not bother with load
        }
        coreView.transition(animate)
        return true
    }

    private fun View.transition(animate: Boolean) {
        L.v { "Registered transition" }
        transitionStart = 0L // Marker for pending transition
        scope.launchMain {
            refreshFlow.distinctUntilChanged()
                // Pseudo windowed mode
                .runningFold(false to false) { (_, prev), curr -> prev to curr }
                // Take until prev was loading and current is not loading
                // Unlike takeWhile, we include the last state (first non matching)
                .transformWhile { emit(it); it != (true to false) }
                .onEach { (prev, curr) ->
                    if (curr) {
                        transitionStart = System.currentTimeMillis()
                        clearAnimation()
                        if (isVisible)
                            fadeOut(duration = 200L)
                    } else if (prev) { // prev && !curr
                        if (animate && prefs.animate) circularReveal(offset = WEB_LOAD_DELAY)
                        else fadeIn(duration = 200L, offset = WEB_LOAD_DELAY)
                        L.v { "Transition loaded in ${System.currentTimeMillis() - transitionStart} ms" }
                    }
                }.collect()
            transitionStart = -1L
        }
    }
}
