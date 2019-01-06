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

import com.pitchedapps.frost.facebook.FB_URL_BASE
import okhttp3.Call

/**
 * Created by Allan Wang on 07/01/18.
 */
fun RequestAuth.sendMessage(group: String, content: String): FrostRequest<Boolean> {

    // todo test more; only tested against tids=cid...
    val body = listOf(
        "tids" to group,
        "body" to content,
        "fb_dtsg" to fb_dtsg,
        "__user" to userId
    ).withEmptyData("m_sess", "__dyn", "__req", "__ajax__")

    return frostRequest(::validateMessage) {
        url("${FB_URL_BASE}messages/send")
        post(body.toForm())
    }
}

/**
 * Messages are a bit weird with their responses
 */
private fun validateMessage(call: Call): Boolean {
    val body = call.execute().body() ?: return false
    // todo
    return true
}
