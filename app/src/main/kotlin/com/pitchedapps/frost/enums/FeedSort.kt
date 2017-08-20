package com.pitchedapps.frost.enums

import android.support.annotation.StringRes
import com.pitchedapps.frost.R

/**
 * Created by Allan Wang on 2017-06-23.
 */
enum class FeedSort(@StringRes val textRes: Int) {
    DEFAULT(R.string.kau_default),
    MOST_RECENT(R.string.most_recent),
    TOP(R.string.top_stories);

    companion object {
        val values = values() //save one instance
        operator fun invoke(index: Int) = values[index]
    }
}