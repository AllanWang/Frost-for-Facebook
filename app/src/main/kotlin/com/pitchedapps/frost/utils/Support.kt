package com.pitchedapps.frost.utils

import android.content.Context
import android.support.annotation.StringRes
import ca.allanwang.kau.email.sendEmail
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-06-29.
 */
enum class Support(@StringRes val title: Int) {
    FEEDBACK(R.string.feedback),
    BUG(R.string.bug_report),
    THEME(R.string.theme_issue),
    FEATURE(R.string.feature_request);

    fun sendEmail(context: Context) {
        with(context) {
            this.sendEmail(string(R.string.dev_email), "${string(R.string.frost_prefix)} ${string(title)}") {
                addItem("Random Frost ID", Prefs.frostId)
            }
        }
    }
}