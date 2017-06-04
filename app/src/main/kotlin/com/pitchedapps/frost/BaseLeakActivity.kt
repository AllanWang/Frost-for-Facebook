package com.pitchedapps.frost

import android.support.v7.app.AppCompatActivity
import com.pitchedapps.frost.utils.refWatch

/**
 * Created by Allan Wang on 2017-06-04.
 */
open class BaseLeakActivity : AppCompatActivity() {
    override fun onDestroy() {
        super.onDestroy()
        refWatch()
    }
}