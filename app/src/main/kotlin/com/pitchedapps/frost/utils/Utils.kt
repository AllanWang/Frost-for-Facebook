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
import android.content.ActivityNotFoundException
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
import ca.allanwang.kau.utils.colorToForeground
import ca.allanwang.kau.utils.ctxCoroutine
import ca.allanwang.kau.utils.darken
import ca.allanwang.kau.utils.isColorDark
import ca.allanwang.kau.utils.navigationBarColor
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.startActivity
import ca.allanwang.kau.utils.startActivityForResult
import ca.allanwang.kau.utils.statusBarColor
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.with
import ca.allanwang.kau.utils.withAlpha
import ca.allanwang.kau.xml.showChangelog
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
import com.pitchedapps.frost.activities.WebOverlayMobileActivity
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.facebook.FACEBOOK_COM
import com.pitchedapps.frost.facebook.FBCDN_NET
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.FbUrlFormatter.Companion.VIDEO_REDIRECT
import com.pitchedapps.frost.facebook.MESSENGER_COM
import com.pitchedapps.frost.facebook.USER_AGENT
import com.pitchedapps.frost.facebook.formattedFbUri
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.ThemeProvider
import com.pitchedapps.frost.prefs.Prefs
import dagger.hilt.android.scopes.ActivityScoped
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.commons.text.StringEscapeUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

/** Created by Allan Wang on 2017-06-03. */
const val EXTRA_COOKIES = "extra_cookies"
const val ARG_URL = "arg_url"
const val ARG_USER_ID = "arg_user_id"
const val ARG_IMAGE_URL = "arg_image_url"
const val ARG_TEXT = "arg_text"
const val ARG_COOKIE = "arg_cookie"

inline fun <reified T : Activity> Context.launchNewTask(
  cookieList: ArrayList<CookieEntity> = arrayListOf(),
  clearStack: Boolean = false
) {
  startActivity<T>(
    clearStack,
    intentBuilder = { putParcelableArrayListExtra(EXTRA_COOKIES, cookieList) }
  )
}

fun Context.launchLogin(cookieList: ArrayList<CookieEntity>, clearStack: Boolean = true) {
  if (cookieList.isNotEmpty()) launchNewTask<SelectorActivity>(cookieList, clearStack)
  else launchNewTask<LoginActivity>(clearStack = clearStack)
}

fun Activity.cookies(): ArrayList<CookieEntity> {
  return intent?.getParcelableArrayListExtra<CookieEntity>(EXTRA_COOKIES) ?: arrayListOf()
}

/**
 * Launches the given url in a new overlay (if it already isn't in an overlay) Note that most
 * requests may need to first check if the url can be launched as an overlay See [requestWebOverlay]
 * to verify the launch
 */
private inline fun <reified T : WebOverlayActivityBase> Context.launchWebOverlayImpl(
  url: String,
  fbCookie: FbCookie,
  prefs: Prefs
) {
  val argUrl = url.formattedFbUrl
  L.v { "Launch received: $url\nLaunch web overlay: $argUrl" }
  if (argUrl.isFacebookUrl && argUrl.contains("/logout.php")) {
    L.d { "Logout php found" }
    ctxCoroutine.launch { fbCookie.logout(this@launchWebOverlayImpl, deleteCookie = false) }
  } else if (!(prefs.linksInDefaultApp && startActivityForUri(Uri.parse(argUrl)))) {
    startActivity<T>(false, intentBuilder = { putExtra(ARG_URL, argUrl) })
  }
}

fun Context.launchWebOverlay(url: String, fbCookie: FbCookie, prefs: Prefs) =
  launchWebOverlayImpl<WebOverlayActivity>(url, fbCookie, prefs)

// TODO Currently, default is overlay. Switch this if default changes
fun Context.launchWebOverlayDesktop(url: String, fbCookie: FbCookie, prefs: Prefs) =
  launchWebOverlay(url, fbCookie, prefs)

fun Context.launchWebOverlayMobile(url: String, fbCookie: FbCookie, prefs: Prefs) =
  launchWebOverlayImpl<WebOverlayMobileActivity>(url, fbCookie, prefs)

private fun Context.fadeBundle() =
  ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    .toBundle()

fun Context.launchImageActivity(imageUrl: String, text: String? = null, cookie: String? = null) {
  startActivity<ImageActivity>(
    intentBuilder = {
      putExtras(fadeBundle())
      putExtra(ARG_IMAGE_URL, imageUrl)
      putExtra(ARG_TEXT, text)
      putExtra(ARG_COOKIE, cookie)
    }
  )
}

