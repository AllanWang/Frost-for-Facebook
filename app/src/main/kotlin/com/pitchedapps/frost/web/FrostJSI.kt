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
package com.pitchedapps.frost.web

import android.app.Activity
import android.webkit.JavascriptInterface
import ca.allanwang.kau.utils.ctxCoroutine
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.WebOverlayActivityBase
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.db.CookieEntity
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.prefs.Prefs
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.WebContext
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.isIndependent
import com.pitchedapps.frost.utils.launchImageActivity
import com.pitchedapps.frost.utils.showWebContextMenu
import com.pitchedapps.frost.views.FrostWebView
import javax.inject.Inject
import kotlinx.coroutines.launch

/** Created by Allan Wang on 2017-06-01. */
@FrostWebScoped
class FrostJSI
@Inject
internal constructor(
  val web: FrostWebView,
  private val activity: Activity,
  private val fbCookie: FbCookie,
  private val prefs: Prefs,
  @FrostRefresh private val refreshEmit: FrostEmitter<Boolean>
) {

  private val mainActivity: MainActivity? = activity as? MainActivity
  private val webActivity: WebOverlayActivityBase? = activity as? WebOverlayActivityBase
  private val headerEmit: FrostEmitter<String>? = mainActivity?.headerEmit
  private val cookies: List<CookieEntity> = activity.cookies()

  /**
   * Attempts to load the url in an overlay Returns {@code true} if successful, meaning the event is
   * consumed, or {@code false} otherwise, meaning the event should be propagated
   */
  @JavascriptInterface
  fun loadUrl(url: String?): Boolean = if (url == null) false else web.requestWebOverlay(url)

  @JavascriptInterface
  fun loadVideo(url: String?, isGif: Boolean): Boolean =
    if (url != null && prefs.enablePip) {
      web.post {
        (activity as? VideoViewHolder)?.showVideo(url, isGif)
          ?: L.e { "Could not load video; contract not implemented" }
      }
      true
    } else {
      false
    }

  @JavascriptInterface
  fun reloadBaseUrl(animate: Boolean) {
    L.d { "FrostJSI reload" }
    web.post {
      web.stopLoading()
      web.reloadBase(animate)
    }
  }

  @JavascriptInterface
  fun contextMenu(url: String?, text: String?) {
    // url will be formatted through webcontext
    web.post {
      activity.showWebContextMenu(
        WebContext(url.takeIf { it.isIndependent }, text),
        fbCookie,
        prefs,
      )
    }
  }

  /**
   * Get notified when a stationary long click starts or ends This will be used to toggle the main
   * activities viewpager swipe
   */
  @JavascriptInterface
  fun longClick(start: Boolean) {
    mainActivity?.contentBinding?.viewpager?.enableSwipe = !start
    if (web.frostWebClient.urlSupportsRefresh) {
      web.parent.swipeDisabledByAction = start
    }
  }

  /** Allow or disallow the pull down to refresh action */
  @JavascriptInterface
  fun disableSwipeRefresh(disable: Boolean) {
    if (!web.frostWebClient.urlSupportsRefresh) {
      return
    }
    web.parent.swipeDisabledByAction = disable
    if (disable) {
      // locked onto an input field; ensure content is visible
      mainActivity?.collapseAppBar()
    }
  }

  @JavascriptInterface
  fun loadLogin() {
    L.d { "Sign up button found; load login" }
    activity.ctxCoroutine.launch { fbCookie.logout(activity, deleteCookie = false) }
  }

  /** Launch image overlay */
  @JavascriptInterface
  fun loadImage(imageUrl: String, text: String?) {
    activity.launchImageActivity(imageUrl, text)
  }

  @JavascriptInterface
  fun emit(flag: Int) {
    web.post { web.frostWebClient.emit(flag) }
  }

  @JavascriptInterface
  fun isReady() {
    L.v { "JSI is ready" }
    refreshEmit(false)
  }

  @JavascriptInterface
  fun handleHtml(html: String?) {
    html ?: return
    web.post { web.frostWebClient.handleHtml(html) }
  }

  @JavascriptInterface
  fun handleHeader(html: String?) {
    html ?: return
    headerEmit?.invoke(html)
  }

  @JavascriptInterface
  fun allowHorizontalScrolling(enable: Boolean) {
    mainActivity?.contentBinding?.viewpager?.enableSwipe = enable
    webActivity?.swipeBack?.disallowIntercept = !enable
  }

  private var isScrolling = false

  @JavascriptInterface
  fun setScrolling(scrolling: Boolean) {
    L.v { "Scrolling $scrolling" }
    this.isScrolling = scrolling
  }

  @JavascriptInterface fun isScrolling(): Boolean = isScrolling
}
