package com.pitchedapps.frost.services

import ca.allanwang.kau.kotlin.firstOrNull
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.pitchedapps.frost.facebook.requests.httpClient
import com.pitchedapps.frost.utils.L
import okhttp3.Request
import org.joda.time.DateTime

/**
 * Created by Allan Wang on 07/04/18.
 */
data class FrostRelease(val versionName: String,
                        val timestamp: Long,
                        val apk: FrostApkRelease? = null,
                        val category: String = "")

data class FrostApkRelease(val size: Long,
                           val name: String,
                           val url: String,
                           val timestamp: Long,
                           val downloadCount: Long = -1)

object UpdateManager {
    internal fun getLatestGithubRelease(): FrostRelease? {
        try {
            val data = getGithubReleaseJsonV3() ?: return null
            return parseGithubReleaseV3(data)
        } catch (e: Exception) {
            L.e(e) {
                "Failed to get github release"
            }
            return null
        }
    }

    private fun JsonNode.asMillis(): Long = DateTime(asText()).millis

    private fun getGithubReleaseJsonV3(): JsonNode? {
        val mapper = ObjectMapper()
        val response = httpClient.newCall(Request.Builder()
                .url("https://api.github.com/repos/AllanWang/Frost-for-Facebook/releases/latest")
                .get().build()).execute().body()?.string() ?: return null
        return mapper.readTree(response)
    }

    private fun parseGithubReleaseV3(data: JsonNode): FrostRelease? {
        val versionName = data.get("tag_name").asText()
        if (versionName.isEmpty()) return null
        val release = FrostRelease(
                versionName = versionName,
                timestamp = data.get("created_at").asMillis(),
                category = "Github")
        val assets = data.get("assets")
        if (!assets.isArray) return release
        val apkRelease = assets.elements().firstOrNull {
            it.get("content_type").asText().contains("android")
        } ?: return release
        val apk = FrostApkRelease(size = apkRelease.get("size").asLong(),
                name = apkRelease.get("name").asText(),
                url = apkRelease.get("browser_download_url").asText(),
                timestamp = apkRelease.get("updated_at").asMillis(),
                downloadCount = apkRelease.get("download_count").asLong())
        return release.copy(apk = apk)
    }
}
