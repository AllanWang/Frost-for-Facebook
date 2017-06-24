package com.pitchedapps.frost.utils.iab

import android.app.Activity
import android.content.Context
import ca.allanwang.kau.utils.isFromGooglePlay
import ca.allanwang.kau.utils.startPlayStoreLink
import com.crashlytics.android.answers.PurchaseEvent
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.*
import org.jetbrains.anko.doAsync

/**
 * Created by Allan Wang on 2017-06-23.
 */
object IAB {

    var helper: IabHelper? = null

    fun setupAsync(context: Context) {
        if (!context.isFromGooglePlay) return
        doAsync {
            if (helper == null) {
                try {
                    helper = IabHelper(context.applicationContext, PUBLIC_BILLING_KEY)
                    helper!!.startSetup {
                        result ->
                        if (!result.isSuccess) L.e("IAB Setup error: $result")
                    }
                } catch (e: Exception) {
                    L.e("IAB error: ${e.message}")
                }
            }
        }
    }
}

private const val FROST_PRO = "frost_pro"

val IS_FROST_PRO: Boolean
    get() = (BuildConfig.DEBUG && Prefs.debugPro) || (IAB.helper?.queryInventory()?.getSkuDetails(FROST_PRO) != null)

private fun Context.checkFromPlay(): Boolean {
    val isPlay = isFromGooglePlay
    if (!isPlay) materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_not_found)
        positiveText(R.string.kau_ok)
        neutralText(R.string.kau_play_store)
        onNeutral { _, _ -> startPlayStoreLink(R.string.play_store_package_id) }
    }
    return isPlay
}

fun Activity.openPlayProPurchase(code: Int) = openPlayPurchase(FROST_PRO, code)

fun Activity.openPlayPurchase(key: String, code: Int) {
    if (!checkFromPlay()) return
    frostAnswersCustom("PLAY_PURCHASE") {
        putCustomAttribute("Key", key)
    }
    IAB.helper?.flagEndAsync()
    IAB.helper?.queryInventoryAsync {
        _, inv ->
        if (inv == null) {
            playStoreErrorDialog()
        } else {
            val donation = inv.getSkuDetails(key)
            if (donation != null) {
                IAB.helper?.launchPurchaseFlow(this@openPlayPurchase, donation.sku, code) {
                    result, _ ->
                    if (result.isSuccess) materialDialogThemed {
                        title(R.string.play_thank_you)
                        content(R.string.play_purchased_pro)
                        positiveText(R.string.kau_ok)
                    } else playStoreErrorDialog()
                    frostAnswers {
                        logPurchase(PurchaseEvent()
                                .putItemId(key)
                                .putSuccess(result.isSuccess))
                    }
                }
            }
        }
    }
}

private fun Context.playStoreErrorDialog() {
    materialDialogThemed {
        title(R.string.uh_oh)
        content(R.string.play_store_billing_error)
        positiveText(R.string.kau_ok)
    }
}