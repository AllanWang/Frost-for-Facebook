/*
 * Copyright 2023 Allan Wang
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
package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.facebook.FbUrlFormatter.Companion.VIDEO_REDIRECT
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
