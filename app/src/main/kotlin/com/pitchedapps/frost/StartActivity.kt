package com.pitchedapps.frost

import android.content.Intent
import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.startActivity
import com.pitchedapps.frost.activities.LoginActivity
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SelectorActivity
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.facebook.FbCookie
import com.pitchedapps.frost.utils.EXTRA_COOKIES
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.launchNewTask

/**
 * Created by Allan Wang on 2017-05-28.
 */
class StartActivity : KauBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FbCookie.switchBackUser {
            loadFbCookiesAsync {
                val cookies = ArrayList(it)
                L.i { "Cookies loaded at time ${System.currentTimeMillis()}" }
                L._d { "Cookies: ${cookies.joinToString("\t")}" }
                if (cookies.isNotEmpty()) {
                    if (Prefs.userId != -1L)
                        startActivity<MainActivity>(intentBuilder = {
                            putParcelableArrayListExtra(EXTRA_COOKIES, cookies)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    else
                        launchNewTask<SelectorActivity>(cookies)
                } else
                    launchNewTask<LoginActivity>()
            }
        }
    }
}