package com.pitchedapps.frost.facebook.retro

import com.pitchedapps.frost.facebook.token
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Allan Wang on 2017-05-30.
 */
interface IFrost {

    @GET("me")
    fun me(@Query(ACCESS_TOKEN) accessToken: String? = token): Call<Me>

}