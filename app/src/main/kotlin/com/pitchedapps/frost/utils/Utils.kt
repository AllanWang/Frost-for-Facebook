package com.pitchedapps.frost.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import ca.allanwang.kau.utils.*
import com.afollestad.materialdialogs.MaterialDialog
import com.pitchedapps.frost.LoginActivity
import com.pitchedapps.frost.R
import com.pitchedapps.frost.WebOverlayActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbTab

/**
 * Created by Allan Wang on 2017-06-03.
 */
internal const val EXTRA_COOKIES = "extra_cookies"
internal const val ARG_URL = "arg_url"

fun Context.launchNewTask(clazz: Class<out Activity>, cookieList: ArrayList<CookieModel> = arrayListOf(), clearStack: Boolean = clazz != LoginActivity::class.java) {
    startActivity(clazz, clearStack, {
        putParcelableArrayListExtra(EXTRA_COOKIES, cookieList)
    })
}

fun Activity.cookies(): ArrayList<CookieModel> {
    return intent?.extras?.getParcelableArrayList<CookieModel>(EXTRA_COOKIES) ?: arrayListOf()
}

val String.formattedFbUrl: String
    get() {
        var url = this
        if (url.startsWith("#!/")) url = url.substring(2)
        if (url.startsWith('/')) url = FB_URL_BASE + url.substring(1)
        url = url.replace("/#!/", "/")
        val ref = url.indexOf("?ref")
        if (ref != -1) url = url.substring(0, ref)
        return url
    }

fun Context.launchWebOverlay(url: String) {
    val argUrl = url.formattedFbUrl
    L.i("Launch web overlay: $argUrl")
    val intent = Intent(this, WebOverlayActivity::class.java)
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.putExtra(ARG_URL, argUrl)
//    val bundle = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_right).toBundle()
    ContextCompat.startActivity(this, intent, null)
}

fun WebOverlayActivity.url(): String {
    return intent.extras?.getString(ARG_URL) ?: FbTab.FEED.url
}


fun Activity.materialDialogThemed(action: MaterialDialog.Builder.() -> Unit): MaterialDialog {
    val builder = MaterialDialog.Builder(this).theme()
    builder.action()
    return builder.show()
}

fun MaterialDialog.Builder.theme(): MaterialDialog.Builder {
    val dimmerTextColor = Prefs.textColor.adjustAlpha(0.8f)
    titleColor(Prefs.textColor)
    contentColor(dimmerTextColor)
    widgetColor(dimmerTextColor)
    backgroundColor(Prefs.bgColor.lighten(0.1f).withMinAlpha(200))
    positiveColor(Prefs.textColor)
    negativeColor(Prefs.textColor)
    neutralColor(Prefs.textColor)
    return this
}

fun Activity.setFrostTheme(forceTransparent: Boolean = false) {
    val isTransparent = (Color.alpha(Prefs.bgColor) != 255) || forceTransparent
    if (Prefs.bgColor.isColorDark())
        setTheme(if (isTransparent) R.style.FrostTheme_Transparent else R.style.FrostTheme)
    else
        setTheme(if (isTransparent) R.style.FrostTheme_Light_Transparent else R.style.FrostTheme_Light)
}

fun Activity.setFrostColors(toolbar: Toolbar? = null, themeWindow: Boolean = true,
                            texts: Array<TextView> = arrayOf(), headers: Array<View> = arrayOf(), backgrounds: Array<View> = arrayOf()) {
    val darkAccent = Prefs.headerColor.darken()
    statusBarColor = darkAccent.darken().withAlpha(255)
    navigationBarColor = darkAccent
    if (themeWindow) window.setBackgroundDrawable(ColorDrawable(Prefs.bgColor))
    toolbar?.setBackgroundColor(darkAccent)
    toolbar?.setTitleTextColor(Prefs.iconColor)
    toolbar?.overflowIcon?.setTint(Prefs.iconColor)
    texts.forEach { it.setTextColor(Prefs.textColor) }
    headers.forEach { it.setBackgroundColor(darkAccent) }
    backgrounds.forEach { it.setBackgroundColor(Prefs.bgColor) }
}