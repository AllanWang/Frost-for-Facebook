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