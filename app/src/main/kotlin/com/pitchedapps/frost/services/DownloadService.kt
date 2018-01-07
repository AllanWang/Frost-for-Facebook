package com.pitchedapps.frost.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.utils.copyFromInputStream
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.createMediaFile
import com.pitchedapps.frost.utils.frostUriFromFile
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.*
import org.jetbrains.anko.toast
import java.io.File

/**
 * Created by Allan Wang on 2017-08-08.
 *
 * Not in use
 *
 * Background file downloader
 * All we are given is a link and a mime type
 *
 * With reference to the <a href="https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java">OkHttp3 sample</a>
 */
@SuppressLint("Registered")
class DownloadService : IntentService("FrostVideoDownloader") {

    companion object {
        const val EXTRA_URL = "download_url"
        private const val MAX_PROGRESS = 1000
        private const val DOWNLOAD_GROUP = "frost_downloads"
    }

    val client: OkHttpClient by lazy { initClient() }

    val start = System.currentTimeMillis()
    var totalSize = 0L
    val downloaded = mutableSetOf<String>()

    private lateinit var notifBuilder: NotificationCompat.Builder
    private var notifId: Int = -1

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.flags == PendingIntent.FLAG_CANCEL_CURRENT) {
            L.i { "Cancelling download service" }
            cancelDownload()
            return Service.START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        val url: String = intent?.getStringExtra(EXTRA_URL) ?: return

        if (downloaded.contains(url)) return

        val request: Request = Request.Builder()
                .url(url)
                .build()

        notifBuilder = frostNotification
        notifId = Math.abs(url.hashCode() + System.currentTimeMillis().toInt())
        val cancelIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        notifBuilder.setContentTitle(string(R.string.downloading_video))
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setWhen(System.currentTimeMillis())
                .setProgress(MAX_PROGRESS, 0, false)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_cancel, string(R.string.kau_cancel), cancelIntent)
                .setGroup(DOWNLOAD_GROUP)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                L.e { "Video download failed" }
                toast("Video download failed")
                return@use
            }

            val stream = response.body()?.byteStream() ?: return@use
            val extension = response.request().body()?.contentType()?.subtype()
            val destination = createMediaFile(if (extension == null) "" else ".$extension")
            destination.copyFromInputStream(stream)

            notifBuilder.setContentIntent(getPendingIntent(this, destination))
            notifBuilder.show()
        }
    }

    private fun NotificationCompat.Builder.show() {
        NotificationManagerCompat.from(this@DownloadService).notify(DOWNLOAD_GROUP, notifId, build())
    }


    private fun getPendingIntent(context: Context, file: File): PendingIntent {
        val uri = context.frostUriFromFile(file)
        val type = context.contentResolver.getType(uri)
        L.i { "DownloadType: retrieved pending intent" }
        L._i { "Contents: $uri $type" }
        val intent = Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(uri, type)
        return PendingIntent.getActivity(context, 0, intent, 0)
    }

    /**
     * Adds url to downloaded list and modifies the notif builder for the finished state
     * Does not show the new notification
     */
    private fun finishDownload(url: String) {
        L.i { "Video download finished" }
        downloaded.add(url)
        notifBuilder.setContentTitle(string(R.string.downloaded_video))
                .setProgress(0, 0, false).setOngoing(false).setAutoCancel(true)
                .apply { mActions.clear() }
    }

    private fun cancelDownload() {
        client.dispatcher().cancelAll()
        NotificationManagerCompat.from(this).cancel(DOWNLOAD_GROUP, notifId)
    }

    private fun onProgressUpdate(url: String, type: MediaType?, percentage: Float, done: Boolean) {
        L.v { "Download request progress $percentage for $url" }
        notifBuilder.setProgress(MAX_PROGRESS, (percentage * MAX_PROGRESS).toInt(), false)
        if (done) finishDownload(url)
        notifBuilder.show()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    private fun initClient(): OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor { chain ->
                val original = chain.proceed(chain.request())
                val body = original.body() ?: return@addNetworkInterceptor original
                if (body.contentLength() > 0L) totalSize += body.contentLength()
                return@addNetworkInterceptor original.newBuilder()
                        .body(ProgressResponseBody(
                                original.request().url().toString(),
                                body,
                                this@DownloadService::onProgressUpdate))
                        .build()
            }
            .build()

    private class ProgressResponseBody(
            val url: String,
            val responseBody: ResponseBody,
            val listener: (url: String, type: MediaType?, percentage: Float, done: Boolean) -> Unit) : ResponseBody() {

        private val bufferedSource: BufferedSource by lazy { Okio.buffer(source(responseBody.source())) }

        override fun contentLength(): Long = responseBody.contentLength()

        override fun contentType(): MediaType? = responseBody.contentType()

        override fun source(): BufferedSource = bufferedSource

        private fun source(source: Source): Source = object : ForwardingSource(source) {

            private var totalBytesRead = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                listener(
                        url,
                        contentType(),
                        totalBytesRead.toFloat() / responseBody.contentLength(),
                        bytesRead == -1L
                )
                return bytesRead
            }
        }
    }
}