fun Activity.launchTabCustomizerActivity() {
  startActivityForResult<TabCustomizerActivity>(
    SettingsActivity.ACTIVITY_REQUEST_TABS,
    bundleBuilder = { with(fadeBundle()) }
  )
}

fun WebOverlayActivity.url(): String {
  return intent.getStringExtra(ARG_URL) ?: FbItem.FEED.url
}

@ActivityScoped
class ActivityThemer
@Inject
constructor(
  private val activity: Activity,
  private val prefs: Prefs,
  private val themeProvider: ThemeProvider
) {
  fun setFrostTheme(forceTransparent: Boolean = false) {
    val isTransparent =
      forceTransparent ||
        (Color.alpha(themeProvider.bgColor) != 255) ||
        (Color.alpha(themeProvider.headerColor) != 255)
    if (themeProvider.bgColor.isColorDark) {
      activity.setTheme(if (isTransparent) R.style.FrostTheme_Transparent else R.style.FrostTheme)
    } else {
      activity.setTheme(
        if (isTransparent) R.style.FrostTheme_Light_Transparent else R.style.FrostTheme_Light
      )
    }
  }

  fun setFrostColors(builder: ActivityThemeUtils.() -> Unit) {
    val themer = ActivityThemeUtils(prefs = prefs, themeProvider = themeProvider)
    themer.builder()
    themer.theme(activity)
  }
}

class ActivityThemeUtils(private val prefs: Prefs, private val themeProvider: ThemeProvider) {
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
      statusBarColor = themeProvider.headerColor.darken(0.1f).withAlpha(255)
      if (prefs.tintNavBar) navigationBarColor = themeProvider.headerColor
      if (themeWindow) window.setBackgroundDrawable(ColorDrawable(themeProvider.bgColor))
      toolbar?.setBackgroundColor(themeProvider.headerColor)
      toolbar?.setTitleTextColor(themeProvider.iconColor)
      toolbar?.overflowIcon?.setTint(themeProvider.iconColor)
      texts.forEach { it.setTextColor(themeProvider.textColor) }
      headers.forEach { it.setBackgroundColor(themeProvider.headerColor) }
      backgrounds.forEach { it.setBackgroundColor(themeProvider.bgColor) }
    }
  }
}

fun frostEvent(name: String, vararg events: Pair<String, Any>) {
  // todo bind
  L.v { "Event: $name ${events.joinToString(", ")}" }
}

/** Helper method to quietly keep track of throwable issues */
fun Throwable?.logFrostEvent(text: String) {
  val msg = if (this == null) text else "$text: $message"
  L.e { msg }
  frostEvent("Errors", "text" to text, "message" to (this?.message ?: "NA"))
}

fun Activity.frostSnackbar(
  @StringRes text: Int,
  themeProvider: ThemeProvider,
  builder: Snackbar.() -> Unit = {}
) = snackbar(text, Snackbar.LENGTH_LONG, frostSnackbar(themeProvider, builder))

fun View.frostSnackbar(
  @StringRes text: Int,
  themeProvider: ThemeProvider,
  builder: Snackbar.() -> Unit = {}
) = snackbar(text, Snackbar.LENGTH_LONG, frostSnackbar(themeProvider, builder))

@SuppressLint("RestrictedApi")
private inline fun frostSnackbar(
  themeProvider: ThemeProvider,
  crossinline builder: Snackbar.() -> Unit
): Snackbar.() -> Unit = {
  builder()
  // hacky workaround, but it has proper checks and shouldn't crash
  ((view as? FrameLayout)?.getChildAt(0) as? SnackbarContentLayout)?.apply {
    messageView.setTextColor(themeProvider.textColor)
    actionView.setTextColor(themeProvider.accentColor)
    // only set if previous text colors are set
    view.setBackgroundColor(themeProvider.bgColor.withAlpha(255).colorToForeground(0.1f))
  }
}

fun Activity.frostNavigationBar(prefs: Prefs, themeProvider: ThemeProvider) {
  navigationBarColor = if (prefs.tintNavBar) themeProvider.headerColor else Color.BLACK
}

@Throws(IOException::class)
fun createMediaFile(extension: String) = createMediaFile("Frost", extension)

@Throws(IOException::class)
fun Context.createPrivateMediaFile(extension: String) = createPrivateMediaFile("Frost", extension)

/**
 * Tries to send the uri to the proper activity via an intent returns [true] if activity is
 * resolved, [false] otherwise For safety, any uri that ([isFacebookUrl] or [isMessengerUrl])
 * without [isExplicitIntent] will return [false]
 */
fun Context.startActivityForUri(uri: Uri): Boolean {
  val url = uri.toString()
  if ((url.isFacebookUrl || url.isMessengerUrl) && !url.isExplicitIntent) {
    return false
  }
  val intent = Intent(Intent.ACTION_VIEW, uri.formattedFbUri)
  return try {
    startActivity(intent)
    true
  } catch (e: ActivityNotFoundException) {
    false
  }
}

