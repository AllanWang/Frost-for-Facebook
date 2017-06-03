package com.pitchedapps.frost

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.pitchedapps.frost.dbflow.loadFbCookiesAsync
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs

/**
 * Created by Allan Wang on 2017-05-28.
 */
class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        L.d("Load cookies ${System.currentTimeMillis()}")
        loadFbCookiesAsync {
            cookies ->
            L.d("Cookies loaded ${System.currentTimeMillis()} $cookies")
            val sorted = cookies.toMutableList()
            val current = cookies.filter { it.id == Prefs.userId }
            if (current.isNotEmpty()) {
                sorted.remove(current[0])
                sorted.add(0, current[0])
            }
            MainActivity.launch(this, sorted)
        }
    }
}