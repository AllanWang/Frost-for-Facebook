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

import ca.allanwang.kau.utils.runOnUiThread
import com.pitchedapps.frost.activities.WebOverlayActivity
import com.pitchedapps.frost.activities.WebOverlayActivityBase
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.facebook.FbItem
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.isImageUrl
import com.pitchedapps.frost.utils.isIndependent
import com.pitchedapps.frost.utils.isIndirectImageUrl
import com.pitchedapps.frost.utils.isVideoUrl
import com.pitchedapps.frost.utils.launchImageActivity
import com.pitchedapps.frost.utils.launchWebOverlay
import com.pitchedapps.frost.views.FrostWebView

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
    L.v { "Request web overlay: $url" }
    val context = context // finalize reference
    if (url.isVideoUrl && context is VideoViewHolder) {
        L.d { "Found video through overlay" }
        context.runOnUiThread { context.showVideo(url.formattedFbUrl) }
        return true
    }
    if (url.isImageUrl) {
        L.d { "Found fb image" }
        context.launchImageActivity(url.formattedFbUrl)
        return true
    }
    if (url.isIndirectImageUrl) {
        L.d { "Found indirect fb image" }
        context.launchImageActivity(url.formattedFbUrl, cookie = FbCookie.webCookie)
        return true
    }
    if (!url.isIndependent) {
        L.d { "Forbid overlay switch" }
        return false
    }
    if (!Prefs.overlayEnabled) return false
    if (context is WebOverlayActivityBase) return false
    L.v { "Request web overlay passed" }
    context.launchWebOverlay(url)
    return true
}

/**
 * If the url contains any one of the whitelist segments, switch to the chat overlay
 */
val messageWhitelist: Set<String> =
    setOf(FbItem.MESSAGES, FbItem.CHAT, FbItem.FEED_MOST_RECENT, FbItem.FEED_TOP_STORIES)
        .mapTo(mutableSetOf(), FbItem::url)

@Deprecated(message = "Should not be used in production as we only support one user agent at a time.")
val String.shouldUseDesktopAgent: Boolean
    get() = when {
        contains("story.php") -> false // do not use desktop for comment section
        contains("/events/") -> false // do not use for events (namely the map)
        contains("/messages") -> true // must use for messages
        else -> false // default to normal user agent
    }
