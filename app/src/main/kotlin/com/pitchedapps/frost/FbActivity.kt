package com.pitchedapps.frost

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v7.app.AppCompatActivity
import com.facebook.AccessToken
import com.pitchedapps.frost.utils.L

/**
 * Created by Allan Wang on 2017-05-29.
 */
open class FbActivity : AppCompatActivity() {
    var accessToken: AccessToken? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accessToken = AccessToken.getCurrentAccessToken()
        L.e("Access ${accessToken?.token}")
    }
}