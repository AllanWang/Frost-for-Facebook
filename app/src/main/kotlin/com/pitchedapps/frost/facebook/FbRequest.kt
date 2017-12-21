package com.pitchedapps.frost.facebook

import com.pitchedapps.frost.BuildConfig
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Created by Allan Wang on 21/12/17.
 */
data class RequestAuth(val userId: Long, val cookie: String, val fb_dtsg: String)

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

private fun RequestAuth.requestBuilder() = Request.Builder()
        .header("Cookie", cookie)
        .header("User-Agent", USER_AGENT_BASIC)
        .cacheControl(CacheControl.FORCE_NETWORK)

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

    val request = requestBuilder()
            .url("${FB_URL_BASE}a/jewel_notifications_log.php")
            .post(body.toForm())
            .build()

    return client.newCall(request)
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
