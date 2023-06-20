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
package com.pitchedapps.frost.compose.webview

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.facebook.FACEBOOK_BASE_COM
import com.pitchedapps.frost.facebook.WWW_FACEBOOK_COM
import com.pitchedapps.frost.facebook.isExplicitIntent
import com.pitchedapps.frost.web.FrostWebHelper
import com.pitchedapps.frost.web.FrostWebStore
import com.pitchedapps.frost.web.UpdateNavigationAction
import com.pitchedapps.frost.web.UpdateProgressAction
import com.pitchedapps.frost.web.UpdateTitleAction
import java.io.ByteArrayInputStream
import javax.inject.Inject

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of webview clients
 */

/** The base of all webview clients Used to ensure that resources are properly intercepted */
abstract class BaseWebViewClient : WebViewClient() {

  protected abstract val webHelper: FrostWebHelper

  override fun shouldInterceptRequest(
    view: WebView,
    request: WebResourceRequest
  ): WebResourceResponse? {
    val requestUrl = request.url?.toString() ?: return null
    return if (webHelper.shouldInterceptUrl(requestUrl)) BLANK_RESOURCE else null
  }

  companion object {
    val BLANK_RESOURCE =
      WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
  }
}

/** The default webview client */
class FrostWebViewClient
@Inject
internal constructor(private val store: FrostWebStore, override val webHelper: FrostWebHelper) :
  BaseWebViewClient() {

  /** True if current url supports refresh. See [doUpdateVisitedHistory] for updates */
  internal var urlSupportsRefresh: Boolean = true

  override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
    super.doUpdateVisitedHistory(view, url, isReload)
    urlSupportsRefresh = webHelper.allowUrlSwipeToRefresh(url)
    store.dispatch(
      UpdateNavigationAction(
        canGoBack = view.canGoBack(),
        canGoForward = view.canGoForward(),
      ),
    )
    //    web.parent.swipeAllowedByPage = urlSupportsRefresh
    //    view.jsInject(JsAssets.AUTO_RESIZE_TEXTAREA.maybe(prefs.autoExpandTextBox), prefs = prefs)
    //    v { "History $url; refresh $urlSupportsRefresh" }
  }

  /** Main injections for facebook content */
  //  protected open val facebookJsInjectors: List<InjectorContract> =
  //    listOf(
  //      //                    CssHider.CORE,
  //      CssHider.HEADER,
  //      CssHider.COMPOSER.maybe(!prefs.showComposer),
  //      CssHider.STORIES.maybe(!prefs.showStories),
  //      CssHider.PEOPLE_YOU_MAY_KNOW.maybe(!prefs.showSuggestedFriends),
  //      CssHider.SUGGESTED_GROUPS.maybe(!prefs.showSuggestedGroups),
  //      CssHider.SUGGESTED_POSTS.maybe(!prefs.showSuggestedPosts),
  //      themeProvider.injector(ThemeCategory.FACEBOOK),
  //      CssHider.NON_RECENT.maybe(
  //        (web.url?.contains("?sk=h_chr") ?: false) && prefs.aggressiveRecents
  //      ),
  //      CssHider.ADS,
  //      CssHider.POST_ACTIONS.maybe(!prefs.showPostActions),
  //      CssHider.POST_REACTIONS.maybe(!prefs.showPostReactions),
  //      CssAsset.FullSizeImage.maybe(prefs.fullSizeImage),
  //      JsAssets.DOCUMENT_WATCHER,
  //      JsAssets.HORIZONTAL_SCROLLING,
  //      JsAssets.AUTO_RESIZE_TEXTAREA.maybe(prefs.autoExpandTextBox),
  //      JsAssets.CLICK_A,
  //      JsAssets.CONTEXT_A,
  //      JsAssets.MEDIA,
  //      JsAssets.SCROLL_STOP,
  //    )
  //
  //  private fun WebView.facebookJsInject() {
  //    jsInject(*facebookJsInjectors.toTypedArray(), prefs = prefs)
  //  }
  //
  //  private fun WebView.messengerJsInject() {
  //    jsInject(themeProvider.injector(ThemeCategory.MESSENGER), prefs = prefs)
  //  }

  override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
    super.onPageStarted(view, url, favicon)
    store.dispatch(UpdateProgressAction(0))
    store.dispatch(UpdateTitleAction(null))
    //    v { "loading $url ${web.settings.userAgentString}" }
    //        refresh.offer(true)
  }

  //  private fun injectBackgroundColor() {
  //    web?.setBackgroundColor(
  //      when {
  //        isMain -> Color.TRANSPARENT
  //        web.url.isFacebookUrl -> themeProvider.bgColor.withAlpha(255)
  //        else -> Color.WHITE
  //      }
  //    )
  //  }

  //  override fun onPageCommitVisible(view: WebView, url: String?) {
  //    super.onPageCommitVisible(view, url)
  //    injectBackgroundColor()
  //    when {
  //      url.isFacebookUrl -> {
  //        v { "FB Page commit visible" }
  //        view.facebookJsInject()
  //      }
  //      url.isMessengerUrl -> {
  //        v { "Messenger Page commit visible" }
  //        view.messengerJsInject()
  //      }
  //      else -> {
  //        //                refresh.offer(false)
  //      }
  //    }
  //  }

  override fun onPageFinished(view: WebView, url: String) {
    //    if (!url.isFacebookUrl && !url.isMessengerUrl) {
    //            refresh.offer(false)
    //      return
    //    }
    //    onPageFinishedActions(url)
  }

  //  internal open fun onPageFinishedActions(url: String) {
  //    if (url.startsWith("${FbItem.Messages.url}/read/") && prefs.messageScrollToBottom) {
  //      web.pageDown(true)
  //    }
  //    injectAndFinish()
  //  }

  // Temp open
  //  internal open fun injectAndFinish() {
  //    v { "page finished reveal" }
  //    //        refresh.offer(false)
  //    injectBackgroundColor()
  //    when {
  //      web.url.isFacebookUrl -> {
  //        web.jsInject(
  //          JsActions.LOGIN_CHECK,
  //          JsAssets.TEXTAREA_LISTENER,
  //          JsAssets.HEADER_BADGES.maybe(isMain),
  //          prefs = prefs
  //        )
  //        web.facebookJsInject()
  //      }
  //      web.url.isMessengerUrl -> {
  //        web.messengerJsInject()
  //      }
  //    }
  //  }

  fun handleHtml(html: String?) {
    logger.atFine().log("Handle html: %s", html)
  }

  fun emit(flag: Int) {
    logger.atInfo().log("Emit %d", flag)
  }

  /**
   * Helper to format the request and launch it returns true to override the url returns false if we
   * are already in an overlaying activity
   */
  //  private fun WebView.launchRequest(request: WebResourceRequest): Boolean {
  //    v { "Launching url: ${request.url}" }
  //    return requestWebOverlay(request.url.toString())
  //  }

  //  private fun launchImage(url: String, text: String? = null, cookie: String? = null): Boolean {
  //    v { "Launching image: $url" }
  //    web.context.launchImageActivity(url, text, cookie)
  //    if (web.canGoBack()) web.goBack()
  //    return true
  //  }

  override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
    logger.atFinest().log("Url loading: %s", request.url)
    val path = request.url?.path ?: return super.shouldOverrideUrlLoading(view, request)
    logger.atFinest().log("Url path: %s", path)
    val url = request.url.toString()
    if (url.isExplicitIntent) {
      //      view.context.startActivityForUri(request.url)
      return true
    }
    //    if (path.startsWith("/composer/")) {
    //      return launchRequest(request)
    //    }
    //    if (url.isIndirectImageUrl) {
    //      return launchImage(url.formattedFbUrl, cookie = fbCookie.webCookie)
    //    }
    //    if (url.isImageUrl) {
    //      return launchImage(url.formattedFbUrl)
    //    }
    //    if (prefs.linksInDefaultApp && view.context.startActivityForUri(request.url)) {
    //      return true
    //    }
    // Convert desktop urls to mobile ones
    if (url.contains("https://www.facebook.com") && webHelper.allowUrlSwipeToRefresh(url)) {
      view.loadUrl(url.replace(WWW_FACEBOOK_COM, FACEBOOK_BASE_COM))
      return true
    }
    return super.shouldOverrideUrlLoading(view, request)
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}

private const val EMIT_THEME = 0b1
private const val EMIT_ID = 0b10
private const val EMIT_COMPLETE = EMIT_THEME or EMIT_ID
private const val EMIT_FINISH = 0
