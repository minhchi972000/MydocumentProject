package wee.digital.sample.ui.fragment.camera1

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import wee.digital.sample.utils.requestPermissions
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraUtils(
    private val fragment: Fragment,
    private val previewView: PreviewView,
    private val onResult: (String?) -> Unit
) {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> = fragment.run {
        return@run requestPermissions(
            onGranted = {
                startCamera()
            },
            onDined = {
                showDialogRequest()
            })
    }

    companion object {
        var PERMISSION_CAMERA = Manifest.permission.CAMERA
    }

    init {
        requestPermissionsLauncher.launch(arrayOf(PERMISSION_CAMERA))
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(fragment.requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(fragment.requireContext()))
    }

    private fun stopCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(fragment.requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(fragment.requireContext()))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        cameraProvider ?: return
        // Preview
        val preview = Preview.Builder().build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Select back camera as a default
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()
            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                fragment.viewLifecycleOwner,
                cameraSelector,
                preview,
            )

        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    private fun showDialogRequest() {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle("Permission required")
            .setMessage("This application needs to access the camera to process barcodes")
            .setPositiveButton("Ok") { _, _ ->
                // Keep asking for permission until granted
                requestPermissionsLauncher.launch(arrayOf(PERMISSION_CAMERA))
            }
            .setCancelable(false)
            .create()
            .apply {
                setCanceledOnTouchOutside(false)
                show()
            }
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }


    fun scanUri(uri: Uri?) {
        uri ?: return
        try {
            val image = InputImage.fromFilePath(fragment.requireContext(), uri)
            CoroutineScope(Dispatchers.IO).launch {
                  scanImage(image)
            }
        } catch (ex: Exception) {
            Log.d("scanUri: ",ex.message.toString())
        }
    }

    private fun scanImage(image: InputImage) {

    }

    fun Uri.toBitmap(): Bitmap {
        val bitmap = BitmapFactory.decodeStream(
            this@CameraUtils.fragment.requireContext().contentResolver.openInputStream(this)
        )
        val blob = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 100, blob)
        return bitmap
    }

}

