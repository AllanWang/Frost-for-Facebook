package com.pitchedapps.frost

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ca.allanwang.kau.utils.startActivity
import com.pitchedapps.frost.activities.IntroActivity

/**
 * Created by Allan Wang on 2017-05-28.
 */
class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        FbCookie.switchBackUser {
//            loadFbCookiesAsync {
//                cookies ->
//                L.d("Cookies loaded ${System.currentTimeMillis()}", cookies.toString())
//                if (cookies.isNotEmpty())
//                    launchNewTask(if (Prefs.userId != -1L) MainActivity::class.java else SelectorActivity::class.java, ArrayList(cookies))
//                else
//                    launchNewTask(LoginActivity::class.java)
//            }
//        }
        startActivity(IntroActivity::class.java)
    }
}