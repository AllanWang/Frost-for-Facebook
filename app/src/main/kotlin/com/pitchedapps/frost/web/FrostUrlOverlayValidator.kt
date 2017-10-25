package com.pitchedapps.frost.web

import com.pitchedapps.frost.activities.WebOverlayActivity
import com.pitchedapps.frost.activities.WebOverlayActivityBase
import com.pitchedapps.frost.activities.WebOverlayBasicActivity
import com.pitchedapps.frost.facebook.FB_URL_BASE
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.USER_AGENT_BASIC
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.isFacebookUrl
import com.pitchedapps.frost.utils.launchWebOverlay

/**
 * Created by Allan Wang on 2017-08-15.
 *
 * Due to the nature of facebook href's, many links
 * cannot be resolved on a new window and must instead
 * by loaded in the current page
 * This helper method will collect all known cases and launch the overlay accordingly
 * Returns {@code true} (default) if action is consumed, {@code false} otherwise
 *
 * If the request already comes from an instance of [WebOverlayActivity], we will then judge
 * whether the user agent string should be changed. All propagated results will return false,
 * as we have no need of sending a new intent to the same activity
 */
fun FrostWebViewCore.requestWebOverlay(url: String): Boolean {
    if (url == "#") return false
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
    /*
     * Non facebook urls can be loaded
     */
    if (!url.formattedFbUrl.isFacebookUrl) {
        context.launchWebOverlay(url)
        L.d("Request web overlay is not a facebook url", url)
        return true
    }
    /*
     * Check blacklist
     */
    if (overlayBlacklist.any { url.contains(it) }) return false
    /*
     * Facebook messages have the following cases for the tid query
     * mid* or id* for newer threads, which can be launched in new windows
     * or a hash for old threads, which must be loaded on old threads
     */
    if (url.contains("/messages/read/?tid=")) {
        if (!url.contains("?tid=id") && !url.contains("?tid=mid")) return false
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
    get() = (messageWhitelist.any { contains(it) }) || this == FB_URL_BASE

/**
 * The following components should never be launched in a new overlay
 */
val overlayBlacklist = setOf("messages/?pageNum", "photoset_token", "sharer.php")