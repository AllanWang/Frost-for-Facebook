package com.pitchedapps.frost

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import butterknife.ButterKnife
import com.jude.swipbackhelper.SwipeBackHelper
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.bindView
import com.pitchedapps.frost.web.FrostWebView


/**
 * Created by Allan Wang on 2017-06-01.
 */
class WebOverlayActivity : AppCompatActivity() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    val frostWeb: FrostWebView by bindView(R.id.frost_webview)

    companion object {
        private const val ARG_URL = "arg_url"
        fun newInstance(context: Context, url: String) {
            val intent = Intent(context, WebOverlayActivity::class.java)
            intent.putExtra(ARG_URL, url)
            val bundle = ActivityOptionsCompat.makeCustomAnimation(context, R.anim.slide_in_right, R.anim.slide_out_right).toBundle()
            ContextCompat.startActivity(context, intent, bundle)
        }

        fun newInstance(context: Context, url: FbTab) = newInstance(context, url.url)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_overlay)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        frostWeb.baseUrl = intent.extras.getString(ARG_URL)
        SwipeBackHelper.onCreate(this)
        SwipeBackHelper.getCurrentPage(this)
                .setSwipeBackEnable(true)
                .setSwipeSensitivity(0.5f)
                .setSwipeRelateEnable(true)
                .setSwipeRelateOffset(300)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        SwipeBackHelper.onPostCreate(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        SwipeBackHelper.onDestroy(this)
    }

}