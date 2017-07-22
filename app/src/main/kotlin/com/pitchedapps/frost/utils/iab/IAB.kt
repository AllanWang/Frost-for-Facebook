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
import com.pitchedapps.frost.activities.SettingsActivity
import com.pitchedapps.frost.utils.*

/**
 * Created by Allan Wang on 2017-06-23.
 *
 * Helper singleton to handle all billing related queries
 * NOTE
 * Make sure you call [IAB.dispose] once an operation is done to release the resources
 * Also make sure that it is called on the very LAST operation if there are a list of async calls.
 * Otherwise the helper will be prematurely disposed
 *
 * For the most part, billing is handled in the [SettingsActivity] and will be disposed when it is destroyed
 * It may also be handled elsewhere when validating purchases, so those calls should dispose themselves
 */
object IAB {

    private var helper: IabHelper? = null

    /**
     * Wrapper function to ensure that the helper exists before executing a command
     *
     * [mustHavePlayStore] decides if dialogs should be shown if play store errors occur
     *
     */
    operator fun invoke(activity: Activity,
                        mustHavePlayStore: Boolean = true,
                        userRequest: Boolean = true,
                        onFailed: () -> Unit = {},
                        onStart: (helper: IabHelper) -> Unit) {
        with(activity) {
            if (isInProgress) {
                if (userRequest) snackbar(R.string.iab_still_in_progress, Snackbar.LENGTH_LONG)
                L.d("Play Store IAB in progress")
            } else if (helper?.disposed ?: true) {
                helper = null
                L.d("Play Store IAB setup async")
                if (!isFrostPlay) {
                    if (mustHavePlayStore) playStoreNotFound()
                    onFailed()
                    return
                }
                try {
                    helper = IabHelper(applicationContext, PUBLIC_BILLING_KEY)
                    helper!!.enableDebugLogging(BuildConfig.DEBUG || Prefs.verboseLogging, "Frost:")
                    helper!!.startSetup {
                        result ->
                        L.d("Play Store IAB setup finished; ${result.isSuccess}")
                        if (result.isSuccess) {
                            L.d("Play Store IAB setup success")
                            onStart(helper!!)
                        } else {
                            L.d("Play Store IAB setup fail")
                            if (mustHavePlayStore)
                                activity.playStoreGenericError("Setup error: ${result.response} ${result.message}")
                            onFailed()
                            IAB.dispose()
                        }
                    }
                } catch (e: Exception) {
                    L.e(e, "Play Store IAB error")
                    if (mustHavePlayStore)
                        playStoreGenericError(null)
                    onFailed()
                    IAB.dispose()
                }
            } else onStart(helper!!)
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
            = helper?.handleActivityResult(requestCode, resultCode, data) ?: false

    /**
     * Call this after any execution to dispose the helper
     */
    fun dispose() {
        synchronized(this) {
            L.d("Play Store IAB dispose")
            helper?.disposeWhenFinished()
            helper = null
        }
    }

    /**
     * Dispose given helper and check if it matches with our own helper
     */
    fun dispose(helper: IabHelper) {
        synchronized(this) {
            L.d("Play Store IAB helper dispose")
            helper.disposeWhenFinished()
            if (IAB.helper?.disposed ?: true)
                this.helper = null
        }
    }

    val isInProgress: Boolean
        get() = helper?.mAsyncInProgress ?: false
}

private const val FROST_PRO = "frost_pro"

private val IabHelper.disposed: Boolean
    get() = mDisposed || mDisposeAfterAsync

val IS_FROST_PRO: Boolean
    get() = (BuildConfig.DEBUG && Prefs.debugPro) || Prefs.pro

private val Context.isFrostPlay: Boolean
    get() = isFromGooglePlay || BuildConfig.DEBUG

fun SettingsActivity.restorePurchases() {
    //like validate, but with a snackbar and without other prompts
    val restore = container.snackbar(R.string.restoring_purchases, Snackbar.LENGTH_INDEFINITE)
    restore.setAction(R.string.kau_close) { restore.dismiss() }
    //called if inventory is not properly retrieved
    val reset = {
        L.d("Play Store Restore reset")
        if (Prefs.pro) {
            Prefs.pro = false
            Prefs.theme = Theme.DEFAULT.ordinal
        }
        finishRestore(restore, false)
    }
    getInventory(false, true, reset) {
        inv, _ ->
        val proSku = inv.hasPurchase(FROST_PRO)
        Prefs.pro = proSku
        L.d("Play Store Restore found: ${Prefs.pro}")
        finishRestore(restore, Prefs.pro)
    }
}

private fun SettingsActivity.finishRestore(snackbar: Snackbar, hasPro: Boolean) {
    snackbar.dismiss()
    materialDialogThemed {
        title(R.string.purchases_restored)
        val text = if (hasPro) R.string.purchases_restored_with_pro else R.string.purchases_restored_without_pro
        content(text)
        positiveText(R.string.reload)
        dismissListener { adapter.notifyAdapterDataSetChanged() }
    }
}

/**
 * If user has pro, check if it's valid and destroy the helper
 * If cache matches result, it finishes silently
 */
fun Activity.validatePro() {
    L.d("Play Store Validate pro")
    try {
        getInventory(Prefs.pro, false, { if (Prefs.pro) playStoreNoLongerPro() }) {
            inv, helper ->
            val proSku = inv.hasPurchase(FROST_PRO)
            L.d("Play Store Validation finished: ${Prefs.pro} should be $proSku")
            if (!proSku && Prefs.pro) playStoreNoLongerPro()
            else if (proSku && !Prefs.pro) playStoreFoundPro()
            IAB.dispose(helper)
        }
    } catch (e: Exception) {
        L.e(e, "Play store validation exception")
        IAB.dispose()
    }
}

fun Activity.getInventory(
        mustHavePlayStore: Boolean = true,
        userRequest: Boolean = true,
        onFailed: () -> Unit = {},
        onSuccess: (inv: Inventory, helper: IabHelper) -> Unit) {
    IAB(this, mustHavePlayStore, userRequest, onFailed) {
        helper ->
        helper.queryInventoryAsync {
            res, inv ->
            L.d("Play Store Inventory query finished")
            if (res.isFailure || inv == null) {
                L.e("Play Store Res error ${res.message}")
                onFailed()
            } else onSuccess(inv, helper)
        }
    }
}

fun Activity.openPlayProPurchase(code: Int) {
    if (!isFrostPlay)
        playStoreProNotAvailable()
    else openPlayPurchase(FROST_PRO, code) {
        Prefs.pro = true
    }
}

fun Activity.openPlayPurchase(key: String, code: Int, onSuccess: (key: String) -> Unit) {
    L.d("Play Store open purchase $key $code")
    getInventory(true, true, { playStoreGenericError("Query res error") }) {
        inv, helper ->
        if (inv.hasPurchase(key)) {
            playStoreAlreadyPurchased(key)
            onSuccess(key)
            return@getInventory
        }
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
}