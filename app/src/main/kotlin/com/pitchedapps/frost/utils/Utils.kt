/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import ca.allanwang.kau.email.EmailBuilder
import ca.allanwang.kau.email.sendEmail
import ca.allanwang.kau.mediapicker.createMediaFile
import ca.allanwang.kau.mediapicker.createPrivateMediaFile
import ca.allanwang.kau.utils.adjustAlpha
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.lighten
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.startActivity
import ca.allanwang.kau.utils.startActivityForResult
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.with
import ca.allanwang.kau.utils.withAlpha
import ca.allanwang.kau.utils.withMinAlpha
import ca.allanwang.kau.xml.showChangelog
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.ImageActivity
import com.pitchedapps.frost.activities.LoginActivity
import com.pitchedapps.frost.activities.SelectorActivity
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.activities.TabCustomizerActivity
import com.pitchedapps.frost.activities.WebOverlayActivity
import com.pitchedapps.frost.activities.WebOverlayActivityBase
import com.pitchedapps.frost.activities.WebOverlayBasicActivity
import com.pitchedapps.frost.dbflow.CookieModel
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FBCDN_NET
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.FbUrlFormatter.Companion.VIDEO_REDIRECT
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.formattedFbUrl
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Locale

/**
 * Created by Allan Wang on 2017-06-03.
 */
const val EXTRA_COOKIES = "extra_cookies"
const val ARG_URL = "arg_url"
const val ARG_USER_ID = "arg_user_id"
const val ARG_IMAGE_URL = "arg_image_url"
const val ARG_TEXT = "arg_text"
const val ARG_COOKIE = "arg_cookie"

inline fun <reified T : Activity> Context.launchNewTask(
    cookieList: ArrayList<CookieModel> = arrayListOf(),
    clearStack: Boolean = false
) {
    startActivity<T>(clearStack, intentBuilder = {
        putParcelableArrayListExtra(EXTRA_COOKIES, cookieList)
    })
}

fun Context.launchLogin(cookieList: ArrayList<CookieModel>, clearStack: Boolean = true) {
    if (cookieList.isNotEmpty()) launchNewTask<SelectorActivity>(cookieList, clearStack)
    else launchNewTask<LoginActivity>(clearStack = clearStack)
}

fun Activity.cookies(): ArrayList<CookieModel> {
    return intent?.getParcelableArrayListExtra<CookieModel>(EXTRA_COOKIES) ?: arrayListOf()
}

/**
 * Launches the given url in a new overlay (if it already isn't in an overlay)
 * Note that most requests may need to first check if the url can be launched as an overlay
 * See [requestWebOverlay] to verify the launch
 */
private inline fun <reified T : WebOverlayActivityBase> Context.launchWebOverlayImpl(url: String) {
    val argUrl = url.formattedFbUrl
    L.v { "Launch received: $url\nLaunch web overlay: $argUrl" }
    if (argUrl.isFacebookUrl && argUrl.contains("/logout.php"))
        FbCookie.logout(this)
    else if (!(Prefs.linksInDefaultApp && resolveActivityForUri(Uri.parse(argUrl))))
        startActivity<T>(false, intentBuilder = {
            putExtra(ARG_URL, argUrl)
        })
}

fun Context.launchWebOverlay(url: String) = launchWebOverlayImpl<WebOverlayActivity>(url)

fun Context.launchWebOverlayBasic(url: String) = launchWebOverlayImpl<WebOverlayBasicActivity>(url)

private fun Context.fadeBundle() = ActivityOptions.makeCustomAnimation(
    this,
    android.R.anim.fade_in, android.R.anim.fade_out
).toBundle()

fun Context.launchImageActivity(imageUrl: String, text: String? = null, cookie: String? = null) {
    startActivity<ImageActivity>(intentBuilder = {
        putExtras(fadeBundle())
        putExtra(ARG_IMAGE_URL, imageUrl)
        putExtra(ARG_TEXT, text)
        putExtra(ARG_COOKIE, cookie)
    })
}

fun Activity.launchTabCustomizerActivity() {
    startActivityForResult<TabCustomizerActivity>(SettingsActivity.ACTIVITY_REQUEST_TABS, bundleBuilder = {
        with(fadeBundle())
    })
}

