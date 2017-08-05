package com.pitchedapps.frost.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.annotation.StringRes
import android.support.design.internal.SnackbarContentLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import ca.allanwang.kau.utils.*
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.*
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.facebook.formattedFbUrl
import java.io.IOException
import java.util.*

/**
 * Created by Allan Wang on 2017-06-03.
 */
const val EXTRA_COOKIES = "extra_cookies"
const val ARG_URL = "arg_url"
const val ARG_USER_ID = "arg_user_id"
const val ARG_IMAGE_URL = "arg_image_url"
const val ARG_TEXT = "arg_text"

fun Context.launchNewTask(clazz: Class<out Activity>, cookieList: ArrayList<CookieModel> = arrayListOf(), clearStack: Boolean = false) {
    startActivity(clazz, clearStack, intentBuilder = {
        putParcelableArrayListExtra(EXTRA_COOKIES, cookieList)
    })
}

fun Context.launchLogin(cookieList: ArrayList<CookieModel>, clearStack: Boolean = true) {
    if (cookieList.isNotEmpty()) launchNewTask(SelectorActivity::class.java, cookieList, clearStack)
    else launchNewTask(LoginActivity::class.java, clearStack = clearStack)
}

fun Activity.cookies(): ArrayList<CookieModel> {
    return intent?.extras?.getParcelableArrayList<CookieModel>(EXTRA_COOKIES) ?: arrayListOf()
}

fun Context.launchWebOverlay(url: String) {
    val argUrl = url.formattedFbUrl
    L.v("Launch received $url")
    L.i("Launch web overlay: $argUrl")
    startActivity(WebOverlayActivity::class.java, false, intentBuilder = {
        putExtra(ARG_URL, argUrl)
    })
}

fun Context.launchImageActivity(imageUrl: String, text: String?) {
    startActivity(ImageActivity::class.java, intentBuilder = {
        putExtra(ARG_IMAGE_URL, imageUrl)
        putExtra(ARG_TEXT, text)
    })
}

fun Activity.launchIntroActivity(cookieList: ArrayList<CookieModel>)
        = launchNewTask(IntroActivity::class.java, cookieList, true)

fun WebOverlayActivity.url(): String {
    return intent.extras?.getString(ARG_URL) ?: FbTab.FEED.url
}

fun Context.materialDialogThemed(action: MaterialDialog.Builder.() -> Unit): MaterialDialog {
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
    if (Prefs.bgColor.isColorDark)
        setTheme(if (isTransparent) R.style.FrostTheme_Transparent else R.style.FrostTheme)
    else
        setTheme(if (isTransparent) R.style.FrostTheme_Light_Transparent else R.style.FrostTheme_Light)
}

fun Activity.setFrostColors(toolbar: Toolbar? = null, themeWindow: Boolean = true,
                            texts: Array<TextView> = arrayOf(), headers: Array<View> = arrayOf(), backgrounds: Array<View> = arrayOf()) {
    statusBarColor = Prefs.headerColor.darken(0.1f).withAlpha(255)
    if (Prefs.tintNavBar) navigationBarColor = Prefs.headerColor
    if (themeWindow) window.setBackgroundDrawable(ColorDrawable(Prefs.bgColor))
    toolbar?.setBackgroundColor(Prefs.headerColor)
    toolbar?.setTitleTextColor(Prefs.iconColor)
    toolbar?.overflowIcon?.setTint(Prefs.iconColor)
    texts.forEach { it.setTextColor(Prefs.textColor) }
    headers.forEach { it.setBackgroundColor(Prefs.headerColor) }
    backgrounds.forEach { it.setBackgroundColor(Prefs.bgColor) }
}

fun frostAnswers(action: Answers.() -> Unit) {
    if (BuildConfig.DEBUG || !Prefs.analytics) return
    Answers.getInstance().action()
}

fun frostAnswersCustom(name: String, vararg events: Pair<String, Any>) {
    frostAnswers {
        logCustom(CustomEvent("Frost $name").apply {
            events.forEach { (key, value) ->
                if (value is Number) putCustomAttribute(key, value)
                else putCustomAttribute(key, value.toString())
            }
        })
    }
}

fun View.frostSnackbar(@StringRes text: Int, builder: Snackbar.() -> Unit = {}) {
    Snackbar.make(this, text, Snackbar.LENGTH_LONG).apply {
        builder()
        //hacky workaround, but it has proper checks and shouldn't crash
        ((view as? FrameLayout)?.getChildAt(0) as? SnackbarContentLayout)?.apply {
            messageView.setTextColor(Prefs.textColor)
            actionView.setTextColor(Prefs.accentColor)
            //only set if previous text colors are set
            view.setBackgroundColor(Prefs.bgColor.withAlpha(255).colorToForeground(0.1f))
        }
        show()
    }
}

fun Activity.frostNavigationBar() {
    navigationBarColor = if (Prefs.tintNavBar) Prefs.headerColor else Color.BLACK
}

fun <T> RequestBuilder<T>.withRoundIcon() = apply(RequestOptions().transform(CircleCrop()))!!

@Throws(IOException::class)
fun createMediaFile(extension: String) = createMediaFile("Frost", extension)

@Throws(IOException::class)
fun Context.createPrivateMediaFile(extension: String) = createPrivateMediaFile("Frost", extension)

/**
 * Tries to send the uri to the proper activity via an intent
 * @returns {@code true} if activity is resolved, {@code false} otherwise
 */
fun Context.resolveActivityForUri(uri: Uri): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(packageManager) == null) return false
    startActivity(intent)
    return true
}

