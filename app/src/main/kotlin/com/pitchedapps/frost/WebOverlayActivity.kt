package com.pitchedapps.frost

import android.os.Bundle
import android.support.v7.widget.Toolbar
import butterknife.ButterKnife
import com.jude.swipbackhelper.SwipeBackHelper
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.utils.url
import com.pitchedapps.frost.web.FrostWebView


/**
 * Created by Allan Wang on 2017-06-01.
 */
class WebOverlayActivity : BaseLeakActivity() {

    val toolbar: Toolbar by bindView(R.id.overlay_toolbar)
    val frostWeb: FrostWebView by bindView(R.id.overlay_frost_webview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_overlay)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        frostWeb.baseUrl = url()
        frostWeb.web.loadBaseUrl()
        SwipeBackHelper.onCreate(this)
        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(true)
                .setSwipeSensitivity(0.5f)
                .setSwipeRelateEnable(true)
                .setSwipeRelateOffset(300)
        frostWeb.web.addTitleListener({ toolbar.title = it })
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        SwipeBackHelper.onPostCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SwipeBackHelper.onDestroy(this)
    }

    override fun onBackPressed() {
        if (!frostWeb.onBackPressed()) super.onBackPressed()
    }
}