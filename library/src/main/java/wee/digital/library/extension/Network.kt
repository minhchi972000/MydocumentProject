package wee.digital.library.extension

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.MutableLiveData
import wee.digital.library.app

val connectivityManager: ConnectivityManager
    get() = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

val connectionInfo: String?
    @SuppressLint("MissingPermission")
    get() {
        val cm = connectivityManager
        when {
            Build.VERSION.SDK_INT > Build.VERSION_CODES.O -> {
                val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return null
                capabilities.run {
                    return when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                        else -> null
                    }
                }
            }
            Build.VERSION.SDK_INT > Build.VERSION_CODES.M -> @Suppress("DEPRECATION") {
                val networkInfo = cm.activeNetworkInfo ?: return null
                return when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> "wifi"
                    ConnectivityManager.TYPE_MOBILE -> "mobile"
                    else -> null
                }
            }
            else -> @Suppress("DEPRECATION") {
                if (cm.activeNetworkInfo?.isConnected == true) {
                    return "is connected"
                }
            }
        }
        return null
    }

val networkLiveData: MutableLiveData<Boolean> by lazy {
    registerNetworkCallback()
    MutableLiveData<Boolean>(networkConnected)
}

val networkAvailableLiveData = SingleLiveData(networkConnected)

val hasWifi: Boolean get() = connectionInfo == "wifi" || connectionInfo == "is connected"

val networkConnected: Boolean
    get() = connectionInfo != null

val networkDisconnected: Boolean
    get() = connectionInfo == null

fun registerNetworkCallback() {
    registerNetworkCallback(object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networkLiveData.postValue(true)
            networkAvailableLiveData.postValue(true)
        }

        override fun onLost(network: Network) {
            networkLiveData.postValue(false)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            print("hasCellular: $hasCellular")

            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            print("hasWifi: $hasWifi")
        }
    })
}

fun registerNetworkCallback(callback: ConnectivityManager.NetworkCallback) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        connectivityManager.registerDefaultNetworkCallback(callback)
    } else {
        val request: NetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }
}

abstract class SimpleNetworkCallback : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
    }

    override fun onLost(network: Network) {
    }
}

@Suppress("DEPRECATION")
val networkReceiver = object : BroadcastReceiver() {
    fun register() {
        app.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onReceive(context: Context, intent: Intent) {
    }
}



