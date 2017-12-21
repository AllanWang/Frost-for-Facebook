package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.utils.L
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.text.StringEscapeUtils

/**
 * Created by Allan Wang on 21/12/17.
 */
data class RequestAuth(val userId: Long = -1, val cookie: String = "", val fb_dtsg: String = "")

private val client: OkHttpClient by lazy {
    val builder = OkHttpClient.Builder()
    if (BuildConfig.DEBUG)
        builder.addInterceptor(HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC))
    builder.build()
}

private fun List<Pair<String, Any?>>.toForm(): RequestBody {
    val builder = FormBody.Builder()
    forEach { (key, value) ->
        val v = value?.toString() ?: ""
        builder.add(key, v)
    }
    return builder.build()
}

private fun String.requestBuilder() = Request.Builder()
        .header("Cookie", this)
        .header("User-Agent", USER_AGENT_BASIC)
        .cacheControl(CacheControl.FORCE_NETWORK)

private fun Request.Builder.call() = client.newCall(build())


fun Pair<Long, String>.getAuth(): RequestAuth? {
    val (userId, cookie) = this
    val call = cookie.requestBuilder()
            .url(FB_URL_BASE)
            .get()
            .call()
    call.execute().body()?.charStream()?.useLines {
        it.forEach {
            val text = StringEscapeUtils.unescapeEcmaScript(it)
            val result = FB_DTSG_MATCHER.find(text)
            val fb_dtsg = result?.groupValues?.get(1)
            if (fb_dtsg != null) {
                L.d(null, "fb_dtsg for $userId: $fb_dtsg")
                return RequestAuth(userId, cookie, fb_dtsg)
            }
        }
    }

    return null
}

fun RequestAuth.markNotificationRead(notifId: Long): Call {

    val body = listOf(
            "click_type" to "notification_click",
            "id" to notifId,
            "target_id" to "null",
            "m_sess" to null,
            "fb_dtsg" to fb_dtsg,
            "__dyn" to null,
            "__req" to null,
            "__ajax__" to null,
            "__user" to userId
    )

    return cookie.requestBuilder()
            .url("${FB_URL_BASE}a/jewel_notifications_log.php")
            .post(body.toForm())
            .call()
}

private inline fun <T, reified R : Any, O> zip(data: Array<T>,
                                               crossinline mapper: (List<R>) -> O,
                                               crossinline caller: (T) -> R): Single<O> {
    val singles = data.map { Single.fromCallable { caller(it) }.subscribeOn(Schedulers.io()) }
    return Single.zip(singles) {
        val results = it.mapNotNull { it as? R }
        mapper(results)
    }
}

fun RequestAuth.markNotificationsRead(vararg notifId: Long) = zip<Long, Boolean, Int>(notifId.toTypedArray(),
        { it.count { it } }) {
    val response = markNotificationRead(it).execute()
    val buffer = CharArray(20)
    response.body()?.charStream()?.read(buffer) ?: return@zip false
    !buffer.toString().contains("error")
}
