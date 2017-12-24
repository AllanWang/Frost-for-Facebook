package com.pitchedapps.frost.facebook

/**
 * Created by Allan Wang on 21/12/17.
 *
 * Collection of regex matchers
 * Input text must be properly unescaped
 *
 * See [StringEscapeUtils]
 */

/**
 * Matches the fb_dtsg component of a page containing it as a hidden value
 */
val FB_DTSG_MATCHER: Regex by lazy { Regex("name=\"fb_dtsg\" value=\"(.*?)\"") }

/**
 * Matches user id from cookie
 */
val FB_USER_MATCHER: Regex by lazy { Regex("c_user=([0-9]*);") }

val FB_EPOCH_MATCHER: Regex by lazy { Regex(":([0-9]+)") }
val FB_NOTIF_ID_MATCHER: Regex by lazy { Regex("notif_id\":([0-9]+)") }
val FB_MESSAGE_NOTIF_ID_MATCHER: Regex by lazy { Regex("thread_fbid_([0-9]+)") }
val FB_PROFILE_PICTURE_MATCHER: Regex by lazy { Regex("url\\(\"(.*?)\"\\)") }

operator fun MatchResult?.get(groupIndex: Int) = this?.groupValues?.get(groupIndex)

