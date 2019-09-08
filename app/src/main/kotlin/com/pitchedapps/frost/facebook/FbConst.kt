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
 * Created by Allan Wang on 2017-06-01.
 */

const val FACEBOOK_COM = "facebook.com"
const val FBCDN_NET = "fbcdn.net"
const val HTTPS_FACEBOOK_COM = "https://$FACEBOOK_COM/"
const val FB_URL_BASE = "https://m.$FACEBOOK_COM/"
fun profilePictureUrl(id: Long) = "https://graph.facebook.com/$id/picture?type=large"
const val FB_LOGIN_URL = "${FB_URL_BASE}login"
const val FB_HOME_URL = "${FB_URL_BASE}home.php"

/*
 * User agent candidates.
 * For those building from source, you can feel free to set the used agent to one of these options.
 * Following https://github.com/AllanWang/Frost-for-Facebook/pull/1531, we do not support multiple
 * agents per login session.
 */

// Default user agent
private const val USER_AGENT_MOBILE_CONST =
    "Mozilla/5.0 (Linux; Android 8.0.0; ONEPLUS A3000) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.90 Mobile Safari/537.36"
// Desktop agent, for pages like messages
private const val USER_AGENT_DESKTOP_CONST =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.90 Safari/537.36"

const val USER_AGENT = USER_AGENT_DESKTOP_CONST

/**
 * Animation transition delay, just to ensure that the styles
 * have properly set in
 */
const val WEB_LOAD_DELAY = 50L
/**
 * Additional delay for transition when called from commit.
 * Note that transitions are also called from onFinish, so this value
 * will never make a load slower than it is
 */
const val WEB_COMMIT_LOAD_DELAY = 200L
