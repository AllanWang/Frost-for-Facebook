package com.pitchedapps.frost.enums

import android.content.Context
import androidx.annotation.StringRes
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.sendFrostEmail

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
            this.sendFrostEmail("${string(R.string.frost_prefix)} ${string(title)}") {
            }
        }
    }
}