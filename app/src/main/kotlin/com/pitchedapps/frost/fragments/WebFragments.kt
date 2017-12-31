package com.pitchedapps.frost.fragments

import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.views.FrostWebView
import com.pitchedapps.frost.web.FrostWebViewClient
import com.pitchedapps.frost.web.FrostWebViewClientMenu

/**
 * Created by Allan Wang on 27/12/17.
 *
 * Basic webfragment
 * Do not extend as this is always a fallback
 */
class WebFragment : BaseFragment() {

    override val layoutRes: Int = R.layout.view_content_web

    /**
     * Given a webview, output a client
     */
    fun client(web: FrostWebView) = when (baseEnum) {
        FbItem.MENU -> FrostWebViewClientMenu(web)
        else -> FrostWebViewClient(web)
    }

}