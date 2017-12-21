package com.pitchedapps.frost.web

import com.pitchedapps.frost.activities.WebOverlayActivity
import com.pitchedapps.frost.activities.WebOverlayActivityBase
import com.pitchedapps.frost.activities.WebOverlayBasicActivity

import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.*
import com.pitchedapps.frost.views.FrostWebView
import org.jetbrains.anko.runOnUiThread

/**
 * Created by Allan Wang on 2017-08-15.
 *
 * Due to the nature of facebook href's, many links
 * cannot be resolved on a new window and must instead
 * by loaded in the current page
 * This helper method will collect all known cases and launch the overlay accordingly
 * Returns [true] (default) if action is consumed, [false] otherwise
 *
 * Note that this is not always called on the main thread!
 * UI related methods should always be posted or they may not be properly executed.
 *
 * If the request already comes from an instance of [WebOverlayActivity], we will then judge
 * whether the user agent string should be changed. All propagated results will return false,
 * as we have no need of sending a new intent to the same activity
 */
fun FrostWebView.requestWebOverlay(url: String): Boolean {
    val context = context // finalize reference
    if (url.isVideoUrl && context is VideoViewHolder) {
        L.i("Found video", url)
        context.runOnUiThread { context.showVideo(url) }
        return true
    }
    if (!url.isIndependent) {
        L.i("Forbid overlay switch", url)
        return false
    }
    if (!Prefs.overlayEnabled) return false
    if (context is WebOverlayActivityBase) {
        L.v("Check web request from overlay", url)
        //already overlay; manage user agent
        if (userAgentString != USER_AGENT_BASIC && url.formattedFbUrl.shouldUseBasicAgent) {
            L.i("Switch to basic agent overlay")
            context.launchWebOverlay(url, WebOverlayBasicActivity::class.java)
            return true
        }
        if (context is WebOverlayBasicActivity && !url.formattedFbUrl.shouldUseBasicAgent) {
            L.i("Switch from basic agent")
            context.launchWebOverlay(url)
            return true
        }
        L.i("return false switch")
        return false
    }
    L.v("Request web overlay passed", url)
    context.launchWebOverlay(url)
    return true
}

/**
 * If the url contains any one of the whitelist segments, switch to the chat overlay
 */
val messageWhitelist = setOf(FbItem.MESSAGES, FbItem.CHAT, FbItem.FEED_MOST_RECENT, FbItem.FEED_TOP_STORIES).map { it.url }.toSet()

val String.shouldUseBasicAgent
    get() = !contains("story.php") //we will use basic agent for anything that isn't a comment section
//    get() = (messageWhitelist.any { contains(it) }) || this == FB_URL_BASE