/** [true] if url contains [FACEBOOK_COM] */
inline val String?.isFacebookUrl
  get() = this != null && (contains(FACEBOOK_COM) || contains(FBCDN_NET))

inline val String?.isMessengerUrl
  get() = this != null && contains(MESSENGER_COM)

inline val String?.isFbCookie
  get() = this != null && contains("c_user")

/** [true] if url is a video and can be accepted by VideoViewer */
inline val String.isVideoUrl
  get() = startsWith(VIDEO_REDIRECT) || (startsWith("https://video-") && contains(FBCDN_NET))

/** [true] if url directly leads to a usable image */
inline val String.isImageUrl: Boolean
  get() {
    return contains(FBCDN_NET) && (contains(".png") || contains(".jpg"))
  }

/** [true] if url can be retrieved to get a direct image url */
inline val String.isIndirectImageUrl: Boolean
  get() {
    return contains("/photo/view_full_size/") && contains("fbid=")
  }

/** [true] if url can be displayed in a different webview */
inline val String?.isIndependent: Boolean
  get() {
    if (this == null || length < 5) return false // ignore short queries
    if (this[0] == '#' && !contains('/')) return false // ignore element values
    if (startsWith("http") && !isFacebookUrl) return true // ignore non facebook urls
    if (dependentSegments.any { contains(it) }) return false // ignore known dependent segments
    return true
  }

val dependentSegments =
  arrayOf(
    "photoset_token",
    "direct_action_execute",
    "messages/?pageNum",
    "sharer.php",
    "events/permalink",
    "events/feed/watch",
    /*
     * Add new members to groups
     *
     * No longer dependent again as of 12/20/2018
     */
    // "madminpanel",
    /** Editing images */
    "/confirmation/?",
    /** Remove entry from "people you may know" */
    "/pymk/xout/",
    /*
     * Facebook messages have the following cases for the tid query
     * mid* or id* for newer threads, which can be launched in new windows
     * or a hash for old threads, which must be loaded on old threads
     */
    "messages/read/?tid=id",
    "messages/read/?tid=mid",
    // For some reason townhall doesn't load independently
    // This will allow it to load, but going back unfortunately messes up the menu client
    // See https://github.com/AllanWang/Frost-for-Facebook/issues/1593
    "/townhall/"
  )

inline val String?.isExplicitIntent
  get() = this != null && (startsWith("intent://") || startsWith("market://"))

fun String.urlEncode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())

fun Context.frostChangelog() = showChangelog(R.xml.frost_changelog)

fun Context.frostUriFromFile(file: File): Uri =
  FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)

/** Gets uri from our own resolver if it's a file, or return the parsed uri otherwise */
fun Context.frostUri(entry: String): Uri {
  val uri = Uri.parse(entry)
  val path = uri.path
  if (uri.scheme == "file" && path != null) {
    return frostUriFromFile(File(path))
  }
  return uri
}

inline fun Context.sendFrostEmail(
  @StringRes subjectId: Int,
  prefs: Prefs,
  crossinline builder: EmailBuilder.() -> Unit
) = sendFrostEmail(string(subjectId), prefs, builder)

inline fun Context.sendFrostEmail(
  subjectId: String,
  prefs: Prefs,
  crossinline builder: EmailBuilder.() -> Unit
) =
  sendEmail("", subjectId) {
    builder()
    addFrostDetails(prefs)
  }

fun EmailBuilder.addFrostDetails(prefs: Prefs) {
  addItem("Prev version", prefs.prevVersionCode.toString())
  val proTag = "FO"
  addItem("Random Frost ID", "${prefs.frostId}-$proTag")
  addItem("Locale", Locale.getDefault().displayName)
}

fun frostJsoup(cookie: String?, url: String): Document =
  Jsoup.connect(url)
    .run { if (cookie.isNullOrBlank()) this else cookie(FACEBOOK_COM, cookie) }
    .userAgent(USER_AGENT)
    .get()

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
    if (parent != null && !parent.exists() && !parent.mkdirs()) return false
  }
  return createNewFile()
}

fun File.createFreshDir(): Boolean {
  if (exists() && !deleteRecursively()) return false
  return mkdirs()
}

fun String.unescapeHtml(): String =
  StringEscapeUtils.unescapeXml(this).replace("\\u003C", "<").replace("\\\"", "\"")

suspend fun Context.loadAssets(themeProvider: ThemeProvider): Unit = coroutineScope {
  themeProvider.preload()
  JsAssets.load(this@loadAssets)
}
