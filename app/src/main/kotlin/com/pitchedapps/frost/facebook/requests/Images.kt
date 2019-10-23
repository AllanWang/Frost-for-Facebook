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

import android.os.Parcelable
import com.pitchedapps.frost.facebook.FB_FBCDN_ID_MATCHER
import com.pitchedapps.frost.facebook.FB_PHOTO_ID_MATCHER
import com.pitchedapps.frost.facebook.FB_REDIRECT_URL_MATCHER
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.facebook.get
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostJsoup
import com.pitchedapps.frost.utils.isIndirectImageUrl
import kotlinx.android.parcel.Parcelize
import org.jsoup.nodes.Element

/**
 * Created by Allan Wang on 29/12/17.
 */

/**
 * Attempts to get the fbcdn url of the supplied image redirect url
 */
fun String?.getFullSizedImageUrl(url: String): String {
    if (this == null || !url.isIndirectImageUrl) return url
    val redirect = try {
        requestBuilder().url(url).get().call()
            .execute().body()?.string()
    } catch (e: Exception) {
        null
    } ?: return url
    return FB_REDIRECT_URL_MATCHER.find(redirect)[1]?.formattedFbUrl ?: url
}

@Parcelize
data class FbImageData(
    val current: String,
    val url: String,
    val prev: String? = null,
    val next: String? = null
) :
    Parcelable {
    companion object {
        fun imageContextUrl(id: String) = "https://mbasic.facebook.com/photo.php?fbid=$id"
        fun fullSizeImageUrl(id: String) =
            "https://mbasic.facebook.com/photo/view_full_size/?fbid=$id"

        fun urlImageId(url: String): String? =
            FB_FBCDN_ID_MATCHER.find(url)[1] ?: FB_PHOTO_ID_MATCHER.find(url)[1]
    }
}

fun String?.getImageData(id: String): FbImageData {
    val url = getFullSizedImageUrl(FbImageData.fullSizeImageUrl(id))
    fun fallback() = FbImageData(current = id, url = url)
    if (this == null) return fallback()
    val doc = try {
        frostJsoup(url = FbImageData.imageContextUrl(id), cookie = this)
    } catch (e: Exception) {
        L.e { "Failed to get image data" }
        return fallback()
    }

    /**
     * Gets url from td entry
     */
    fun Element.adjacentId(): String? {
        val adjacentUrl = selectFirst("a")?.attr("href") ?: return null
        return FbImageData.urlImageId(adjacentUrl)
    }

    val adjacent =
        doc.selectFirst("table.v")
            ?.select("td")
            ?.takeIf { it.size == 2 }
            ?.mapNotNull { it.adjacentId() }
            ?.takeIf { it.size == 2 }
            ?: return fallback()

    return FbImageData(current = id, url = url, prev = adjacent[0], next = adjacent[1])
}