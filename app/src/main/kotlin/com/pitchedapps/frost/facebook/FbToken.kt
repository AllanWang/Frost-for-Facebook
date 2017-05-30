package com.pitchedapps.frost.facebook

import com.facebook.AccessToken

/**
 * Created by Allan Wang on 2017-05-30.
 */
val token: String?
    get() = AccessToken.getCurrentAccessToken()?.token

fun setToken() {

}