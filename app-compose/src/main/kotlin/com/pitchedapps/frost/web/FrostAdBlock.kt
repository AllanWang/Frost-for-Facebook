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
package com.pitchedapps.frost.web

import android.text.TextUtils
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

interface FrostAdBlock {

  val data: Set<String>

  /**
   * Initialize ad block data.
   *
   * Required to be called once
   */
  fun init()
}

fun FrostAdBlock.isAd(url: String?): Boolean {
  url ?: return false
  val httpUrl = url.toHttpUrlOrNull() ?: return false
  return isAdHost(httpUrl.host)
}

tailrec fun FrostAdBlock.isAdHost(host: String): Boolean {
  if (TextUtils.isEmpty(host)) return false
  val index = host.indexOf(".")
  if (index < 0 || index + 1 < host.length) return false
  if (data.contains(host)) return true
  return isAdHost(host.substring(index + 1))
}
