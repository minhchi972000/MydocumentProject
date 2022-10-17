package wee.digital.library.extension

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import wee.digital.library.app
import java.util.*

val locationPermission
    get() = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

private fun requestedPermission(): Array<String> {
    return app.packageManager
        .getPackageInfo(app.packageName, PackageManager.GET_PERMISSIONS)
        .requestedPermissions
}

private fun LifecycleOwner.showDialogPermission(permissions: List<String>) {
    val sb = StringBuilder()
    permissions.iterator().forEach { permission ->
        val s = permission.replace("android.permission.", "")
            .replace("_", " ").lowercase(Locale.getDefault())
        sb.append(" $s,")
    }
    sb.deleteCharAt(sb.lastIndex)
    val permissionsText = sb.toString()
    val activity = requireActivity() ?: return
    AlertDialog.Builder(activity)
        .setMessage("Permission:$permissionsText had been denied")
        .setPositiveButton("Close") { dialog, _ -> dialog.cancel() }
        .setNegativeButton("Setting") { dialog, _ ->
            dialog.cancel()
            activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.data = Uri.fromParts("package", activity.packageName, null)
            })
        }.show()
}

fun isGranted(vararg permissions: String): Boolean {
    permissions.iterator().forEach {
        if (ContextCompat.checkSelfPermission(app, it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun Fragment.onGrantedPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
    onDenied: (List<String>) -> Unit
) {
    val deniedPermissions = mutableListOf<String>()
    val notGrantedPermissions = mutableListOf<String>()
    for (permission in permissions) {
        when {
            isGranted(permission) -> {
                continue
            }
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission) -> {
                deniedPermissions.add(permission)
            }
            else -> {
                notGrantedPermissions.add(permission)
            }
        }
    }
    permissions.forEach {
        when {
            isGranted(it) -> {
                //continue
            }
            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), it) -> {
                deniedPermissions.add(it)
            }
            else -> {
                notGrantedPermissions.add(it)
            }
        }
    }
    if (notGrantedPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(
            requireActivity(),
            notGrantedPermissions.toTypedArray(),
            101
        )
        return
    }
    if (deniedPermissions.isNotEmpty()) {
        onDenied(deniedPermissions.toList())
        return
    }
    onGranted.invoke()
}

fun Fragment.observerPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
    onDenied: (List<String>) -> Unit
) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            if (isGranted(*permissions)) {
                onGranted.invoke()
            }
        }
    })
    if (!isGranted(*permissions)) {
        onGrantedPermission(permissions = permissions, onGranted = { }, onDenied = onDenied)
    }
}

fun FragmentActivity.onGrantedPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
    onDenied: (List<String>) -> Unit
) {
    val deniedPermissions = mutableListOf<String>()
    val notGrantedPermissions = mutableListOf<String>()
    permissions.forEach {
        when {
            isGranted(it) -> {
                //continue
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, it) -> {
                deniedPermissions.add(it)
            }
            else -> {
                notGrantedPermissions.add(it)
            }
        }
    }
    if (notGrantedPermissions.isNotEmpty()) {
        ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), 101)
        return
    }
    if (deniedPermissions.isNotEmpty()) {
        onDenied(deniedPermissions.toList())
        return
    }
    onGranted.invoke()
}

fun FragmentActivity.observerPermission(
    vararg permissions: String,
    onGranted: () -> Unit,
    onDenied: (List<String>) -> Unit
) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            if (isGranted(*permissions)) {
                onGranted.invoke()
            }
        }
    })
    if (!isGranted(*permissions)) {
        onGrantedPermission(permissions = permissions, onGranted = { }, onDenied = onDenied)
    }
}

class PermissionHandler(val lifecycleOwner: LifecycleOwner) {

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    var requestLauncher: ActivityResultLauncher<String>? = null

    private var onGranted: (() -> Unit)? = null

    private var onDenied: ((List<String>) -> Unit)? = null

    init {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                val request = ActivityResultContracts.RequestPermission()
                val callback = ActivityResultCallback<Boolean> { isGranted ->
                    if (isGranted) {
                        onGranted?.invoke()
                    }
                }
                (lifecycleOwner as? Fragment)?.also {
                    requestLauncher = it.registerForActivityResult(request, callback)
                    return
                }
                (lifecycleOwner as? FragmentActivity)?.also {
                    requestLauncher = it.registerForActivityResult(request, callback)
                    return
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                requestLauncher?.unregister()
            }
        })
    }

    fun launchWhenGranted(
        permission: String,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        this.onGranted = onGranted
        this.onDenied = onDenied
        requestLauncher?.launch(permission)
    }

    fun launchWhenResumed(
        permission: String,
        onGranted: () -> Unit,
        onDenied: (List<String>) -> Unit
    ) {
        this.onGranted = { }
        this.onDenied = onDenied
        lifecycleOwner.lifecycleScope.launchWhenResumed {
            if (isGranted(permission)) {
                onGranted()
            }
        }
        launchWhenGranted(permission, { }, onDenied)
    }

}







