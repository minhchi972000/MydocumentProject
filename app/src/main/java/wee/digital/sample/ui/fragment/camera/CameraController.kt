package wee.digital.sample.ui.fragment.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.View
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.WindowMetricsCalculator
import kotlinx.coroutines.*
import wee.digital.sample.app
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * implementation "androidx.window:window:1.1.0-alpha02"
 * def camerax_version = '1.1.0'
 * implementation "androidx.camera:camera-core:$camerax_version"
 * implementation "androidx.camera:camera-camera2:$camerax_version"
 * implementation "androidx.camera:camera-lifecycle:$camerax_version"
 * implementation "androidx.camera:camera-view:$camerax_version"
 */
class CameraController(private val view: ViewInterface) {

    interface ViewInterface {
        fun cameraLifecycleOwner(): LifecycleOwner
        fun cameraPreviewView(): PreviewView
        fun cameraImageAnalysis(): Boolean = false
        fun cameraDisplayRotation(): Int = cameraPreviewView().display.rotation

        fun cameraAspectRatio(): Int {
            //return AspectRatio.RATIO_4_3
            val activity: Activity =
                cameraPreviewView().context as? Activity ?: return AspectRatio.RATIO_4_3
            val wmc = WindowMetricsCalculator.getOrCreate()

            val metrics = wmc.computeCurrentWindowMetrics(activity).bounds
            //val size = Size(640, 480)
            val previewRatio = max(metrics.width(), metrics.height()).toDouble() / min(
                metrics.width(),
                metrics.height()
            )
            if (abs(previewRatio - 4.0 / 3) <= abs(previewRatio - 16.0 / 9)) {
                return AspectRatio.RATIO_4_3
            }
            return AspectRatio.RATIO_16_9
        }

        fun viewLaunch(block: suspend CoroutineScope.() -> Unit) {
            cameraLifecycleOwner().lifecycleScope.launch(
                Dispatchers.Main,
                CoroutineStart.DEFAULT,
                block
            )
        }
    }

