package com.pitchedapps.frost.utils.iab

import android.app.Activity
import android.content.Context
import ca.allanwang.kau.utils.isFromGooglePlay
import ca.allanwang.kau.utils.startPlayStoreLink
import com.crashlytics.android.answers.PurchaseEvent
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.*

/**
 * Created by Allan Wang on 2017-06-23.
 */
object IAB {

    var helper: IabHelper? = null

    fun setupAsync(activity: Activity) {
        if (helper == null) {
            L.d("IAB setup async")
            if (!activity.isFromGooglePlay && !BuildConfig.DEBUG) return L.d("IAB not from google play")
            try {
                helper = IabHelper(activity.applicationContext, PUBLIC_BILLING_KEY)
                helper!!.startSetup {
                    result ->
                    L.d("IAB result ${result.message}")
                    if (!result.isSuccess) L.eThrow("IAB Setup error: $result")
                }
            } catch (e: Exception) {
                L.e(e, "IAB error")
                activity.playStoreNoLongerPro()
            }
        }
    }
}

private const val FROST_PRO = "frost_pro"

val IS_FROST_PRO: Boolean
    get() = (BuildConfig.DEBUG && Prefs.debugPro) || Prefs.previouslyPro

private fun Context.checkFromPlay(): Boolean {
    val isPlay = isFromGooglePlay || BuildConfig.DEBUG
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
    L.d("Open play purchase $key $code")
    if (!checkFromPlay()) return
    frostAnswersCustom("PLAY_PURCHASE") {
        putCustomAttribute("Key", key)
    }
    L.d("IAB flag end async")
    IAB.helper?.flagEndAsync() ?: return playStoreGenericError("Null flag end async")
    L.d("IAB query inv async")
    IAB.helper!!.queryInventoryAsync {
        res, inv ->
        if (res.isFailure) return@queryInventoryAsync playStoreGenericError("Query res error")
        if (inv == null) return@queryInventoryAsync playStoreGenericError("Empty inventory")
        L.d("IAB: inventory ${inv.allOwnedSkus}")
        val donation = inv.getSkuDetails(key) ?: return@queryInventoryAsync playStoreGenericError("Donation null")
        IAB.helper!!.launchPurchaseFlow(this@openPlayPurchase, donation.sku, code) {
            result, _ ->
            if (result.isSuccess) materialDialogThemed {
                title(R.string.play_thank_you)
                content(R.string.play_purchased_pro)
                positiveText(R.string.kau_ok)
            } else playStoreGenericError("Result: ${result.message}")
            frostAnswers {
                logPurchase(PurchaseEvent()
                        .putItemId(key)
                        .putSuccess(result.isSuccess))
            }
        }
    }
}