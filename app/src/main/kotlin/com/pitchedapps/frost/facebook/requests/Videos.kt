package com.pitchedapps.frost.facebook.requests

import com.pitchedapps.frost.facebook.USER_AGENT_HD_CONST
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.frostJsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

suspend fun String.getHdVideoUrl(url: String, timeout: Long = 3000): String? =
    withContext(Dispatchers.IO) {
        try {
            withTimeout(timeout) {
                val document =
                    frostJsoup(this@getHdVideoUrl, url = url, userAgent = USER_AGENT_HD_CONST)

                null
            }
        } catch (e: Exception) {
            L.e(e) { "Failed to load full size image url" }
            null
        }
    }