    interface CallBack {
        fun cameraOnImageAnalysis(imageProxy: ImageProxy) = Unit
        fun cameraOnStateChanged(state: String) = Unit
        fun cameraOnError(error: String?) = Unit
        fun cameraOnPictureTaken(bitmap: Bitmap) = Unit

    }


    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvideJob: Job? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var targetRotation: Int = 0
    private val displayManager: DisplayManager get() = app.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    var callback: CallBack? = null
    private var analysisExecutor: ExecutorService? = null
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(id: Int) = Unit
        override fun onDisplayRemoved(id: Int) = Unit
        override fun onDisplayChanged(id: Int) {
            try {
                if (id == view.cameraPreviewView().display.displayId) {
                    targetRotation = view.cameraDisplayRotation()
                }
            } catch (_: Exception) {

            }

        }
    }

    init {
        displayManager.registerDisplayListener(displayListener, null)
    }

    fun start() {
        cameraProvideJob?.cancel()
        cameraProvideJob = view.cameraLifecycleOwner().lifecycleScope.launch(Dispatchers.Main) {
            try {
                cameraProvider =
                    withContext(Dispatchers.IO) { ProcessCameraProvider.getInstance(app).get() }
//                lensFacing = when {
//                    cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true -> CameraSelector.LENS_FACING_BACK
//                    cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true -> CameraSelector.LENS_FACING_FRONT
//                    else -> throw IllegalStateException("Camera not found")
//                }
                // init camera use case
                camera = initUseCaseAndStart()
                camera?.cameraInfo?.cameraState?.observe(view.cameraLifecycleOwner()) { state ->
                    view.viewLaunch {
                        if (state.error != null) {
                            callback?.cameraOnError(state.error.toString())
                        } else {
                            callback?.cameraOnStateChanged(state.type.toString())
                        }
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    fun stop() {
        cameraProvideJob?.cancel()
        cameraProvider?.unbindAll()
        analysisExecutor?.shutdown()
        displayManager.unregisterDisplayListener(displayListener)
    }

    fun takePicture(
        viewCapture: View,
        sizePreview: IntArray,
        onSuccess: ((Bitmap) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val executor = Executors.newSingleThreadExecutor()
        imageCapture?.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(e: ImageCaptureException) {
                    executor.shutdown()
                    view.viewLaunch {
                        onError(e)
                        callback?.cameraOnError(e.message)
                    }

                }

                @SuppressLint("UnsafeOptInUsageError")
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        executor.shutdown()
                        val rotation = image.imageInfo.rotationDegrees
                        val bitmap = image.image?.toBitmap().rotate(rotation)
                            ?: throw NullPointerException("bitmap is null")

                        val rectBitmap = calculateRectBimap(bitmap, sizePreview, viewCapture)
                        val bitmapFinal = cropBitmapWithRect(bitmap, Rect(rectBitmap)) ?: bitmap
                        view.viewLaunch {
                            onSuccess?.invoke(bitmapFinal)
                            callback?.cameraOnPictureTaken(bitmap)
                        }
                    } catch (e: Exception) {
                        view.viewLaunch {
                            onError?.invoke(e)
                            callback?.cameraOnError(e.message)
                        }
                    }
                    image.close()
                }

            })

    }

    fun cropBitmapWithRect(bitmap: Bitmap, rect: Rect): Bitmap? {
        return try {
            val cropBitmap = Bitmap.createBitmap(
                bitmap,
                rect.left, rect.top, rect.width(), rect.height()
            )
            cropBitmap
        } catch (ex: Exception) {
            Log.d("getFaceResult", ex.message.toString())
            null
        }
    }

    private fun calculateRectBimap(bitmap: Bitmap, sizeScreen: IntArray, viewCapture: View): Rect {
        val centerBitmap = bitmap.width / 2
        val wBimap =
            (bitmap.height * (sizeScreen[0].toFloat() / sizeScreen[1].toFloat())).toInt()

        val ratioW = sizeScreen[0] / wBimap.toFloat()
        val ratioH = sizeScreen[1] / bitmap.height.toFloat()

        val wViewNew = (viewCapture.width / ratioW).toInt()

        val leftNew = centerBitmap - (wViewNew / 2)
        val topNew = (viewCapture.top / ratioH).toInt()
        val rightNew = leftNew + wViewNew
        val bottomNew = topNew + wViewNew

        return Rect(
            leftNew,
            topNew,
            rightNew,
            bottomNew
        )
    }

    fun takePictureAndSave(
        onSuccess: ((Bitmap) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val imageFile: File = createImageFile()
        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(imageFile)
            .setMetadata(metadata)
            .build()
        val executor = Executors.newSingleThreadExecutor()
        view.cameraLifecycleOwner().lifecycleScope.launch(Dispatchers.IO) {
            imageCapture?.takePicture(
                outputOptions,
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(e: ImageCaptureException) {
                        executor.shutdown()
                        view.viewLaunch {
                            onError(e)
                            callback?.cameraOnError(e.message)
                        }
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        executor.shutdown()
                        try {
                            val savedUri = output.savedUri ?: Uri.fromFile(imageFile)
                            // Bitmap type hardware
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source =
                                    ImageDecoder.createSource(app.contentResolver, savedUri)
                                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                                    decoder.isMutableRequired = true
                                }
                            } else {
                                MediaStore.Images.Media.getBitmap(app.contentResolver, savedUri)
                            }
                            view.viewLaunch {
                                onSuccess?.invoke(bitmap)
                                callback?.cameraOnPictureTaken(bitmap)
                            }

                        } catch (e: Exception) {
                            view.viewLaunch {
                                onError?.invoke(e)
                                callback?.cameraOnError(e.message)
                            }

                        }
                    }
                }
            )
        }

    }

    fun Bitmap?.rotate(degrees: Int): Bitmap? {
        this ?: return null
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
    }

    fun Image.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun switchLens() {
        stop()
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        start()
    }

    @SuppressLint("RestrictedApi")
    private fun initUseCaseAndStart(): Camera? {

        //val size = Size(640, 480)

        val aspectRatio: Int = view.cameraAspectRatio()
        val rotation: Int = view.cameraDisplayRotation()
        val size = Size(1280, 720)

        val useCases = mutableListOf<UseCase>()

        val preview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .setTargetRotation(rotation)
            .build()


        useCases.add(preview)

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(aspectRatio)
            .build()
        useCases.add(imageCapture!!)


        /**
         * [androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888]
         * [androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888]
         */
        if (view.cameraImageAnalysis()) {
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetRotation(rotation)
                //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            analysisExecutor = Executors.newSingleThreadExecutor()
            imageAnalysis?.setAnalyzer(analysisExecutor!!) { imageProxy: ImageProxy ->
                callback?.also {
                    it.cameraOnImageAnalysis(imageProxy)
                }

            }
            useCases.add(imageAnalysis!!)
        }

        cameraProvider?.unbindAll()

        preview?.setSurfaceProvider(view.cameraPreviewView().surfaceProvider)
        return cameraProvider?.safeBindUseCase(*useCases.toTypedArray())
    }

    private fun ProcessCameraProvider?.safeUnbind(vararg arr: UseCase?) {
        arr.filterNotNull().forEach {
            try {
                this?.unbind(it)
            } catch (e: Exception) {
                callback?.cameraOnError(e.message)
            }
        }
    }

    private fun ProcessCameraProvider?.safeBindUseCase(vararg arr: UseCase?): Camera? {
        return try {
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            val useCases = arr.filterNotNull().toTypedArray()
            if (useCases.isEmpty()) return null
            this?.bindToLifecycle(
                view.cameraLifecycleOwner(),
                cameraSelector,
                *useCases
            )
        } catch (e: Exception) {
            callback?.cameraOnError(e.message)
            null
        }

    }

    companion object {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        private fun createImageFile(): File {
            val mediaDir: File? = app.externalMediaDirs.firstOrNull()?.let {
                val file = File(it, "Images")
                file.mkdirs()
                return@let file
            }
            val dir: File = if (mediaDir?.exists() == true) mediaDir else app.filesDir
            val timeFmt = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            val fileName = "${timeFmt.format(System.currentTimeMillis())}.jpg"
            return File(dir, fileName)
        }

        fun isGrantedPermission(): Boolean {
            arrayOf(Manifest.permission.CAMERA).forEach {
                if (ContextCompat.checkSelfPermission(
                        app,
                        it
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }
    }
}