/*
 * Copyright 2021 Allan Wang
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

import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import com.google.common.flogger.FluentLogger
import com.pitchedapps.frost.ext.WebTargetId
import com.pitchedapps.frost.view.FrostWebView
import com.pitchedapps.frost.web.state.FrostWebStore
import com.pitchedapps.frost.web.state.get
import com.pitchedapps.frost.web.state.state.ContentState
import com.pitchedapps.frost.web.usecases.TabUseCases
import com.pitchedapps.frost.webview.FrostChromeClient
import com.pitchedapps.frost.webview.FrostWeb
import com.pitchedapps.frost.webview.FrostWebScoped
import com.pitchedapps.frost.webview.FrostWebViewClient
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import mozilla.components.lib.state.ext.flow
import mozilla.components.lib.state.ext.observeAsState

@FrostWebScoped
class FrostWebCompose
@Inject
internal constructor(
  @FrostWeb val tabId: WebTargetId,
  private val store: FrostWebStore,
  private val client: FrostWebViewClient,
  private val chromeClient: FrostChromeClient,
  private val tabUseCases: TabUseCases,
) {

  /**
   * Webview implementation in compose
   *
   * Based off of
   * https://github.com/google/accompanist/blob/main/web/src/main/java/com/google/accompanist/web/WebView.kt
   *
   * @param modifier A compose modifier
   * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
   *   the WebView back. navigation from outside the composable.
   * @param onCreated Called when the WebView is first created, this can be used to set additional
   *   settings on the WebView. WebChromeClient and WebViewClient should not be set here as they
   *   will be subsequently overwritten after this lambda is called.
   * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved if
   *   you need to save and restore state in this WebView.
   */
  @Composable
  fun WebView(
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
  ) {

    var webView by remember { mutableStateOf<WebView?>(null) }

    webView?.let { wv ->
      val lifecycleOwner = LocalLifecycleOwner.current

      val canGoBack by
        store.observeAsState(initialValue = false) { it[tabId]?.content?.canGoBack == true }

      BackHandler(captureBackPresses && canGoBack) { tabUseCases.requests.goBack(tabId) }

      LaunchedEffect(wv, store) {
        fun storeFlow(action: suspend Flow<ContentState>.() -> Unit) = launch {
          store.flow(lifecycleOwner).mapNotNull { it[tabId]?.content }.action()
        }

        storeFlow {
          mapNotNull { it.transientState.targetUrl }
            .distinctUntilChanged()
            .collect { url ->
              tabUseCases.responses.respondUrl(tabId, url)
              wv.loadUrl(url)
            }
        }
        storeFlow {
          mapNotNull { it.transientState.navStep }
            .distinctUntilChanged()
            .filter { it != 0 }
            .collect { steps ->
              tabUseCases.responses.respondSteps(tabId, steps)
              if (wv.canGoBackOrForward(steps)) {
                wv.goBackOrForward(steps)
              } else {
                logger.atWarning().log("web %s cannot go back %d steps", tabId, steps)
              }
            }
        }
      }
    }

    AndroidView(
      factory = { context ->
        val childView =
          FrostWebView(context)
            .apply {
              onCreated(this)

              logger.atInfo().log("Created webview for %s", tabId)

              this.layoutParams =
                FrameLayout.LayoutParams(
                  FrameLayout.LayoutParams.MATCH_PARENT,
                  FrameLayout.LayoutParams.MATCH_PARENT,
                )

              //        state.viewState?.let {
              //          this.restoreState(it)
              //        }

              webChromeClient = chromeClient
              webViewClient = client

              val url = store.state[tabId]?.content?.url
              if (url != null) loadUrl(url)
            }
            .also { webView = it }

        val parentLayout = NestedScrollView(context)
        parentLayout.layoutParams =
          FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
          )
        parentLayout.addView(childView)

        parentLayout
      },
      modifier = modifier,
      onRelease = { parentFrame ->
        val wv = parentFrame.getChildAt(0) as WebView
        onDispose(wv)
        // We don't want to release webviews while the activity is active as they take time to be
        // recreated (+ loading time)
        // Keeping is fine since we should have < 10 webviews active in the entire app.
        // Release should only happen after the main screen is complete
        logger.atInfo().log("Released webview for %s", tabId)
      },
    )
  }

  companion object {
    private val logger = FluentLogger.forEnclosingClass()
  }
}
