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
import com.pitchedapps.frost.contracts.FrostObservables
import com.pitchedapps.frost.contracts.FrostViewContract
import com.pitchedapps.frost.contracts.DynamicUiContract
import com.pitchedapps.frost.contracts.FrostUrlData
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class FrostRefreshView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), SwipeRefreshLayout.OnRefreshListener,
        FrostObservables, FrostUrlData, DynamicUiContract {

    val refresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    val progress: ProgressBar by bindView(R.id.progress_bar)

    override var progressObservable: PublishSubject<Int> = PublishSubject.create()
    override var refreshObservable: PublishSubject<Boolean> = PublishSubject.create()
    override var titleObservable: BehaviorSubject<String> = BehaviorSubject.create()

    override lateinit var baseUrl: String
    override var baseEnum: FbItem? = null

    lateinit var inner: FrostViewContract

    init {
        inflate(context, R.layout.view_swipable, this)

        reloadOwnTheme()

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
        refresh.setOnRefreshListener(this)
    }

    override fun reloadTextSize() {
        inner.reloadTextSize()
    }

    private fun reloadOwnTheme() {
        progress.tint(Prefs.textColor.withAlpha(180))
        refresh.setColorSchemeColors(Prefs.iconColor)
        refresh.setProgressBackgroundColorSchemeColor(Prefs.headerColor.withAlpha(255))
    }

    override fun reloadTheme() {
        reloadOwnTheme()
        inner.reloadTheme()
    }

    override fun onRefresh() = inner.reload(true)
}