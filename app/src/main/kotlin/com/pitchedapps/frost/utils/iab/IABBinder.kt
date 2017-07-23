package com.pitchedapps.frost.utils.iab

import android.app.Activity
import android.content.Intent
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.crashlytics.android.answers.PurchaseEvent
import com.pitchedapps.frost.BuildConfig
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.frostAnswers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by Allan Wang on 2017-07-22.
 */
private const val FROST_PRO = "frost_pro"

val IS_FROST_PRO: Boolean
    get() = (BuildConfig.DEBUG && Prefs.debugPro) || Prefs.pro

interface FrostBilling : BillingProcessor.IBillingHandler {
    fun Activity.onCreateBilling()
    fun onDestroyBilling()
    fun purchasePro()
    fun restorePurchases(once: Boolean)
    fun onActivityResultBilling(requestCode: Int, resultCode: Int, data: Intent?): Boolean
}

open class IABBinder : FrostBilling {

    var bp: BillingProcessor? = null
    var activity: Activity? = null

    override fun Activity.onCreateBilling() {
        bp = BillingProcessor.newBillingProcessor(this, PUBLIC_BILLING_KEY, this@IABBinder)
        activity = this
        bp!!.initialize()
    }

    override fun onDestroyBilling() {
        bp?.release()
        bp = null
        activity = null
    }

    override fun onBillingInitialized() {
        L.d("IAB initialized")
    }

    override fun onPurchaseHistoryRestored() {
        L.d("IAB restored")
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails) {
        L.d("IAB $productId purchased")
        frostAnswers {
            logPurchase(PurchaseEvent()
                    .putItemId(productId)
                    .putSuccess(true)
            )
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable) {
        frostAnswers {
            logPurchase(PurchaseEvent()
                    .putCustomAttribute("result", errorCode.toString())
                    .putSuccess(false))
        }
        L.e(error, "IAB error $errorCode")
    }

    override fun onActivityResultBilling(requestCode: Int, resultCode: Int, data: Intent?): Boolean
            = bp?.handleActivityResult(requestCode, resultCode, data) ?: false

    override fun purchasePro() {
        if (bp == null) return
        if (!bp!!.isOneTimePurchaseSupported)
            activity!!.playStorePurchaseUnsupported()
        else
            bp!!.purchase(activity, FROST_PRO)
    }

    override fun restorePurchases(once: Boolean) {
        if (bp == null) return
        doAsync {
            bp?.loadOwnedPurchasesFromGoogle()
            if (bp?.isPurchased(FROST_PRO) ?: false) {
                uiThread {
                    if (Prefs.pro) activity!!.playStoreNoLongerPro()
                    else if (!once) purchasePro()
                    if (once) onDestroyBilling()
                }
            } else {
                uiThread {
                    if (!Prefs.pro) activity!!.playStoreFoundPro()
                    else if (!once) activity!!.purchaseRestored()
                    if (once) onDestroyBilling()
                }
            }
        }
    }
}

class IABSettings : IABBinder() {

    override fun onBillingInitialized() {
        super.onBillingInitialized()

    }

    override fun onPurchaseHistoryRestored() {
        super.onPurchaseHistoryRestored()
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails) {
        super.onProductPurchased(productId, details)
    }

    override fun onBillingError(errorCode: Int, error: Throwable) {
        super.onBillingError(errorCode, error)
        activity?.playStoreGenericError(null)
    }
}

class IABMain : IABBinder() {

    override fun onBillingInitialized() {
        super.onBillingInitialized()
        restorePurchases(true)
    }

    override fun onPurchaseHistoryRestored() {
        super.onPurchaseHistoryRestored()
        restorePurchases(true)
    }
}