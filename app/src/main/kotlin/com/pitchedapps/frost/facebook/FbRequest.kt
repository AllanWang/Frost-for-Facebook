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
data class RequestAuth(val userId: Long = -1,
                       val cookie: String = "",
                       val fb_dtsg: String = "",
                       val rev: String = "") {
    val isValid
        get() = userId > 0 && cookie.isNotEmpty() && fb_dtsg.isNotEmpty() && rev.isNotEmpty()
}

private val client: OkHttpClient by lazy {
    val builder = OkHttpClient.Builder()
    if (BuildConfig.DEBUG)
        builder.addInterceptor(HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BASIC))
    builder.build()
}

private fun List<Pair<String, Any?>>.toForm(): FormBody {
    val builder = FormBody.Builder()
    forEach { (key, value) ->
        val v = value?.toString() ?: ""
        builder.add(key, v)
    }
    return builder.build()
}

private fun List<Pair<String, Any?>>.withEmptyData(vararg key: String): List<Pair<String, Any?>> {
    val newList = toMutableList()
    newList.addAll(key.map { it to null })
    return newList
}

private fun String.requestBuilder() = Request.Builder()
        .header("Cookie", this)
        .header("User-Agent", USER_AGENT_BASIC)
        .cacheControl(CacheControl.FORCE_NETWORK)

private fun Request.Builder.call() = client.newCall(build())


fun Pair<Long, String>.getAuth(): RequestAuth {
    val (userId, cookie) = this
    var auth = RequestAuth(userId, cookie)
    val call = cookie.requestBuilder()
            .url("https://touch.facebook.com")
            .get()
            .call()
    call.execute().body()?.charStream()?.useLines {
        it.forEach {
            val text = StringEscapeUtils.unescapeEcmaScript(it)
            val fb_dtsg = FB_DTSG_MATCHER.find(text)[1]
            if (fb_dtsg != null) {
                L.d(null, "fb_dtsg for $userId: $fb_dtsg")
                auth = auth.copy(fb_dtsg = fb_dtsg)
                if (auth.isValid) return auth
            }

            val rev = FB_REV_MATCHER.find(text)[1]
            if (rev != null) {
                L.d(null, "rev for $userId: $rev")
                auth = auth.copy(rev = rev)
                if (auth.isValid) return auth
            }
        }
    }

    return auth
}

fun RequestAuth.markNotificationRead(notifId: Long): Call {

    val body = listOf(
            "click_type" to "notification_click",
            "id" to notifId,
            "target_id" to "null",
            "fb_dtsg" to fb_dtsg,
            "__user" to userId
    ).withEmptyData("m_sess", "__dyn", "__req", "__ajax__")

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

/**
 * Execute the call and attempt to check validity
 */
fun Call.executeAndCheck(): Boolean {
    val body = execute().body() ?: return false
    var empty = true
    body.charStream().useLines {
        it.forEach {
            if (empty && it.isNotEmpty()) empty = false
            if (it.contains("error")) return true
        }
    }
    return !empty
}
