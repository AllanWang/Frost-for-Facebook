package com.pitchedapps.frost.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import kotlinx.coroutines.channels.Channel

class ConnectivityObserver(
    val context: Context,
    val channel: Channel<Boolean>
) {

    private val connectivityManager
        get() = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Suppress("DEPRECATION")
    private val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)

    private val broadCastReceiver by lazy {
        object : BroadcastReceiver() {
            @Suppress("DEPRECATION")
            override fun onReceive(context: Context?, intent: Intent?) {
                if (ConnectivityManager.CONNECTIVITY_ACTION != intent?.action) {
                    return
                }
                val networkInfo = connectivityManager.activeNetworkInfo
                val connected = networkInfo?.isConnectedOrConnecting == true
                channel.offer(connected)
            }
        }
    }

    private val networkCallback: ConnectivityManager.NetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                channel.offer(true)
            }

            override fun onLost(network: Network?) {
                channel.offer(false)
            }
        }
    }

    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            context.applicationContext.registerReceiver(broadCastReceiver, intentFilter)
        } else {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            context.applicationContext.unregisterReceiver(broadCastReceiver)
        } else {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}