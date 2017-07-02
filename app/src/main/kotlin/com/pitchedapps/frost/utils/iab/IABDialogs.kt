package com.pitchedapps.frost.utils.iab

import android.app.Activity
import ca.allanwang.kau.utils.restart
import ca.allanwang.kau.utils.startPlayStoreLink
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.MainActivity
import com.pitchedapps.frost.R
import com.pitchedapps.frost.SettingsActivity
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.materialDialogThemed

/**
 * Created by Allan Wang on 2017-06-30.
 */
private fun playStoreLog(text: String) {
    L.e(Throwable(text), "Play Store Exception")
}

private fun Activity.playRestart() {
    if (this is MainActivity) restart()
    else if (this is SettingsActivity) {
        setResult(MainActivity.REQUEST_RESTART)
        finish()
    }
}

fun Activity.playStoreNoLongerPro() {
    if (!Prefs.previouslyPro) return //never pro to begin with
    Prefs.previouslyPro = false
    playStoreLog("No Longer Pro")
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_not_pro)
        positiveText(R.string.reload)
        dismissListener {
            this@playStoreNoLongerPro.playRestart()
        }
    }
}

fun Activity.playStoreNotFound() {
    L.d("Play store not found")
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_not_found)
        positiveText(R.string.kau_ok)
        neutralText(R.string.kau_play_store)
        onNeutral { _, _ -> startPlayStoreLink(R.string.play_store_package_id) }
    }
}

fun Activity.playStoreProNotAvailable() {
    playStoreLog("Pro found; store not available")
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_not_found)
        positiveText(R.string.kau_ok)
        neutralText(R.string.kau_play_store)
        onNeutral { _, _ -> startPlayStoreLink(R.string.play_store_package_id) }
    }
}

fun Activity.playStoreGenericError(text: String = "Store generic error") {
    playStoreLog("IAB: $text")
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_billing_error)
        positiveText(R.string.kau_ok)
    }
}

fun Activity.playStoreAlreadyPurchased(key: String) {
    materialDialogThemed {
        title(R.string.play_already_purchased)
        content(String.format(string(R.string.play_already_purchased_content), key))
        positiveText(R.string.reload)
        dismissListener {
            this@playStoreAlreadyPurchased.playRestart()
        }
    }
}

fun Activity.playStorePurchasedSuccessfully(key: String) {
    materialDialogThemed {
        title(R.string.play_thank_you)
        content(R.string.play_purchased_pro)
        positiveText(R.string.kau_ok)
    }
}