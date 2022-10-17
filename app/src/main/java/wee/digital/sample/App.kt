package wee.digital.sample

import android.app.Activity
import androidx.multidex.MultiDexApplication
import wee.digital.library.Library
import wee.digital.library.util.Logger
import wee.digital.library.util.SharedPref
import wee.digital.widget.Widget
import java.lang.ref.WeakReference


class App : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        app = this
        Library.init(this)
        Widget.init(this)
    }

}

lateinit var app: App private set

val log by lazy { Logger("sampleApp") }

val pref by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { SharedPref(BuildConfig.APPLICATION_ID) }

private var activityReference: WeakReference<Activity>? = null

val currentActivity: Activity? get() = activityReference?.get()

const val appId = BuildConfig.APPLICATION_ID

const val dbVersion = 1

const val serviceUrl = ""

val hasSample: Boolean get() = false

val sampleDelayed: Long get() = 2000

val isDebug get() = BuildConfig.DEBUG

val flavor get() = BuildConfig.FLAVOR

val versionCode get() = BuildConfig.VERSION_CODE

val versionName get() = BuildConfig.VERSION_NAME

