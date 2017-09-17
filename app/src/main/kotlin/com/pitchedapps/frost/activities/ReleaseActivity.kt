package com.pitchedapps.frost.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.startLink
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.*

/**
 * Created by Allan Wang on 2017-09-17.
 */
class ReleaseActivity : BaseActivity() {

    val main: TextView by bindView(R.id.main)

    val opt: Button by bindView(R.id.opt)

    val ctn: Button by bindView(R.id.ctn)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_release)
        opt.setOnClickListener { v ->
            startLink(R.string.play_store_beta_link)
        }
        ctn.setOnClickListener { v ->
            launchNewTask(if (Prefs.userId != -1L) MainActivity::class.java else SelectorActivity::class.java, cookies())
        }
        setFrostTheme()
        setFrostColors(texts = arrayOf(opt, ctn,main ))
    }
}