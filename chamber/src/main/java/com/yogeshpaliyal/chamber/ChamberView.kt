package com.yogeshpaliyal.chamber

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*



@SuppressLint("ClickableViewAccessibility")
class ChamberView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var previewView: PreviewView = PreviewView(context)
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private var cameraControl: CameraControl? = null
    private var imageCapture: ImageCapture? = null

    private var imageAnalyzer : ImageAnalysis ?= null

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var mChamberInterface: ChamberListener? = null

    private var mChamberResultListener: ((result: ChamberResult) -> Unit)? = null

    private var flashModeChangeListener : ((flashMode: Int)->Unit) ?=null

    fun setFlashModeChangeListener(flashModeChangeListener: (flashMode: Int)->Unit){
        this.flashModeChangeListener = flashModeChangeListener
    }

    fun getFlashMode() = imageCapture?.flashMode

    fun getCameraSide() = cameraSelector

    private var lifecycleOwner: LifecycleOwner? = null

    fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
        startCamera()
    }

    fun setImageAnalyzer(imageAnalyzer : ImageAnalysis){
        this.imageAnalyzer = imageAnalyzer
        startCamera()
    }

    fun setResultListener(mChamberResultListener: (result: ChamberResult) -> Unit) {
        this.mChamberResultListener = mChamberResultListener
    }


    init {
        previewView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    // Get the MeteringPointFactory from PreviewView
                    val factory = previewView.meteringPointFactory

                    // Create a MeteringPoint from the tap coordinates
                    val point = factory.createPoint(motionEvent.x, motionEvent.y)

                    // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                    val action = FocusMeteringAction.Builder(point).build()

                    // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                    // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                    cameraControl?.startFocusAndMetering(action)

                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }

        addView(previewView)

    }


    fun changeFlashMode(@ImageCapture.FlashMode flashMode: Int){
        imageCapture?.flashMode = flashMode
        flashModeChangeListener?.invoke(flashMode)
    }

    fun switchCamera(cameraSelector: CameraSelector){
        this.cameraSelector = cameraSelector
        startCamera()
    }


    private fun startCamera() {
        val tempContext = context ?: return
        val tempLifecycleOwner = lifecycleOwner ?: throw NullPointerException("Lifecycle is null, Bind to lifecycle")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(tempContext)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = imageCapture ?: ImageCapture.Builder().build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera

                val camera = cameraProvider.bindToLifecycle(
                    tempLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )

                changeFlashMode(FLASH_MODE_OFF)

                cameraControl = camera.cameraControl
                // setFlashButton()
            } catch (exc: Exception) {
                // Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Capture the image
     */
    fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = mChamberInterface?.getOutputFile() ?: getOutputFile()


        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    //Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    mChamberResultListener?.invoke(ChamberResult.Error(exc))
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    mChamberResultListener?.invoke(ChamberResult.Success(output.savedUri ?: Uri.fromFile(photoFile)))
                }
            })
    }

    private fun getOutputFile(): File {
        return File(
            getOutputDirectory(),
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )
    }

    private fun getOutputDirectory(): File? {
        val mediaDir = context?.externalMediaDirs?.firstOrNull()
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context?.filesDir
    }
}