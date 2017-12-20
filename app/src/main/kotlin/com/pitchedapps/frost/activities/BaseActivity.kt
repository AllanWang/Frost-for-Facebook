package com.pitchedapps.frost.activities

import android.content.res.Configuration
import android.os.Bundle
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.searchview.SearchViewHolder
import com.pitchedapps.frost.contracts.VideoViewHolder
import com.pitchedapps.frost.utils.setFrostTheme

/**
 * Created by Allan Wang on 2017-06-12.
 */
abstract class BaseActivity : KauBaseActivity() {

    /**
     * Inherited consumer to customize back press
     */
    protected open fun backConsumer(): Boolean = false

    override final fun onBackPressed() {
        if (this is SearchViewHolder && searchViewOnBackPress()) return
        if (this is VideoViewHolder && videoOnBackPress()) return
        if (backConsumer()) return
        super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this !is WebOverlayActivityBase) setFrostTheme()
    }

//
//    private var networkDisposable: Disposable? = null
//    private var networkConsumer: ((Connectivity) -> Unit)? = null
//
//    fun setNetworkObserver(consumer: (connectivity: Connectivity) -> Unit) {
//        this.networkConsumer = consumer
//    }
//
//    private fun observeNetworkConnectivity() {
//        val consumer = networkConsumer ?: return
//        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(applicationContext)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { connectivity: Connectivity ->
//                    connectivity.apply {
//                        L.d("Network connectivity changed: isAvailable: $isAvailable isRoaming: $isRoaming")
//                        consumer(connectivity)
//                    }
//                }
//    }
//
//    private fun disposeNetworkConnectivity() {
//        if (networkDisposable?.isDisposed == false)
//            networkDisposable?.dispose()
//        networkDisposable = null
//    }
//
//    override fun onResume() {
//        super.onResume()
////        disposeNetworkConnectivity()
////        observeNetworkConnectivity()
//    }
//
//    override fun onPause() {
//        super.onPause()
////        disposeNetworkConnectivity()
//    }


    override fun onStop() {
        if (this is VideoViewHolder) videoOnStop()
        super.onStop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (this is VideoViewHolder) videoViewer?.updateLocation()
    }
}