package com.pitchedapps.frost.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import ca.allanwang.kau.utils.copyFromInputStream
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.createMediaFile
import okhttp3.*
import okio.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by Allan Wang on 2017-08-08.
 *
 * Background file downloader
 * All we are given is a link and a mime type
 * To keep it simple, we'll opt for an IntentService and queued downloads
 */
class DownloadService : Service() {

    companion object {
        private const val EXTRA_URL = "download_url"
    }

    val client: OkHttpClient by lazy { initClient() }

    val urls = ConcurrentLinkedQueue<String>()
    var mostRecentStartId = 0
    val start = System.currentTimeMillis()
    var totalSize = 0L


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        client.dispatcher().cancelAll()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url: String? = intent?.getStringExtra(EXTRA_URL)
        if (url == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }
        if (urls.contains(url)) {
            toast("Already in progress")
            stopSelf(startId)
            return START_NOT_STICKY
        }
        urls.add(url)
        mostRecentStartId = startId
        val request: Request = Request.Builder()
                .url(url)
                .tag(startId)
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val id = call.request().tag() as Int
                L.e("Download failed; ${e.message}")
                this@DownloadService.toast("Download with id $id failed")
            }

            override fun onResponse(call: Call, response: Response) {
                val id = call.request().tag() as Int
                if (!response.isSuccessful) {
                    L.e("Download failed; ${response.message()}")
                    this@DownloadService.toast("Download with id $id failed")
                } else {
                    val stream = response.body()?.byteStream() ?: return
                    val extension = response.request().body()?.contentType()?.subtype()
                    val destination = createMediaFile(if (extension == null) "" else ".$extension")
                    destination.copyFromInputStream(stream)
                    //todo add clickable action here
                }
                //todo call notification finished here
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
        return START_REDELIVER_INTENT
    }

    fun startNotification() {

    }

    fun finishNotification() {

        //check to see if we are done with our requests
    }


    fun onProgressUpdate(url: String, type: MediaType?, percentage: Float, done: Boolean) {

    }

    private fun initClient(): OkHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor {
                chain ->
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

            override fun read(sink: Buffer?, byteCount: Long): Long {
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