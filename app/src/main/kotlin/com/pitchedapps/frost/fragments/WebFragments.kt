package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.R
import com.pitchedapps.frost.views.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewClient
import com.pitchedapps.frost.web.FrostWebViewClientMenu

/**
 * Created by Allan Wang on 27/12/17.
 */
open class WebFragment : BaseFragment() {

    override val layoutRes: Int = R.layout.view_content_web

    /**
     * Given a webview, output a client
     */
    open fun client(web: FrostWebView) = FrostWebViewClient(web)

}

class WebFragmentMenu : WebFragment() {

    override fun client(web: FrostWebView) = FrostWebViewClientMenu(web)

}