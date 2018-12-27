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

import android.webkit.JavascriptInterface
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.contracts.MainActivityContract
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.WebContext
import com.pitchedapps.frost.utils.cookies
import com.pitchedapps.frost.utils.isIndependent
import com.pitchedapps.frost.utils.launchImageActivity
import com.pitchedapps.frost.utils.showWebContextMenu
import com.pitchedapps.frost.views.FrostWebView
import io.reactivex.subjects.Subject
import kotlinx.coroutines.channels.SendChannel

/**
 * Created by Allan Wang on 2017-06-01.
 */
class FrostJSI(val web: FrostWebView) {

    private val context = web.context
    private val activity = context as? MainActivity
    private val header: Subject<String>? = activity?.headerBadgeObservable
    private val refresh: SendChannel<Boolean> = web.parent.refreshChannel
    private val cookies = activity?.cookies() ?: arrayListOf()

    /**
     * Attempts to load the url in an overlay
     * Returns {@code true} if successful, meaning the event is consumed,
     * or {@code false} otherwise, meaning the event should be propagated
     */
    @JavascriptInterface
    fun loadUrl(url: String?): Boolean = if (url == null) false else web.requestWebOverlay(url)

    @JavascriptInterface
    fun loadVideo(url: String?, isGif: Boolean): Boolean =
        if (url != null && Prefs.enablePip) {
            web.post {
                (context as? VideoViewHolder)?.showVideo(url, isGif)
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
    fun contextMenu(url: String, text: String?) {
        if (!text.isIndependent) return
        //url will be formatted through webcontext
        web.post { context.showWebContextMenu(WebContext(url, text)) }
    }

    /**
     * Get notified when a stationary long click starts or ends
     * This will be used to toggle the main activities viewpager swipe
     */
    @JavascriptInterface
    fun longClick(start: Boolean) {
        activity?.viewPager?.enableSwipe = !start
        web.parent.swipeEnabled = !start
    }

    /**
     * Allow or disallow the pull down to refresh action
     */
    @JavascriptInterface
    fun disableSwipeRefresh(disable: Boolean) {
        web.parent.swipeEnabled = !disable
        if (disable) {
            // locked onto an input field; ensure content is visible
            (context as? MainActivityContract)?.collapseAppBar()
        }
    }

    @JavascriptInterface
    fun loadLogin() {
        FbCookie.logout(context)
    }

    /**
     * Launch image overlay
     */
    @JavascriptInterface
    fun loadImage(imageUrl: String, text: String?) {
        context.launchImageActivity(imageUrl, text)
    }

    @JavascriptInterface
    fun emit(flag: Int) {
        web.post { web.frostWebClient.emit(flag) }
    }

    @JavascriptInterface
    fun isReady() {
        refresh.offer(false)
    }

    @JavascriptInterface
    fun handleHtml(html: String?) {
        html ?: return
        web.post { web.frostWebClient.handleHtml(html) }
    }

    @JavascriptInterface
    fun handleHeader(html: String?) {
        html ?: return
        header?.onNext(html)
    }
}
