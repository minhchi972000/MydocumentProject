package wee.digital.sample.data.firebase

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.*
import wee.digital.library.extension.toast
import wee.digital.sample.app
import wee.digital.sample.pref

val currentUser get() = auth.currentUser

val auth get() = Firebase.auth

var fcmToken: String?
    get() = pref.str("fcmToken")
    set(value) {
        pref.putStr("fcmToken", value)
    }

var installationId: String?
    get() = pref.str("installationId")
    set(value) {
        pref.putStr("installationId", value)
    }

private fun d(s: String) {
    Log.d("Firebase", s)
}

fun onFirebaseAppInit(onCompleted: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        FirebaseApp.initializeApp(app)
        delay(300)
        try {
            if (installationId.isNullOrEmpty()) installationId = withContext(Dispatchers.IO) {
                Tasks.await(FirebaseInstallations.getInstance().id)
            }
            if (fcmToken.isNullOrEmpty()) fcmToken = withContext(Dispatchers.IO) {
                Tasks.await(Firebase.messaging.token)
            }
            d("InstallationId: $installationId")
            d("FcmToken: $fcmToken")
        } catch (e: Exception) {
            toast(e.message)
        }
        onCompleted()
    }
}
