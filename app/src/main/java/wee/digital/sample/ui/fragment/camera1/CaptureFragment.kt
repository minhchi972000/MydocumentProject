package wee.digital.sample.ui.fragment.camera1

import android.app.Activity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import wee.digital.library.extension.darkStatusBarWidgets
import wee.digital.library.extension.lightStatusBarWidgets
import wee.digital.sample.R
import wee.digital.sample.databinding.CaptureAvatarBinding
import wee.digital.sample.ui.base.Inflating
import wee.digital.sample.ui.main.MainDialog
import wee.digital.sample.utils.requestActivityResult
import wee.digital.widget.extension.HoleCircle

class CaptureFragment : MainDialog<CaptureAvatarBinding>() {

    private var cameraUtils: CameraUtils? = null

    private var chooseImageLauncher = requestActivityResult {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data
            //barcode?.scanUri(uri)
        }
    }

    override fun inflating(): Inflating = CaptureAvatarBinding::inflate

    override fun onStart() {
        super.onStart()
        context?.let {
            activity?.window?.statusBarColor = ContextCompat.getColor(it, R.color.color_black)
        }
        lightStatusBarWidgets()
    }

    override fun onViewCreated() {
        lifecycleScope.launch {
            vb.hvQRCode.holeCircle = HoleCircle(
                vb.viewCapture,
                padding = 0F
            )
            cameraUtils = CameraUtils(this@CaptureFragment, vb.previewView) {

            }
        }
    }

    override fun onDestroyView() {
        context?.let {
            activity?.window?.statusBarColor = ContextCompat.getColor(it, R.color.color_white)
        }
        darkStatusBarWidgets()
        super.onDestroyView()
        cameraUtils?.shutdown()
    }

}