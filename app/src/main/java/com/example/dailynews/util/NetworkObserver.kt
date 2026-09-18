package com.example.dailynews.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkObserver(context: Context) {

    private val manager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {

        fun current(): Boolean {
            val network = manager.activeNetwork ?: return false
            val capabilities = manager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
        }

        trySend(current())

        val callback = object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(current())
            }
        }

        manager.registerDefaultNetworkCallback(callback)

        awaitClose {
            manager.unregisterNetworkCallback(callback)
        }
    }
}
