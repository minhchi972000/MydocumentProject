package vn.wee.facepay.ui.profile.camera

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.view.Window
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import vn.wee.facepay.databinding.CaptureAvatarBinding
import vn.wee.facepay.ui.base.Inflating
import vn.wee.facepay.ui.dialog.image.ImageFragment
import vn.wee.facepay.ui.main.MainDialog
import wee.digital.camera.camera.toByteArray
import wee.digital.library.extension.*
import wee.digital.sample.ui.fragment.camera.CameraController
import wee.digital.widget.extension.HoleCircle
import wee.digital.widget.util.logDebug


class CaptureFragment : MainDialog<CaptureAvatarBinding>(), CameraController.ViewInterface,
    CameraController.CallBack {

    private val cameraController = CameraController(this@CaptureFragment)
    private var firstOpen = true
    private val sizePreview by lazy { intArrayOf(vb.previewView.width,vb.previewView.height) }

    override fun inflating(): Inflating = CaptureAvatarBinding::inflate

    override fun onViewCreated() {
        addClickListener(vb.viewAccept, vb.viewCaptureArrowDown)
    }

    override fun onWindowConfig(window: Window) {
        statusBarColor(Color.BLACK)
        lightStatusBarWidgets()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        statusBarColor(Color.WHITE)
        darkStatusBarWidgets()
    }

    override fun onResume() {
        super.onResume()
        observerPermission(
            Manifest.permission.CAMERA,
            onGranted = {
                launchCamera()
            },
            onDenied = {
                logDebug("Permission denied!")
                //Optional
                if (firstOpen) {
                    firstOpen = false
                    try {
                        startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", requireContext().packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    } catch (_: Exception) {
                    }

                }
            })
    }

    private fun launchCamera() {
        lifecycleScope.launch {
            vb.hvQRCode.holeCircle = HoleCircle(
                vb.viewCapture,
                padding = 0F
            )
            cameraController.start()
            cameraController.callback = this@CaptureFragment
        }
    }

    override fun cameraLifecycleOwner(): LifecycleOwner {
        return this
    }

    override fun cameraPreviewView(): PreviewView {
        return vb.previewView
    }

    override fun cameraImageAnalysis(): Boolean {
        return true
    }

    override fun onViewClick(v: View?) {
        when(v){
            vb.viewCaptureArrowDown->{
                onBackPressed()
            }
            vb.viewAccept->{
                cameraController.takePicture(vb.viewCapture, sizePreview, onSuccess = {
                    imageVM.imageCaptureLiveData.value = it.toByteArray()
                    show(ImageFragment())
                }, onError = {
                    println(it.message)
                })
            }
        }
    }

}
