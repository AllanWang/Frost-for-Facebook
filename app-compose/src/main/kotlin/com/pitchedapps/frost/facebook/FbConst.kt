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

/** Created by Allan Wang on 2017-06-01. */
const val FACEBOOK_COM = "facebook.com"
const val MESSENGER_COM = "messenger.com"
const val FBCDN_NET = "fbcdn.net"
const val WWW_FACEBOOK_COM = "www.$FACEBOOK_COM"
const val WWW_MESSENGER_COM = "www.$MESSENGER_COM"
const val HTTPS_FACEBOOK_COM = "https://$WWW_FACEBOOK_COM"
const val HTTPS_MESSENGER_COM = "https://$WWW_MESSENGER_COM"
const val FACEBOOK_BASE_COM = "m.$FACEBOOK_COM"
const val FB_URL_BASE = "https://$FACEBOOK_BASE_COM/"
const val FACEBOOK_MBASIC_COM = "mbasic.$FACEBOOK_COM"
const val FB_URL_MBASIC_BASE = "https://$FACEBOOK_MBASIC_COM/"

fun profilePictureUrl(id: Long) = "https://graph.facebook.com/$id/picture?type=large"

const val FB_LOGIN_URL = "${FB_URL_BASE}login"
const val FB_HOME_URL = "${FB_URL_BASE}home.php"
const val MESSENGER_THREAD_PREFIX = "$HTTPS_MESSENGER_COM/t/"
