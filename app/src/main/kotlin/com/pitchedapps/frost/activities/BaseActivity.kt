package com.pitchedapps.frost.activities

import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.pitchedapps.frost.R
import com.pitchedapps.frost.utils.L
import com.pitchedapps.frost.utils.Prefs
import com.pitchedapps.frost.utils.materialDialogThemed
import com.pitchedapps.frost.utils.setFrostTheme
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Allan Wang on 2017-06-12.
 */
abstract class BaseActivity : KauBaseActivity() {
    override fun onBackPressed() {
        if (isTaskRoot && Prefs.exitConfirmation) {
            materialDialogThemed {
                title(R.string.kau_exit)
                content(R.string.kau_exit_confirmation)
                positiveText(R.string.kau_yes)
                negativeText(R.string.kau_no)
                onPositive { _, _ -> super.onBackPressed() }
                checkBoxPromptRes(R.string.kau_do_not_show_again, false, { _, b -> Prefs.exitConfirmation = !b })
            }
        } else super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFrostTheme()
    }

    private var networkDisposable: Disposable? = null
    private var networkConsumer: ((Connectivity) -> Unit)? = null

    fun setNetworkObserver(consumer: (connectivity: Connectivity) -> Unit) {
        this.networkConsumer = consumer
    }

    private fun observeNetworkConnectivity() {
        val consumer = networkConsumer ?: return
        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    connectivity: Connectivity ->
                    connectivity.apply {
                        L.d("Network connectivity changed: isAvailable: $isAvailable isRoaming: $isRoaming")
                        consumer(connectivity)
                    }
                }
    }

    private fun disposeNetworkConnectivity() {
        if (networkDisposable?.isDisposed == false)
            networkDisposable?.dispose()
        networkDisposable = null
    }

    override fun onResume() {
        super.onResume()
        disposeNetworkConnectivity()
        observeNetworkConnectivity()
    }

    override fun onPause() {
        super.onPause()
        disposeNetworkConnectivity()
    }
}