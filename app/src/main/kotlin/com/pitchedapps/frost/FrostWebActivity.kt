package com.pitchedapps.frost

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import ca.allanwang.kau.utils.*
import com.jude.swipbackhelper.SwipeBackHelper
import com.pitchedapps.frost.facebook.FbTab
import com.pitchedapps.frost.utils.ARG_URL
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.formattedFbUrl
import com.pitchedapps.frost.utils.setFrostColors
import com.pitchedapps.frost.web.FrostWebView


/**
 * Created by Allan Wang on 2017-06-19.
 *
 * Replica of [WebOverlayActivity] with a different base url
 * Didn't use activity-alias because it causes issues when only one activity has the singleInstance mode
 */
class FrostWebActivity:WebOverlayActivity() {
    override val url: String
        get() = intent.dataString!!.formattedFbUrl

}