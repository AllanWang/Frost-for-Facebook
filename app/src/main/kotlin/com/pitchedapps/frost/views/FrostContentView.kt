package com.pitchedapps.frost.views

import android.content.Context
import android.os.Build
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import ca.allanwang.kau.utils.*
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.WEB_LOAD_DELAY
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FrostContentWeb @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrostContentView<FrostWebView>(context, attrs, defStyleAttr, defStyleRes) {

    override val layoutRes: Int = R.layout.view_content_base_web

}

class FrostContentRecycler @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrostContentView<FrostRecyclerView>(context, attrs, defStyleAttr, defStyleRes) {

    override val layoutRes: Int = R.layout.view_content_base_recycler

}

abstract class FrostContentView<out T> @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
        FrostContentParent where T : View, T : FrostContentCore {

    private val refresh: SwipeRefreshLayout by bindView(R.id.content_refresh)
    private val progress: ProgressBar by bindView(R.id.content_progress)
    val coreView: T by bindView(R.id.content_core)

    override val core: FrostContentCore
        get() = coreView

    override val progressObservable: PublishSubject<Int> = PublishSubject.create()
    override val refreshObservable: PublishSubject<Boolean> = PublishSubject.create()
    override val titleObservable: BehaviorSubject<String> = BehaviorSubject.create()

    override lateinit var baseUrl: String
    override var baseEnum: FbItem? = null

    protected abstract val layoutRes: Int

    override var swipeEnabled: Boolean
        get() = refresh.isEnabled
        set(value) {
            refresh.isEnabled = value
            if (!value) {
                // locked onto an input field; ensure content is visible
                (context as? MainActivityContract)?.collapseAppBar()
            }
        }

    /**
     * Sets up everything
     * Called by [bind]
     */
    protected fun init() {
        inflate(context, layoutRes, this)
        coreView.parent = this

        // bind observables
        progressObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            progress.invisibleIf(it == 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                progress.setProgress(it, true)
            else
                progress.progress = it
        }

        refreshObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    refresh.isRefreshing = it
                    refresh.isEnabled = true
                }
        refresh.setOnRefreshListener { coreView.reload(true) }

        reloadThemeSelf()

    }

    override fun bind(container: FrostContentContainer) {
        baseUrl = container.baseUrl
        baseEnum = container.baseEnum
        init()
        core.bind(container)
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
        titleObservable.onComplete()
        progressObservable.onComplete()
        refreshObservable.onComplete()
        core.destroy()
    }

    private var dispose: Disposable? = null
    private var transitionStart: Long = -1

    /**
     * Hook onto the refresh observable for one cycle
     * Animate toggles between the fancy ripple and the basic fade
     * The cycle only starts on the first load since there may have been another process when this is registered
     */
    override fun registerTransition(urlChanged: Boolean, animate: Boolean): Boolean {
        if (!urlChanged && dispose != null) {
            L.v { "Consuming url load" }
            return false // still in progress; do not bother with load
        }
        L.v { "Registered transition" }
        with(coreView) {
            var loading = dispose != null
            dispose?.dispose()
            dispose = refreshObservable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it) {
                            loading = true
                            transitionStart = System.currentTimeMillis()
                            clearAnimation()
                            if (isVisible)
                                fadeOut(duration = 200L)
                        } else if (loading) {
                            loading = false
                            if (animate && Prefs.animate) circularReveal(offset = WEB_LOAD_DELAY)
                            else fadeIn(duration = 200L, offset = WEB_LOAD_DELAY)
                            L.v { "Transition loaded in ${System.currentTimeMillis() - transitionStart} ms" }
                            dispose?.dispose()
                            dispose = null
                        }
                    }
        }
        return true
    }
}