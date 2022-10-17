package wee.digital.library.extension

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import wee.digital.library.app
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.reflect.KClass


fun <T : Activity> Fragment.start(cls: KClass<T>) {
    requireActivity().start(cls)
}

fun <T : Activity> Activity.start(cls: KClass<T>) {
    startActivity(Intent(this, cls.java))
}

fun navigateSettings(intentString: String?) {
    val intent = Intent(intentString)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    app.startActivity(intent)
}

fun navigateSettings() {
    navigateSettings(Settings.ACTION_SETTINGS)
}

fun navigateLocationSettings() {
    navigateSettings(Settings.ACTION_LOCALE_SETTINGS)
}

fun navigateSecuritySettings() {
    navigateSettings(Settings.ACTION_SECURITY_SETTINGS)
}

fun navigateWifiSettings() {
    navigateSettings(Settings.ACTION_WIFI_SETTINGS)
}

fun navigateBluetoothSettings() {
    navigateSettings(Settings.ACTION_BLUETOOTH_SETTINGS)
}

fun navigateEmail() {
    app.startActivity(Intent(Intent.ACTION_MAIN).also {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        it.addCategory(Intent.CATEGORY_APP_EMAIL)
    })
}

fun navigateCHPlay() {
    try {
        app.startActivity(Intent(Intent.ACTION_VIEW).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.data = Uri.parse("market://details?id=${app.packageName}")
        })
    } catch (ex: android.content.ActivityNotFoundException) {
        app.startActivity(Intent(Intent.ACTION_VIEW).also {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.data = Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")
        })
    }
}

fun navigateBrowser(url: String) {
    app.startActivity(Intent(Intent.ACTION_VIEW).also {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        it.data = Uri.parse(url)
    })
}

fun navigateCall(phone: String) {
    app.startActivity(Intent(Intent.ACTION_DIAL).also {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        it.data = Uri.parse("tel:$phone")
    })
}

fun navigateDateSettings() {
    navigateSettings(Settings.ACTION_DATE_SETTINGS)
}

fun navigateAppSettings() {
    app.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        it.data = Uri.fromParts("package", app.packageName, null)
    })
}

fun realPathFromURI(uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val cursor: Cursor = app.contentResolver.query(uri, projection, null, null, null)
        ?: return uri.path
    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
    cursor.moveToFirst()
    return cursor.getString(columnIndex)
}

fun openApplication(pkg: String){
    try {
        openApplicationOrThrow(pkg)
    }catch (e: Exception){

    }
}

fun openApplicationOrThrow(pkg: String){
    val intent = app.packageManager.getLaunchIntentForPackage(pkg)
    app.startActivity(intent)
}

fun openApplicationOrElse(pkg: String) : Boolean{
    return try {
        openApplicationOrThrow(pkg)
        false
    }catch (e: Exception){
        true
    }
}

/**
 * include in dependency [androidx.activity:activity-ktx:X.X.X]
 */
class IntentResultLauncher {

    private var launcher: ActivityResultLauncher<Intent>? = null

    fun observer(activity: ComponentActivity, callBack: (Intent) -> Unit) {

        val resultContracts = ActivityResultContracts.StartActivityForResult()

        val callback = ActivityResultCallback<ActivityResult> {
            val data: Intent = it?.data ?: return@ActivityResultCallback
            callBack(data)
        }

        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                launcher = activity.registerForActivityResult(resultContracts, callback)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                launcher?.unregister()
            }
        })
    }

    fun observer(fragment: Fragment, callBack: (Intent) -> Unit) {

        val resultContracts = ActivityResultContracts.StartActivityForResult()

        val callback = ActivityResultCallback<ActivityResult> { result ->
            val data: Intent = result?.data ?: return@ActivityResultCallback
            callBack(data)
        }

        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                launcher = fragment.registerForActivityResult(resultContracts, callback)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                launcher?.unregister()
            }
        })
    }
}

class ImagePickerJob(val lifecycleOwner: LifecycleOwner) {

    init {
        observer(lifecycleOwner)
    }

    private val imagePickerIntent: Intent
        get() {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
            }
            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
                    it.type = "image/*"
                }
            return Intent.createChooser(getIntent, "Select Image").also {
                it.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
            }
        }

    private var launcher: ActivityResultLauncher<Intent>? = null

    private var callBack: (Bitmap) -> Unit = {}

    private fun observer(lifecycleOwner: LifecycleOwner) {

        val forResult = ActivityResultContracts.StartActivityForResult()

        val callback = ActivityResultCallback<ActivityResult> {
            getImageFromIntent(it)?.also { image ->
                callBack(image)
            }
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                launcher = when (lifecycleOwner) {
                    is ComponentActivity -> lifecycleOwner.registerForActivityResult(
                        forResult,
                        callback
                    )
                    is Fragment -> lifecycleOwner.registerForActivityResult(forResult, callback)
                    else -> null
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                launcher?.unregister()
            }
        })
    }

    private fun getImageFromIntent(result: ActivityResult?): Bitmap? {
        val data: Intent = result?.data ?: return null
        val uri: Uri = data.data ?: return null
        val outputStream = ByteArrayOutputStream()
        return try {
            val path: String = realPathFromURI(uri) ?: return null
            val file = File(path)
            val inputStream = FileInputStream(file)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            bitmap
        } catch (ignore: IOException) {
            null
        } finally {
            outputStream.safeClose()
        }
    }

    fun startForResult(callBack: (Bitmap) -> Unit) {
        this.callBack = callBack
        launcher?.launch(imagePickerIntent)
    }
}

class VoicePickerJob(val lifecycleOwner: LifecycleOwner) {

    init {
        observer(lifecycleOwner)
    }

    private var launcher: ActivityResultLauncher<Intent>? = null

    private var callBack: () -> Unit = {}

    private fun observer(lifecycleOwner: LifecycleOwner) {

        val forResult = ActivityResultContracts.StartActivityForResult()

        val callback = ActivityResultCallback<ActivityResult> {
            println("")
        }

        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                launcher = when (lifecycleOwner) {
                    is ComponentActivity -> lifecycleOwner.registerForActivityResult(
                        forResult,
                        callback
                    )
                    is Fragment -> lifecycleOwner.registerForActivityResult(forResult, callback)
                    else -> null
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                launcher?.unregister()
            }
        })
    }

    private fun getImageFromIntent(result: ActivityResult?): Bitmap? {
        val data: Intent = result?.data ?: return null
        val uri: Uri = data.data ?: return null
        val outputStream = ByteArrayOutputStream()
        return try {
            val path = realPathFromURI(uri) ?: return null
            val file = File(path)
            val inputStream = FileInputStream(file)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            bitmap
        } catch (ignore: IOException) {
            null
        } finally {
            outputStream.safeClose()
        }
    }

    fun startForResult(callBack: () -> Unit) {
        this.callBack = callBack
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
            it.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            it.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech")
        }
        launcher?.launch(intent)
    }
}
