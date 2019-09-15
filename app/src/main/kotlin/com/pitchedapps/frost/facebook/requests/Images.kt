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
package com.pitchedapps.frost.facebook.requests

import com.pitchedapps.frost.facebook.FB_REDIRECT_URL_MATCHER
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.utils.L
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Created by Allan Wang on 29/12/17.
 */

/**
 * Attempts to get the fbcdn url of the supplied image redirect url
 */
suspend fun String.getFullSizedImageUrl(url: String, timeout: Long = 3000): String? =
    withContext(Dispatchers.IO) {
        try {
            withTimeout(timeout) {
                val redirect = requestBuilder().url(url).get().call()
                    .execute().body()?.string() ?: return@withTimeout null
                FB_REDIRECT_URL_MATCHER.find(redirect)[1]?.formattedFbUrl
            }
        } catch (e: Exception) {
            L.e(e) { "Failed to load full size image url" }
            null
        }
    }
