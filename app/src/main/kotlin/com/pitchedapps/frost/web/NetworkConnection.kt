package com.pitchedapps.frost.web

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import com.pitchedapps.frost.utils.L
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FrostNetworkModule {
    @Provides
    @Singleton
    @FrostNetworkPrivate
    fun refreshMutableFlow(): MutableSharedFlow<NetworkConnection.Type> =
        MutableStateFlow(NetworkConnection.Type.Unmetered)

    @Provides
    @Singleton
    @FrostNetwork
    fun refreshFlow(
        @FrostNetworkPrivate mutableFlow: MutableSharedFlow<NetworkConnection.Type>
    ): SharedFlow<NetworkConnection.Type> = mutableFlow.asSharedFlow()

    @Provides
    @Singleton
    @FrostNetwork
    fun refreshEmit(
        @FrostNetworkPrivate mutableFlow: MutableSharedFlow<NetworkConnection.Type>
    ): FrostEmitter<NetworkConnection.Type> = mutableFlow.asFrostEmitter()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class FrostNetworkPrivate

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FrostNetwork

@Singleton
class NetworkConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    @FrostNetwork val networkEmit: FrostEmitter<Boolean>
) {

    enum class Type {
        Unmetered, Metered, Disconnected
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun Network.debug() {
        val capabilities =
            connectivityManager.getNetworkCapabilities(this) ?: return L._d { "No capabilities" }
        mapOf(
            NetworkCapabilities.NET_CAPABILITY_INTERNET to "internet",
            NetworkCapabilities.NET_CAPABILITY_NOT_METERED to "not metered",
            NetworkCapabilities.TRANSPORT_WIFI to "wifi",
            NetworkCapabilities.TRANSPORT_CELLULAR to "cellular",
        ).forEach { (c, tag) ->
            L._d { "Capability $tag ${capabilities.hasCapability(c)}" }
        }
    }

    private fun Network.toType(): Type? {
        val capabilities = connectivityManager.getNetworkCapabilities(this) ?: return null
        if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) return Type.Disconnected
        return when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED) -> Type.Unmetered
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return null
    }

    private val networkCallback: ConnectivityManager.NetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                L._d { "Network available" }
                network.debug()
                networkEmit(true)
            }

            override fun onLost(network: Network) {
                L._d { "Network lost" }
                network.debug()
                networkEmit(false)
            }
        }
    }

    fun start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            connectivityManager.unregisterNetworkCallback(networkCallback)
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