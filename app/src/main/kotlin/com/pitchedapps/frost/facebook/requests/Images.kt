package com.pitchedapps.frost.facebook.requests

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import com.pitchedapps.frost.facebook.FB_URL_BASE
import okhttp3.Call
import okhttp3.Request
import java.io.IOException
import java.io.InputStream

/**
 * Created by Allan Wang on 29/12/17.
 */
fun RequestAuth.getFullSizedImage(fbid: Long) = frostRequest(::getJsonUrl) {
    url("${FB_URL_BASE}photo/view_full_size/?fbid=$fbid&__ajax__=&__user=$userId")
    get()
}

class T : StreMo
class ImageFbidLoader : BaseGlideUrlLoader<Long> {

    override fun handles(model: Long?) = true

    override fun getUrl(model: Long?, width: Int, height: Int, options: Options?): String {
        TODO("not implemented")
    }

}

class ImageFbidFetcher(private val fbid: Long,
                       private val cookie: String) : DataFetcher<InputStream> {

    @Volatile private var cancelled: Boolean = false
    private var urlCall: Call? = null
    private var inputStream: InputStream? = null

    private fun DataFetcher.DataCallback<in InputStream>.fail(msg: String) {
        onLoadFailed(RuntimeException(msg))
    }

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = DataSource.REMOTE

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        cookie.fbRequest(fail = { callback.fail("Invalid auth") }) {
            if (cancelled) return@fbRequest callback.fail("Cancelled")
            val url = getFullSizedImage(fbid).invoke() ?: return@fbRequest callback.fail("Null url")
            if (cancelled) return@fbRequest callback.fail("Cancelled")
            urlCall = Request.Builder().url(url).get().call()

            inputStream = try {
                urlCall?.execute()?.body()?.byteStream()
            } catch (e: IOException) {
                null
            }

            callback.onDataReady(inputStream)
        }
    }

    override fun cleanup() {
        try {
            inputStream?.close()
        } catch (e: IOException) {
        } finally {
            inputStream = null
        }
    }

    override fun cancel() {
        cancelled = true
        urlCall?.cancel()
        urlCall = null
        cleanup()
    }
}
