package com.pitchedapps.frost.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.kotlin.firstOrNull
import ca.allanwang.kau.utils.string
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.facebook.requests.httpClient
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.ReleasePrefs
import com.pitchedapps.frost.utils.frostEvent
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.joda.time.DateTime
import java.util.concurrent.Future

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

class UpdateService : JobService() {

    private var future: Future<Unit>? = null

    private val startTime = System.currentTimeMillis()

    override fun onStopJob(params: JobParameters?): Boolean {
        val time = System.currentTimeMillis() - startTime
        L.d { "Update service has finished abruptly in $time ms" }
        frostEvent("UpdateTime",
                "Type" to "Service force stop",
                "Duration" to time)
        future?.cancel(true)
        future = null
        return false
    }

    fun finish(params: JobParameters?) {
        val time = System.currentTimeMillis() - startTime
        L.i { "Update service has finished in $time ms" }
        frostEvent("UpdateTime",
                "Type" to "Service",
                "Duration" to time)
        jobFinished(params, false)
        future?.cancel(true)
        future = null
    }

    override fun onStartJob(params: JobParameters?): Boolean {
//        L.i { "Fetching update" }
//        future = doAsync {
//            fetch()
//            finish(params)
//        }
//        return true
        return false
    }

    private fun fetch() {
        val release = UpdateManager.getLatestGithubRelease() ?: return
        val timestamp = release.apk?.timestamp ?: return
        if (ReleasePrefs.lastTimeStamp >= timestamp) return
        ReleasePrefs.lastTimeStamp = timestamp
        if (BuildConfig.VERSION_NAME.contains(release.apk.name)) return
        updateNotification(release)
    }

    private fun updateNotification(release: FrostRelease) {
        val notifBuilder = frostNotification(NOTIF_CHANNEL_UPDATES)
                .setFrostAlert(true, Prefs.notificationRingtone)
                .setContentTitle(string(R.string.frost_name))
                .setContentText(string(R.string.update_notif_message))
        NotificationManagerCompat.from(this).notify(release.versionName.hashCode(), notifBuilder.build())
    }

}

const val UPDATE_PERIODIC_JOB = 7

fun Context.scheduleUpdater(enable: Boolean): Boolean =
        scheduleJob<UpdateService>(UPDATE_PERIODIC_JOB, if (enable) 1440 else -1)

const val UPDATE_JOB_NOW = 6

fun Context.fetchUpdates(): Boolean =
        fetchJob<UpdateService>(UPDATE_JOB_NOW)