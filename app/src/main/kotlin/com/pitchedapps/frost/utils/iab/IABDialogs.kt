package com.pitchedapps.frost.utils.iab

import android.app.Activity
import ca.allanwang.kau.utils.restart
import ca.allanwang.kau.utils.startLink
import ca.allanwang.kau.utils.startPlayStoreLink
import ca.allanwang.kau.utils.string
import com.crashlytics.android.answers.PurchaseEvent
import com.pitchedapps.frost.R
import com.pitchedapps.frost.activities.MainActivity
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswers
import com.pitchedapps.frost.utils.materialDialogThemed

/**
 * Created by Allan Wang on 2017-06-30.
 */

private fun playStoreLog(text: String) {
    L.e(Throwable(text), "IAB Play Store Exception")
}

/**
 * Properly restart an activity
 */
private fun Activity.playRestart() {
    if (this is SettingsActivity) {
        setResult(MainActivity.REQUEST_RESTART)
        finish()
    } else restart()
}

fun Activity?.playStoreNoLongerPro() {
    Prefs.pro = false
    L.d("IAB No longer pro")
    frostAnswers {
        logPurchase(PurchaseEvent()
                .putCustomAttribute("result", "no longer pro")
                .putSuccess(false))
    }
    if (this == null) return
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_not_pro)
        positiveText(R.string.reload)
        dismissListener {
            this@playStoreNoLongerPro.playRestart()
        }
    }
}

fun Activity?.playStoreFoundPro() {
    Prefs.pro = true
    L.d("Found pro")
    if (this == null) return
    materialDialogThemed {
        title(R.string.found_pro)
        content(R.string.found_pro_desc)
        positiveText(R.string.reload)
        dismissListener {
            this@playStoreFoundPro.playRestart()
        }
    }
}

fun Activity.playStorePurchaseUnsupported() {
    L.d("Play store not found")
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_unsupported)
        negativeText(R.string.kau_close)
        positiveText(R.string.kau_play_store)
        neutralText(R.string.paypal)
        onPositive { _, _ -> startPlayStoreLink(R.string.play_store_package_id) }
        onNeutral { _, _ -> startLink(string(R.string.dev_paypal)) }
    }
}

fun Activity.playStorePurchasedSuccessfully(key: String) {
    L.d("Play store purchased $key successfully")
    materialDialogThemed {
        title(R.string.play_thank_you)
        content(R.string.play_purchased_pro)
        positiveText(R.string.kau_ok)
        neutralText(R.string.kau_rate)
        onNeutral { _, _ -> startPlayStoreLink(R.string.play_store_package_id) }
    }
}

fun Activity.purchaseRestored() {
    L.d("Purchase restored")
    materialDialogThemed {
        title(R.string.play_thank_you)
        content(R.string.purchases_restored_with_pro)
        positiveText(R.string.kau_ok)
    }
}