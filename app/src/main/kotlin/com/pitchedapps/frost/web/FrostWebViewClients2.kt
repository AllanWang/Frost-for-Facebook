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

import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebView
import ca.allanwang.kau.utils.withAlpha
import com.pitchedapps.frost.enums.ThemeCategory
import com.pitchedapps.frost.injectors.JsActions
import com.pitchedapps.frost.injectors.JsAssets
import com.pitchedapps.frost.injectors.jsInject
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.isFacebookUrl
import com.pitchedapps.frost.utils.isMessengerUrl
import com.pitchedapps.frost.utils.launchImageActivity
import com.pitchedapps.frost.views.FrostWebView
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import javax.inject.Inject
import javax.inject.Qualifier

/**
 * Created by Allan Wang on 2017-05-31.
 *
 * Collection of webview clients
 */
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class FrostWebClient

@EntryPoint
@InstallIn(FrostWebComponent::class)
interface FrostWebClientEntryPoint {

  @FrostWebScoped @FrostWebClient fun webClient(): FrostWebViewClient
}

@Module
@InstallIn(FrostWebComponent::class)
interface FrostWebViewClientModule {
  @Binds @FrostWebClient fun webClient(binds: FrostWebViewClient2): FrostWebViewClient
}

/** The default webview client */
open class FrostWebViewClient2
@Inject
constructor(web: FrostWebView, @FrostRefresh private val refreshEmit: FrostEmitter<Boolean>) :
  FrostWebViewClient(web) {

  init {
    L.i { "Refresh web client 2" }
  }

  override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
    super.doUpdateVisitedHistory(view, url, isReload)
    urlSupportsRefresh = urlSupportsRefresh(url)
    web.parent.swipeAllowedByPage = urlSupportsRefresh
    view.jsInject(JsAssets.AUTO_RESIZE_TEXTAREA.maybe(prefs.autoExpandTextBox), prefs = prefs)
    v { "History $url; refresh $urlSupportsRefresh" }
  }

  private fun urlSupportsRefresh(url: String?): Boolean {
    if (url == null) return false
    if (url.isMessengerUrl) return false
    if (!url.isFacebookUrl) return true
    if (url.contains("soft=composer")) return false
    if (url.contains("sharer.php") || url.contains("sharer-dialog.php")) return false
    return true
  }

  private fun WebView.facebookJsInject() {
    jsInject(*facebookJsInjectors.toTypedArray(), prefs = prefs)
  }

  private fun WebView.messengerJsInject() {
    jsInject(themeProvider.injector(ThemeCategory.MESSENGER), prefs = prefs)
  }

  override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
    super.onPageStarted(view, url, favicon)
    if (url == null) return
    v { "loading $url ${web.settings.userAgentString}" }
    refreshEmit(true)
  }

  private fun injectBackgroundColor() {
    web.setBackgroundColor(
      when {
        isMain -> Color.TRANSPARENT
        web.url.isFacebookUrl -> themeProvider.bgColor.withAlpha(255)
        else -> Color.WHITE
      }
    )
  }

  override fun onPageCommitVisible(view: WebView, url: String?) {
    super.onPageCommitVisible(view, url)
    injectBackgroundColor()
    when {
      url.isFacebookUrl -> {
        v { "FB Page commit visible" }
        view.facebookJsInject()
      }
      url.isMessengerUrl -> {
        v { "Messenger Page commit visible" }
        view.messengerJsInject()
      }
      else -> {
        refreshEmit(false)
      }
    }
  }

  override fun onPageFinished(view: WebView, url: String?) {
    url ?: return
    v { "finished $url" }
    if (!url.isFacebookUrl && !url.isMessengerUrl) {
      refreshEmit(false)
      return
    }
    onPageFinishedActions(url)
  }

  internal override fun injectAndFinish() {
    v { "page finished reveal" }
    refreshEmit(false)
    injectBackgroundColor()
    when {
      web.url.isFacebookUrl -> {
        web.jsInject(
          JsActions.LOGIN_CHECK,
          JsAssets.TEXTAREA_LISTENER,
          JsAssets.HEADER_BADGES.maybe(isMain),
          prefs = prefs
        )
        web.facebookJsInject()
      }
      web.url.isMessengerUrl -> {
        web.messengerJsInject()
      }
    }
  }

  /**
   * Helper to format the request and launch it returns true to override the url returns false if we
   * are already in an overlaying activity
   */
  private fun launchRequest(request: WebResourceRequest): Boolean {
    v { "Launching url: ${request.url}" }
    return web.requestWebOverlay(request.url.toString())
  }

  private fun launchImage(url: String, text: String? = null, cookie: String? = null): Boolean {
    v { "Launching image: $url" }
    web.context.launchImageActivity(url, text, cookie)
    if (web.canGoBack()) web.goBack()
    return true
  }
}

private const val EMIT_THEME = 0b1
private const val EMIT_ID = 0b10
private const val EMIT_COMPLETE = EMIT_THEME or EMIT_ID
private const val EMIT_FINISH = 0
