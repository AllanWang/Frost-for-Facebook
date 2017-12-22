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