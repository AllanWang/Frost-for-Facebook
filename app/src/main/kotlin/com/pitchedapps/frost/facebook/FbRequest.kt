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
private val authMap: MutableMap<String, RequestAuth> = mutableMapOf()

fun String.fbRequest(action: RequestAuth.() -> Unit) {
    val savedAuth = authMap[this]
    if (savedAuth != null) {
        savedAuth.action()
    } else {
        val auth = getAuth()
        if (!auth.isValid) {
            L.e("Attempted fbrequest with invalid auth")
            return
        }
        authMap.put(this, auth)
        L.i(null, "Found auth $auth")
        auth.action()
    }
}

data class RequestAuth(val userId: Long = -1,
                       val cookie: String = "",
                       val fb_dtsg: String = "",
                       val rev: String = "") {
    val isValid
        get() = userId > 0 && cookie.isNotEmpty() && fb_dtsg.isNotEmpty() && rev.isNotEmpty()
}

/**
 * Request container with the execution call
 */
class FrostRequest<out T : Any>(val call: Call, private val invoke: (Call) -> T) {
    fun invoke() = invoke(call)
}

private inline fun <T : Any> RequestAuth.frostRequest(
        noinline invoke: (Call) -> T,
        builder: Request.Builder.() -> Request.Builder // to ensure we don't do anything extra at the end
): FrostRequest<T> {
    val request = cookie.requestBuilder()
    request.builder()
    return FrostRequest(request.call(), invoke)
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

fun String.getAuth(): RequestAuth {
    var auth = RequestAuth(cookie = this)
    val id = FB_USER_MATCHER.find(this)[1]?.toLong() ?: return auth
    auth = auth.copy(userId = id)
    val call = this.requestBuilder()
            .url(FB_URL_BASE)
            .get()
            .call()
    call.execute().body()?.charStream()?.useLines {
        it.forEach {
            val text = StringEscapeUtils.unescapeEcmaScript(it)
            val fb_dtsg = FB_DTSG_MATCHER.find(text)[1]
            if (fb_dtsg != null) {
                L.d(null, "fb_dtsg for ${auth.userId}: $fb_dtsg")
                auth = auth.copy(fb_dtsg = fb_dtsg)
                if (auth.isValid) return auth
            }

            val rev = FB_REV_MATCHER.find(text)[1]
            if (rev != null) {
                L.d(null, "rev for ${auth.userId}: $rev")
                auth = auth.copy(rev = rev)
                if (auth.isValid) return auth
            }
        }
    }

    return auth
}

fun RequestAuth.markNotificationRead(notifId: Long): FrostRequest<Boolean> {

    val body = listOf(
            "click_type" to "notification_click",
            "id" to notifId,
            "target_id" to "null",
            "fb_dtsg" to fb_dtsg,
            "__user" to userId
    ).withEmptyData("m_sess", "__dyn", "__req", "__ajax__")

    return frostRequest(::executeForNoError) {
        url("${FB_URL_BASE}a/jewel_notifications_log.php")
        post(body.toForm())
    }
}

inline fun <T, reified R : Any, O> Array<T>.zip(crossinline mapper: (List<R>) -> O,
                                                crossinline caller: (T) -> R): Single<O> {
    val singles = map { Single.fromCallable { caller(it) }.subscribeOn(Schedulers.io()) }
    return Single.zip(singles) {
        val results = it.mapNotNull { it as? R }
        mapper(results)
    }
}

fun RequestAuth.markNotificationsRead(vararg notifId: Long) =
        notifId.toTypedArray().zip<Long, Boolean, Boolean>(
                { it.all { it } },
                { markNotificationRead(it).invoke() })

/**
 * Execute the call and attempt to check validity
 * Valid = not blank & no "error" instance
 */
fun executeForNoError(call: Call): Boolean {
    val body = call.execute().body() ?: return false
    var empty = true
    body.charStream().useLines {
        it.forEach {
            if (it.contains("error")) return false
            if (empty && it.isNotEmpty()) empty = false
        }
    }
    return !empty
}
