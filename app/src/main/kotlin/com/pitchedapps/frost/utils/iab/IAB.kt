package com.pitchedapps.frost.utils.iab

import android.app.Activity
import android.content.Context
import android.content.Intent
import ca.allanwang.kau.utils.isFromGooglePlay
import com.crashlytics.android.answers.PurchaseEvent
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.SettingsActivity
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswers

/**
 * Created by Allan Wang on 2017-06-23.
 */
object IAB {

    private var helper: IabHelper? = null

    /**
     * Wrapper function to ensure that the helper exists before executing a command
     *
     * [mustHavePlayStore] decides if dialogs should be shown if play store errors occur
     *
     * [onStart] should return true if we wish to dispose the helper after the operation
     * and false otherwise
     *
     */
    operator fun invoke(activity: Activity, mustHavePlayStore: Boolean = true, onStart: (helper: IabHelper) -> Boolean) {
        with(activity) {
            if (helper?.mDisposed ?: true) {
                helper = null
                L.d("IAB setup async")
                if (!isFrostPlay) {
                    if (mustHavePlayStore) playStoreNotFound()
                    return
                }
                try {
                    helper = IabHelper(applicationContext, PUBLIC_BILLING_KEY)
                    helper!!.enableDebugLogging(BuildConfig.DEBUG, "Frost:")
                    helper!!.startSetup {
                        result ->
                        if (result.isSuccess) {
                            if (onStart(helper!!))
                                helper!!.disposeWhenFinished()
                        } else if (mustHavePlayStore)
                            activity.playStoreGenericError("Setup error: ${result.response} ${result.message}")
                    }
                } catch (e: Exception) {
                    L.e(e, "IAB error")
                    if (mustHavePlayStore)
                        playStoreGenericError(null)
                }
            } else if (onStart(helper!!))
                helper!!.disposeWhenFinished()
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
            = helper?.handleActivityResult(requestCode, resultCode, data) ?: false


    /**
     * Call this after any execution to dispose the helper
     */
    fun dispose() {
        helper?.disposeWhenFinished()
        helper = null
    }
}

private const val FROST_PRO = "frost_pro"

val IS_FROST_PRO: Boolean
    get() = (BuildConfig.DEBUG && Prefs.debugPro) || Prefs.previouslyPro

private val Context.isFrostPlay: Boolean
    get() = isFromGooglePlay || BuildConfig.DEBUG

fun SettingsActivity.restorePurchases() {
    validatePro(this)
}

/**
 * If user has pro, check if it's valid and destroy the helper
 */
fun Activity.validatePro(activity: Activity) {
    IAB(activity, Prefs.previouslyPro) { //if pro, ensure that it is in inventory; if not, check quietly if it exists
        helper ->
        with(activity) {
            helper.queryInventoryAsync {
                res, inv ->
                if (res.isFailure) return@queryInventoryAsync playStoreGenericError("Query res error")
                if (inv?.getSkuDetails(FROST_PRO) != null) {
                    //owns pro
                    if (!Prefs.previouslyPro)
                        playStoreFoundPro()
                } else if (Prefs.previouslyPro) {
                    //doesn't own pro but has it
                    playStoreNoLongerPro()
                }
            }
        }
        true
    }
}

fun Activity.openPlayProPurchase(code: Int) {
    if (!IS_FROST_PRO)
        playStoreProNotAvailable()
    else openPlayPurchase(FROST_PRO, code) {
        Prefs.previouslyPro = true
    }
}

fun Activity.openPlayPurchase(key: String, code: Int, onSuccess: (key: String) -> Unit) {
    L.d("Open play purchase $key $code")
    IAB(this, true) {
        helper ->
        helper.queryInventoryAsync {
            res, inv ->
            if (res.isFailure) return@queryInventoryAsync playStoreGenericError("Query res error")
            if (inv?.getSkuDetails(key) != null) return@queryInventoryAsync playStoreAlreadyPurchased(key)
            L.d("IAB: inventory ${inv.allOwnedSkus}")
            helper.launchPurchaseFlow(this@openPlayPurchase, key, code) {
                result, _ ->
                if (result.isSuccess) {
                    onSuccess(key)
                    playStorePurchasedSuccessfully(key)
                }
                frostAnswers {
                    logPurchase(PurchaseEvent()
                            .putItemId(key)
                            .putCustomAttribute("result", result.message)
                            .putSuccess(result.isSuccess))
                }
            }
        }
        false
    }
}