fun WebOverlayActivity.url(): String {
    return intent.getStringExtra(ARG_URL) ?: FbItem.FEED.url
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

class ActivityThemeUtils {

    private var toolbar: Toolbar? = null
    var themeWindow = true
    private var texts = mutableListOf<TextView>()
    private var headers = mutableListOf<View>()
    private var backgrounds = mutableListOf<View>()

    fun toolbar(toolbar: Toolbar) {
        this.toolbar = toolbar
    }

    fun text(vararg views: TextView) {
        texts.addAll(views)
    }

    fun header(vararg views: View) {
        headers.addAll(views)
    }

    fun background(vararg views: View) {
        backgrounds.addAll(views)
    }

    fun theme(activity: Activity) {
        with(activity) {
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
    }
}

inline fun Activity.setFrostColors(builder: ActivityThemeUtils.() -> Unit) {
    val themer = ActivityThemeUtils()
    themer.builder()
    themer.theme(this)
}

fun frostEvent(name: String, vararg events: Pair<String, Any>) {
    // todo bind
    L.v { "Event: $name ${events.joinToString(", ")}" }
}

/**
 * Helper method to quietly keep track of throwable issues
 */
fun Throwable?.logFrostEvent(text: String) {
    val msg = if (this == null) text else "$text: $message"
    L.e { msg }
    frostEvent("Errors", "text" to text, "message" to (this?.message ?: "NA"))
}

fun Activity.frostSnackbar(@StringRes text: Int, builder: Snackbar.() -> Unit = {}) =
    snackbar(text, Snackbar.LENGTH_LONG, frostSnackbar(builder))

fun View.frostSnackbar(@StringRes text: Int, builder: Snackbar.() -> Unit = {}) =
    snackbar(text, Snackbar.LENGTH_LONG, frostSnackbar(builder))

@SuppressLint("RestrictedApi")
private inline fun frostSnackbar(crossinline builder: Snackbar.() -> Unit): Snackbar.() -> Unit = {
    builder()
    //hacky workaround, but it has proper checks and shouldn't crash
    ((view as? FrameLayout)?.getChildAt(0) as? SnackbarContentLayout)?.apply {
        messageView.setTextColor(Prefs.textColor)
        actionView.setTextColor(Prefs.accentColor)
        //only set if previous text colors are set
        view.setBackgroundColor(Prefs.bgColor.withAlpha(255).colorToForeground(0.1f))
    }
}

fun Activity.frostNavigationBar() {
    navigationBarColor = if (Prefs.tintNavBar) Prefs.headerColor else Color.BLACK
}

@Throws(IOException::class)
fun createMediaFile(extension: String) = createMediaFile("Frost", extension)

@Throws(IOException::class)
fun Context.createPrivateMediaFile(extension: String) = createPrivateMediaFile("Frost", extension)

/**
 * Tries to send the uri to the proper activity via an intent
 * returns [true] if activity is resolved, [false] otherwise
 * For safety, any uri that [isFacebookUrl] without [isExplicitIntent] will return [false]
 */
fun Context.resolveActivityForUri(uri: Uri): Boolean {
    val url = uri.toString()
    if (url.isFacebookUrl && !url.isExplicitIntent) return false
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(packageManager) == null) return false
    startActivity(intent)
    return true
}

/**
 * [true] if url contains [FACEBOOK_COM]
 */
inline val String?.isFacebookUrl
    get() = this != null && (contains(FACEBOOK_COM) || contains(FBCDN_NET))

/**
 * [true] if url is a video and can be accepted by VideoViewer
 */
inline val String.isVideoUrl
    get() = startsWith(VIDEO_REDIRECT) ||
        (startsWith("https://video-") && contains(FBCDN_NET))

/**
 * [true] if url directly leads to a usable image
 */
inline val String.isImageUrl: Boolean
    get() {
        return contains(FBCDN_NET) && (contains(".png") || contains(".jpg"))
    }

/**
 * [true] if url can be retrieved to get a direct image url
 */
inline val String.isIndirectImageUrl: Boolean
    get() {
        return contains("/photo/view_full_size/") && contains("fbid=")
    }

/**
 * [true] if url can be displayed in a different webview
 */
inline val String?.isIndependent: Boolean
    get() {
        if (this == null || length < 5) return false                // ignore short queries
        if (this[0] == '#' && !contains('/')) return false          // ignore element values
        if (startsWith("http") && !isFacebookUrl) return true       // ignore non facebook urls
        if (dependentSegments.any { contains(it) }) return false    // ignore known dependent segments
        return true
    }

val dependentSegments = arrayOf(
    "photoset_token", "direct_action_execute", "messages/?pageNum", "sharer.php",
    "events/permalink", "events/feed/watch",
    /*
     * Add new members to groups
     */
    "madminpanel",
    /**
     * Editing images
     */
    "/confirmation/?",
    /*
     * Facebook messages have the following cases for the tid query
     * mid* or id* for newer threads, which can be launched in new windows
     * or a hash for old threads, which must be loaded on old threads
     */
    "messages/read/?tid=id", "messages/read/?tid=mid"
)

inline val String?.isExplicitIntent
    get() = this != null && startsWith("intent://")

fun Context.frostChangelog() = showChangelog(R.xml.frost_changelog, Prefs.textColor) {
    theme()
}

fun Context.frostUriFromFile(file: File): Uri =
    FileProvider.getUriForFile(
        this,
        BuildConfig.APPLICATION_ID + ".provider",
        file
    )

inline fun Context.sendFrostEmail(@StringRes subjectId: Int, crossinline builder: EmailBuilder.() -> Unit) =
    sendFrostEmail(string(subjectId), builder)

inline fun Context.sendFrostEmail(subjectId: String, crossinline builder: EmailBuilder.() -> Unit) =
    sendEmail(string(R.string.dev_email), subjectId) {
        builder()
        addFrostDetails()
    }

fun EmailBuilder.addFrostDetails() {
    addItem("Prev version", Prefs.prevVersionCode.toString())
    val proTag = "FO"
//    if (IS_FROST_PRO) "TY" else "FP"
    addItem("Random Frost ID", "${Prefs.frostId}-$proTag")
    addItem("Locale", Locale.getDefault().displayName)
}

fun frostJsoup(url: String) = frostJsoup(FbCookie.webCookie, url)

fun frostJsoup(cookie: String?, url: String) =
    Jsoup.connect(url).cookie(FACEBOOK_COM, cookie).userAgent(USER_AGENT_BASIC).get()!!

fun Element.first(vararg select: String): Element? {
    select.forEach {
        val e = select(it)
        if (e.size > 0) return e.first()
    }
    return null
}

fun File.createFreshFile(): Boolean {
    if (exists()) {
        if (!delete()) return false
    } else {
        val parent = parentFile
        if (!parent.exists() && !parent.mkdirs())
            return false
    }
    return createNewFile()
}

fun File.createFreshDir(): Boolean {
    if (exists() && !deleteRecursively())
        return false
    return mkdirs()
}

fun String.unescapeHtml(): String =
    StringEscapeUtils.unescapeXml(this)
        .replace("\\u003C", "<")
        .replace("\\\"", "\"")
