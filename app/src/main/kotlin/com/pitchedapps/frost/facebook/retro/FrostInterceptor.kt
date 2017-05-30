package com.pitchedapps.frost.facebook.retro

import android.content.Context
import com.pitchedapps.frost.facebook.token
import com.pitchedapps.frost.utils.Utils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by Allan Wang on 2017-05-30.
 */
private val maxStale = 60 * 60 * 24 * 28 //maxAge to get from cache if online (4 weeks)
const val ACCESS_TOKEN = "access_token"

class FrostInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response? {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val urlBase = request.url()
        val urlWithToken = urlBase.newBuilder()
        if (urlBase.queryParameter(ACCESS_TOKEN) == null && token != null)
            urlWithToken.addQueryParameter(ACCESS_TOKEN, token)
        requestBuilder.url(urlWithToken.build())
        if (!Utils.isNetworkAvailable(context)) requestBuilder.addHeader("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
        return chain.proceed(requestBuilder.build())
    }

}