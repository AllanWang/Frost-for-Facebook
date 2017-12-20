package com.pitchedapps.frost.views

import android.content.Context
import android.os.Build
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ProgressBar
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.invisibleIf
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.contracts.*
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FrostRefreshView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
        FrostContentParent {

    val refresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    val progress: ProgressBar by bindView(R.id.progress_bar)

    override var progressObservable: PublishSubject<Int> = PublishSubject.create()
    override var refreshObservable: PublishSubject<Boolean> = PublishSubject.create()
    override var titleObservable: BehaviorSubject<String> = BehaviorSubject.create()

    override lateinit var baseUrl: String
    override var baseEnum: FbItem? = null
    override var core: FrostContentCore? = null

    init {
        inflate(context, R.layout.view_swipable, this)

        reloadThemeSelf()

        // bind observables
        progressObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            progress.invisibleIf(it == 100)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) progress.setProgress(it, true)
            else progress.progress = it
        }
        refreshObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            refresh.isRefreshing = it
            refresh.isEnabled = true
        }
        refresh.setOnRefreshListener { core?.reload(true) }
    }


    override fun clean() {
        val core = core ?: return
        core.destroy()
        refresh.removeAllViews()
        this.core = null
    }

    override fun bind(container: FrostContentContainer) {
        baseUrl = container.baseUrl
        baseEnum = container.baseEnum
        when (container) {
            is FrostContentContainerDynamic -> {
                clean() // todo check if necessary
                container.content = this
                val core = container.createCore(context)
                this.core = core
                core.parent = this
                val child = core.bind(container)
                refresh.addView(child)
            }
            is FrostContentContainerStatic -> {
                core = container.core
                container.core.parent = this
                container.core.bind(container)
            }
            else -> throw IllegalStateException("Binding container contract is invalid: ${container::class.java.simpleName}")
        }

    }

    override fun reloadTheme() {
        reloadThemeSelf()
        core?.reloadTheme()
    }

    override fun reloadTextSize() {
        core?.reloadTextSize()
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