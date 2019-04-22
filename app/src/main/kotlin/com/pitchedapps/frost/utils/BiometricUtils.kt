package com.pitchedapps.frost.utils

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import ca.allanwang.kau.utils.string
import com.pitchedapps.frost.R
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias BiometricDeferred = CompletableDeferred<BiometricPrompt.CryptoObject?>

/**
 * Container for [BiometricPrompt]
 * Inspired by coroutine's CommonPool
 */
object BiometricUtils {

    private val executor: Executor
        get() = pool ?: getOrCreatePoolSync()

    @Volatile
    private var pool: ExecutorService? = null

    /**
     * Checks if biometric authentication is possible
     * Currently, this means checking for enrolled fingerprints
     */
    @Suppress("DEPRECATION")
    fun isSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val fingerprintManager = context.getSystemService(FingerprintManager::class.java) ?: return false
        return fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()
    }

    private fun getOrCreatePoolSync(): Executor =
        pool ?: Executors.newSingleThreadExecutor().also { pool = it }

    private fun shouldPrompt(context: Context): Boolean {
        return true
    }

    fun authenticate(activity: FragmentActivity): BiometricDeferred {
        val deferred: BiometricDeferred = CompletableDeferred()
        if (!shouldPrompt(activity)) {
            deferred.complete(null)
            return deferred
        }
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.string(R.string.biometrics_prompt_title))
            .setNegativeButtonText(activity.string(R.string.kau_cancel))
            .build()
        BiometricPrompt(activity, executor, Callback(activity, deferred)).authenticate(info)
        return deferred
    }

    private class Callback(val activity: FragmentActivity, val deferred: BiometricDeferred) :
        BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            deferred.cancel()
            activity.finish()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            deferred.complete(result.cryptoObject)
        }

        override fun onAuthenticationFailed() {
            deferred.cancel()
            activity.finish()
        }
    }

    /**
     * For completeness we provide a shutdown function.
     * In practice, we initialize the executor only when it is first used,
     * and keep it alive throughout the app lifecycle, as it will be used an arbitrary number of times,
     * with unknown frequency
     */
    @Synchronized
    fun shutdown() {
        pool?.shutdown()
        pool = null
    }
}