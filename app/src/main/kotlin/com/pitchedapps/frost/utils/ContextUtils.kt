package com.pitchedapps.frost.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import com.pitchedapps.frost.R
import com.pitchedapps.frost.WebOverlayActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FbTab

/**
 * Created by Allan Wang on 2017-06-03.
 */
private const val EXTRA_COOKIES = "extra_cookies"
private const val ARG_URL = "arg_url"

fun Activity.launchNewTask(clazz: Class<out Activity>, cookieList: ArrayList<CookieModel> = arrayListOf()) {
    val intent = (Intent(this, clazz))
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putParcelableArrayListExtra(EXTRA_COOKIES, cookieList)
    startActivity(intent)
    finish()
}

fun Activity.cookies(): ArrayList<CookieModel> {
    return intent.extras?.getParcelableArrayList<CookieModel>(EXTRA_COOKIES) ?: arrayListOf()
}

fun Context.launchWebOverlay(url: String) {
    val intent = Intent(this, WebOverlayActivity::class.java)
    intent.putExtra(ARG_URL, url)
    val bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_right).toBundle()
    ContextCompat.startActivity(this, intent, bundle)
}

fun WebOverlayActivity.url(): String {
    return intent.extras?.getString(ARG_URL) ?: FbTab.FEED.url
}