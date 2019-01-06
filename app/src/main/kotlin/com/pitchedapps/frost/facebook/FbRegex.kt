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
val FB_REV_MATCHER: Regex by lazy { Regex("\"app_version\":\"(.*?)\"") }

/**
 * Matches user id from cookie
 */
val FB_USER_MATCHER: Regex = Regex("c_user=([0-9]*);")

val FB_EPOCH_MATCHER: Regex = Regex(":([0-9]+)")
val FB_NOTIF_ID_MATCHER: Regex = Regex("notif_([0-9]+)")
val FB_MESSAGE_NOTIF_ID_MATCHER: Regex = Regex("(?:thread|user)_fbid_([0-9]+)")
val FB_CSS_URL_MATCHER: Regex = Regex("url\\([\"|']?(.*?)[\"|']?\\)")
val FB_JSON_URL_MATCHER: Regex = Regex("\"(http.*?)\"")
val FB_IMAGE_ID_MATCHER: Regex = Regex("fbcdn.*?/[0-9]+_([0-9]+)_")
val FB_REDIRECT_URL_MATCHER: Regex = Regex("url=(.*?fbcdn.*?)\"")

operator fun MatchResult?.get(groupIndex: Int) = this?.groupValues?.get(groupIndex)
