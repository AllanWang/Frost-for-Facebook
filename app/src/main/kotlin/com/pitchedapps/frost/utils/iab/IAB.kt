package com.pitchedapps.frost.utils.iab

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import ca.allanwang.kau.utils.isFromGooglePlay
import ca.allanwang.kau.utils.snackbar
import com.crashlytics.android.answers.PurchaseEvent
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.R
import com.pitchedapps.frost.SettingsActivity
import com.pitchedapps.frost.utils.*

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
    operator fun invoke(activity: Activity, mustHavePlayStore: Boolean = true, onFailed: () -> Unit = {}, onStart: (helper: IabHelper) -> Boolean) {
        with(activity) {
            if (helper?.mDisposed ?: true) {
                helper = null
                L.d("IAB setup async")
                if (!isFrostPlay) {
                    if (mustHavePlayStore) playStoreNotFound()
                    onFailed()
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
                        } else {
                            if (mustHavePlayStore)
                                activity.playStoreGenericError("Setup error: ${result.response} ${result.message}")
                            onFailed()
                        }
                    }
                } catch (e: Exception) {
                    L.e(e, "IAB error")
                    if (mustHavePlayStore)
                        playStoreGenericError(null)
                    onFailed()
                }
            } else if (onStart(helper!!))
                helper!!.disposeWhenFinished()
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
            = helper?.handleActivityResult(requestCode, resultCode, data) ?: false

    fun cancel() {

    }

    /**
     * Call this after any execution to dispose the helper
     * Ensure that async calls have already finished beforehand
     */
    fun dispose() {
        helper?.dispose()
        helper = null
    }

    val isInProgress: Boolean
        get() = helper?.mAsyncInProgress ?: false
}

private const val FROST_PRO = "frost_pro"

val IS_FROST_PRO: Boolean
    get() = (BuildConfig.DEBUG && Prefs.debugPro) || Prefs.previouslyPro

private val Context.isFrostPlay: Boolean
    get() = isFromGooglePlay || BuildConfig.DEBUG

fun SettingsActivity.restorePurchases() {
    //like validate, but with a snackbar and without other prompts
    var restore: Snackbar? = null
    restore = container.snackbar(R.string.restoring_purchases, Snackbar.LENGTH_INDEFINITE) {
        setAction(R.string.kau_close) { restore?.dismiss() }
    }
    //called if inventory is not properly retrieved
    val reset = {
        if (Prefs.previouslyPro) {
            Prefs.previouslyPro = false
            Prefs.theme = Theme.DEFAULT.ordinal
        }
        finishRestore(restore)
    }
    getInventory(false, true, reset) {
        val proSku = it.getSkuDetails(FROST_PRO)
        Prefs.previouslyPro = proSku != null
        finishRestore(restore)
    }
}

private fun SettingsActivity.finishRestore(snackbar: Snackbar?) {
    snackbar?.dismiss()
    materialDialogThemed {
        title(R.string.purchases_restored)
        content(if (Prefs.previouslyPro) R.string.purchases_restored_with_pro else R.string.purchases_restored_without_pro)
        positiveText(R.string.reload)
        dismissListener { adapter.notifyAdapterDataSetChanged() }
    }
}

/**
 * If user has pro, check if it's valid and destroy the helper
 */
fun Activity.validatePro() {
    getInventory(Prefs.previouslyPro, true, { if (Prefs.previouslyPro) playStoreNoLongerPro() }) {
        val proSku = it.getSkuDetails(FROST_PRO)
        if (proSku == null && Prefs.previouslyPro) playStoreNoLongerPro()
        else if (proSku != null && !Prefs.previouslyPro) playStoreFoundPro()
    }
}

fun Activity.getInventory(
        mustHavePlayStore: Boolean = true,
        disposeOnFinish: Boolean = true,
        onFailed: () -> Unit = {},
        onSuccess: (inv: Inventory) -> Unit) {
    IAB(this, mustHavePlayStore, onFailed) {
        helper ->
        helper.queryInventoryAsync {
            res, inv ->
            if (res.isFailure || inv == null) onFailed()
            else onSuccess(inv)
        }
        disposeOnFinish
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