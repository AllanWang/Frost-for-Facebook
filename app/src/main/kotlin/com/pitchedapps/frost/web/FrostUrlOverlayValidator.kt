package com.pitchedapps.frost.web

import android.content.Context
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
 * Returns {@code true} (default) if overlay is launcher, {@code false} otherwise
 */
fun Context.requestWebOverlay(url: String): Boolean {
    if (url == "#") return false
    /*
     * Non facebook urls can be loaded
     */
    if (!url.formattedFbUrl.isFacebookUrl) {
        launchWebOverlay(url)
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
    launchWebOverlay(url)
    return true
}

/**
 * The following components should never be launched in a new overlay
 */
val overlayBlacklist = setOf("messages/?pageNum", "photoset_token")