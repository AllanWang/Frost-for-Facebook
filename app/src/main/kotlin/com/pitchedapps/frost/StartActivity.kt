package com.pitchedapps.frost

import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import com.pitchedapps.frost.activities.LoginActivity
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SelectorActivity
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.facebook.FbCookie
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
                cookies ->
                L.d("Cookies loaded ${System.currentTimeMillis()}", cookies.toString())
                if (cookies.isNotEmpty())
                    launchNewTask(if (Prefs.userId != -1L) MainActivity::class.java else SelectorActivity::class.java, ArrayList(cookies))
                else
                    launchNewTask(LoginActivity::class.java)
            }
        }
    }
}