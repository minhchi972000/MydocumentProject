package wee.digital.library

import android.app.Application
import wee.digital.library.extension.registerNetworkCallback

object Library {

    fun init(application: Application) {
        mApp = application
        registerNetworkCallback()
    }
}

private var mApp: Application? = null

val app: Application
    get() = mApp ?: throw NullPointerException(
        "Library module must be init with " +
                "Library.init(application: Application) in android.app.Application.onCreate()"
    )