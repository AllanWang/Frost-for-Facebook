package com.pitchedapps.frost.web

import android.content.Context
import android.os.Build
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.Prefs
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostWebView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes), SwipeRefreshLayout.OnRefreshListener {
    val refresh: SwipeRefreshLayout by bindView(R.id.swipe_refresh)
    val web: FrostWebViewCore by bindView(R.id.frost_webview_core)
    val progress: ProgressBar by bindView(R.id.progressBar)

    init {
        inflate(getContext(), R.layout.swipe_webview, this)
        progress.tint(Prefs.textColor.withAlpha(180))
        refresh.setColorSchemeColors(Prefs.iconColor)
        refresh.setProgressBackgroundColorSchemeColor(Prefs.headerColor.withAlpha(255))
        web.progressObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            progress.visibility = if (it == 100) View.INVISIBLE else View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) progress.setProgress(it, true)
            else progress.progress = it
        }
        web.refreshObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
            refresh.isRefreshing = it
        }
        refresh.setOnRefreshListener(this)
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {
                web.visibility = View.VISIBLE
            }

            override fun onViewAttachedToWindow(v: View) {}
        })
    }

    //Some urls have postJavascript injections so make sure we load the base url
    override fun onRefresh() {
        when (web.baseUrl) {
            FbTab.MENU.url -> web.loadBaseUrl(true)
            else -> web.reload(true)
        }
    }

    fun onBackPressed(): Boolean {
        if (web.canGoBack()) {
            web.goBack()
            return true
        }
        return false
    }
}