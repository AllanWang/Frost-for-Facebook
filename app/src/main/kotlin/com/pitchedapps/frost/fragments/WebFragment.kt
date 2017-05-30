package com.pitchedapps.frost.fragments

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbUrl
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.putString
import com.pitchedapps.frost.views.FrostWebView
import com.pitchedapps.frost.views.SwipeRefreshBase
import com.pitchedapps.frost.views.WebStatus

/**
 * Created by Allan Wang on 2017-05-29.
 */


class WebFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    override fun onActivityEvent(position: Int, key: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRefresh() {
        web.reload()
    }

    companion object {
        private val ARG_URL = "arg_url"
        fun newInstance(position: Int, url: String) = BaseFragment.newInstance(WebFragment(), position).putString(ARG_URL, url)
        fun newInstance(position: Int, url: FbUrl = FbUrl.FEED) = newInstance(position, url.url)
    }

    val refresh: SwipeRefreshBase by bindView(R.id.swipe_refresh)
    val web: FrostWebView by bindView(R.id.frost_webview)
    lateinit var url: String
    private lateinit var unbinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = arguments.getString(ARG_URL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.swipe_webview, container, false)
        unbinder = ButterKnife.bind(this, view)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        web.observable.subscribe {
            t: WebStatus ->
            when (t) {
                WebStatus.LOADED, WebStatus.ERROR -> refresh.isRefreshing = false
                WebStatus.LOADING -> refresh.isRefreshing = true
            }
        }
        refresh.setOnRefreshListener(this)
        refresh.shouldSwipe = {
            L.e("Y ${web.scrollY}")
            SwipeRefreshBase.shouldScroll(web)
        }
        web.loadUrl(url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbinder.unbind()
    }

    override fun onBackPressed(): Boolean {
        if (web.canGoBack()) {
            web.goBack()
            return true
        }
        return false
    }
}