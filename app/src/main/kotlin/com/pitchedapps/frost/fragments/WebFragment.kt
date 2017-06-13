package com.pitchedapps.frost.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.kau.utils.withBundle
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.web.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewCore
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by Allan Wang on 2017-05-29.
 */


class WebFragment : Fragment() {

    companion object {
        private const val ARG_URL = "arg_url"
        private const val ARG_ID = "arg_id"
        operator fun invoke(url: String) = WebFragment().withBundle {
            putString(ARG_URL, url)
        }

        operator fun invoke(data: FbTab) = WebFragment().withBundle {
            putString(ARG_URL, data.url)
            if (data.javascript != null) putSerializable(ARG_ID, data.javascript)
        }
    }

    //    val refresh: SwipeRefreshLayout by lazy { frostWebView.refresh }
    val web: FrostWebViewCore by lazy { frostWebView.web }
    lateinit var url: String
    var baseJavascript: JsAssets? = null
    lateinit private var frostWebView: FrostWebView
    private var firstLoad = true
    private var refreshDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments.getString(ARG_URL)
        baseJavascript = arguments.getSerializable(ARG_ID) as? JsAssets
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        frostWebView = FrostWebView(context)
        frostWebView.web.baseUrl = url
        frostWebView.web.baseJavascript = baseJavascript
        return frostWebView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstLoad()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        firstLoad()
    }

    fun firstLoad() {
        if (userVisibleHint && isVisible && firstLoad) {
            web.loadBaseUrl()
            firstLoad = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        refreshDisposable?.dispose()
        if (context is MainActivity)
            refreshDisposable = context.refreshObservable.observeOn(AndroidSchedulers.mainThread()).subscribe {
                clearHistory ->
                if (clearHistory) web.clearHistory()
                web.loadBaseUrl()
            }
    }

    override fun onDetach() {
        refreshDisposable?.dispose()
        super.onDetach()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        pauseLoad = false
        firstLoad()
    }

    override fun onPause() {
        pauseLoad = true
        super.onPause()
    }

    var pauseLoad: Boolean
        get() = web.settings.blockNetworkLoads
        set(value) {
            web.settings.blockNetworkLoads = value
        }


    fun onBackPressed() = frostWebView.onBackPressed()
}