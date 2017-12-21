package com.pitchedapps.frost.views

import android.content.Context
import android.os.Build
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.invisibleIf
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.FrostContentContainer
import com.pitchedapps.frost.contracts.FrostContentCore
import com.pitchedapps.frost.contracts.FrostContentParent
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FrostContentWeb @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrostContentView<FrostWebView>(context, attrs, defStyleAttr, defStyleRes) {

    override val layoutRes: Int = R.layout.view_content_base_web

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
        refreshObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
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
}