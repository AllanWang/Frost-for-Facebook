package com.pitchedapps.frost.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.FrostWebActivity
import com.pitchedapps.frost.facebook.formattedFbUrl
import com.pitchedapps.frost.services.frostConfig
import com.pitchedapps.frost.services.frostNotification
import com.pitchedapps.frost.services.getNotificationPendingCancelIntent
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okio.*
import org.jetbrains.anko.AnkoAsyncContext
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Allan Wang on 2017-08-04.
 *
 * With reference to the <a href="https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java">OkHttp3 sample</a>
 */
fun Context.frostDownload(url: String) {
    L.d("Received download request", "Download $url")
    val type: DownloadType = if (url.contains("video-seal-1")) DownloadType.VIDEO
    else return
    kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) {
        granted, _ ->
        if (granted) doAsync { frostDownloadImpl(url, type) }
    }
}

private const val MAX_PROGRESS = 1000
private const val DOWNLOAD_GROUP = "frost_downloads"

private enum class DownloadType(val titleRes: Int) {
    VIDEO(R.string.downloading_video),
    FILE(R.string.downloading_file)
}

private fun AnkoAsyncContext<Context>.frostDownloadImpl(url: String, type: DownloadType) {
    L.d("Starting download request")
    val c = weakRef.get() ?: return
    val intent = Intent(c, FrostWebActivity::class.java)
    intent.data = Uri.parse(url.formattedFbUrl)
    val notifId = url.hashCode()
    val pendingIntent = PendingIntent.getActivity(c, 0, intent, 0)
    val notifBuilder = c.frostNotification
            .setContentTitle(c.string(type.titleRes))
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_PROGRESS)
            .setWhen(System.currentTimeMillis())
            .setProgress(MAX_PROGRESS, 0, false)
            .setOngoing(true)
            .addAction(R.drawable.ic_action_cancel, c.string(R.string.kau_cancel), c.getNotificationPendingCancelIntent(notifId))
            .setGroup(DOWNLOAD_GROUP)

    notifBuilder.show(weakRef, notifId)

    val request: Request = Request.Builder()
            .url(url)
            .tag(url)
            .build()

    var client: OkHttpClient? = null
    client = OkHttpClient.Builder()
            .addNetworkInterceptor {
                chain ->
                val original = chain.proceed(chain.request())
                return@addNetworkInterceptor original.newBuilder().body(ProgressResponseBody(original.body()!!) {
                    bytesRead, contentLength, done ->
                    //cancel request if context reference is now invalid
                    if (weakRef.get() == null) {
                        client?.cancel(url)
                    }
                    weakRef.get() ?: return@ProgressResponseBody client?.cancel(url) ?: Unit
                    val percentage = bytesRead.toFloat() / contentLength.toFloat() * MAX_PROGRESS
                    L.v("Download request progress: $percentage")
                    notifBuilder.setProgress(MAX_PROGRESS, percentage.toInt(), false)
                    if (done) {
                        notifBuilder.setOngoing(false)
                        L.d("Download request finished")
                    }
                    notifBuilder.show(weakRef, notifId)
                }).build()
            }
            .build()
    try {
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val destination = createMediaFile(".mp4")
        response.body()?.byteStream()?.use {
            input ->
            destination.outputStream().use {
                output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {

    }
}

private fun OkHttpClient.cancel(url: String) {
    val call = dispatcher().runningCalls().firstOrNull { it.request().tag() == url }
    if (call != null && !call.isCanceled) call.cancel()
}

private fun NotificationCompat.Builder.show(weakRef: WeakReference<Context>, notifId: Int) {
    val c = weakRef.get() ?: return
    NotificationManagerCompat.from(c).notify(DOWNLOAD_GROUP, notifId, build().frostConfig())
}

private fun NotificationCompat.Builder.finish(weakRef: WeakReference<Context>, notifId: Int) {
    val c = weakRef.get() ?: return
    NotificationManagerCompat.from(c).notify(DOWNLOAD_GROUP, notifId, build().frostConfig())
}

private class ProgressResponseBody(
        val responseBody: ResponseBody,
        val listener: (bytesRead: Long, contentLength: Long, done: Boolean) -> Unit) : ResponseBody() {

    private val bufferedSource: BufferedSource by lazy { Okio.buffer(source(responseBody.source())) }

    override fun contentLength(): Long = responseBody.contentLength()

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun source(): BufferedSource = bufferedSource

    private fun source(source: Source): Source = object : ForwardingSource(source) {

        private var totalBytesRead = 0L

        override fun read(sink: Buffer?, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            // read() returns the number of bytes read, or -1 if this source is exhausted.
            totalBytesRead += if (bytesRead != -1L) bytesRead else 0
            listener(totalBytesRead, responseBody.contentLength(), bytesRead == -1L)
            return bytesRead
        }
    }
}