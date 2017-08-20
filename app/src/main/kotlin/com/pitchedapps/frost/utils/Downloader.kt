package com.pitchedapps.frost.utils

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.FileProvider
import ca.allanwang.kau.permissions.PERMISSION_WRITE_EXTERNAL_STORAGE
import ca.allanwang.kau.permissions.kauRequestPermissions
import ca.allanwang.kau.utils.copyFromInputStream
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.services.frostNotification
import com.pitchedapps.frost.services.getNotificationPendingCancelIntent
import com.pitchedapps.frost.services.quiet
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

/**
 * Created by Allan Wang on 2017-08-04.
 *
 * With reference to the <a href="https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/Progress.java">OkHttp3 sample</a>
 */
fun Context.frostDownload(url: String) {
    L.d("Received download request", "Download $url")
    val type = if (url.contains("video")) DownloadType.VIDEO
    else return L.d("Download request does not match any type")
    kauRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE) {
        granted, _ ->
        if (granted) doAsync { frostDownloadImpl(url, type) }
    }
}

private const val MAX_PROGRESS = 1000
//private val DOWNLOAD_GROUP: String? = null
private const val DOWNLOAD_GROUP = "frost_downloads"

private enum class DownloadType(val downloadingRes: Int, val downloadedRes: Int) {
    VIDEO(R.string.downloading_video, R.string.downloaded_video),
    FILE(R.string.downloading_file, R.string.downloaded_file);

    fun getPendingIntent(context: Context, file: File): PendingIntent {
        val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        val type = context.contentResolver.getType(uri)
        L.d("DownloadType: retrieved pending intent - $uri $type")
        val intent = Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(uri, type)
        return PendingIntent.getActivity(context, 0, intent, 0)
    }
}

private fun AnkoAsyncContext<Context>.frostDownloadImpl(url: String, type: DownloadType) {
    L.d("Starting download request")
    val notifId = Math.abs(url.hashCode() + System.currentTimeMillis().toInt())
    var notifBuilderAttempt: NotificationCompat.Builder? = null
    weakRef.get()?.apply {
        notifBuilderAttempt = frostNotification.quiet
                .setContentTitle(string(type.downloadingRes))
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setWhen(System.currentTimeMillis())
                .setProgress(MAX_PROGRESS, 0, false)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_cancel, string(R.string.kau_cancel), getNotificationPendingCancelIntent(DOWNLOAD_GROUP, notifId))
                .setGroup(DOWNLOAD_GROUP)
    }
    val notifBuilder = notifBuilderAttempt ?: return
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
                    val ctx = weakRef.get() ?: return@ProgressResponseBody client?.cancel(url) ?: Unit
                    val percentage = bytesRead.toFloat() / contentLength.toFloat() * MAX_PROGRESS
                    L.v("Download request progress: $percentage")
                    notifBuilder.setProgress(MAX_PROGRESS, percentage.toInt(), false)
                    if (done) {
                        notifBuilder.setFinished(ctx, type)
                        L.d("Download request finished")
                    }
                    notifBuilder.show(weakRef, notifId)
                }).build()
            }
            .build()
    client.newCall(request).execute().use {
        response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        val stream = response.body()?.byteStream()
        if (stream != null) {
            val destination = createMediaFile(".mp4")
            destination.copyFromInputStream(stream)
            weakRef.get()?.apply {
                notifBuilder.setContentIntent(type.getPendingIntent(this, destination))
                notifBuilder.show(weakRef, notifId)
            }
        }
    }
}

private fun NotificationCompat.Builder.setFinished(context: Context, type: DownloadType)
        = setContentTitle(context.string(type.downloadedRes))
        .setProgress(0, 0, false).setOngoing(false).setAutoCancel(true)
        .apply { mActions.clear() }

private fun OkHttpClient.cancel(url: String) {
    val call = dispatcher().runningCalls().firstOrNull { it.request().tag() == url }
    if (call != null && !call.isCanceled) call.cancel()
}

private fun NotificationCompat.Builder.show(weakRef: WeakReference<Context>, notifId: Int) {
    val c = weakRef.get() ?: return
    NotificationManagerCompat.from(c).notify(DOWNLOAD_GROUP, notifId, build())
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