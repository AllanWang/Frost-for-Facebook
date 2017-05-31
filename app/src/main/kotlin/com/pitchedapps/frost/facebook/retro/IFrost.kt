package com.pitchedapps.frost.facebook.retro

import com.pitchedapps.frost.facebook.token
import com.pitchedapps.frost.utils.L
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Allan Wang on 2017-05-30.
 */
interface IFrost {

    @GET("me")
    fun me(): Call<ResponseBody>


}

fun <T> Call<T>.enqueueFrost(success: (call: Call<T>, response: Response<T>) -> Unit) {
    this.enqueue(object : Callback<T> {
        override fun onFailure(call: Call<T>?, t: Throwable?) {
            L.e("Frost enqueue error")
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful && !call.isCanceled)
                success.invoke(call, response)
            else
                L.e("Frost response received but not successful")
        }
    })
}