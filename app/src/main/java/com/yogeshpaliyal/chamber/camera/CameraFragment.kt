package com.yogeshpaliyal.chamber.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yogeshpaliyal.chamber.MainViewModel
import com.yogeshpaliyal.chamber.R
import com.yogeshpaliyal.chamber.databinding.FragmentCameraBinding
import com.yogeshpaliyal.chamber.preview.PreviewFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding

    private val mViewModel by lazy {
        activity?.viewModels<MainViewModel>()?.value
    }

    private var imageCapture: ImageCapture? = null

    private var outputDirectory: File? = null
    private lateinit var cameraExecutor: ExecutorService

    private var cameraControl: CameraControl? = null
    var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    var flashMode = ImageCapture.FLASH_MODE_OFF

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }
        binding.btnFlipCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        binding.btnFlash.setOnClickListener {
            when (flashMode) {
                ImageCapture.FLASH_MODE_OFF -> {
                    flashMode = ImageCapture.FLASH_MODE_ON
                    binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
                }
                ImageCapture.FLASH_MODE_ON -> {
                    flashMode = ImageCapture.FLASH_MODE_AUTO
                    binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
                }
                ImageCapture.FLASH_MODE_AUTO -> {
                    flashMode = ImageCapture.FLASH_MODE_OFF
                    binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
                }
            }
            startCamera()
        }

        binding.viewFinder.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                MotionEvent.ACTION_UP -> {
                    // Get the MeteringPointFactory from PreviewView
                    val factory = binding.viewFinder.getMeteringPointFactory()

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
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        mViewModel?.capturedImage?.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it?.getContentIfNotHandled()?.let {
                findNavController().navigate(PreviewFragmentDirections.actionGlobalPreviewFragment())
            }
        })

        return binding.root
    }

    private fun getOutputDirectory(): File? {
        val mediaDir = context?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else context?.filesDir
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    context,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all { permission ->
        context?.let {
            ContextCompat.checkSelfPermission(
                it, permission
            ) == PackageManager.PERMISSION_GRANTED
        } ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val tempContext = context ?: return
        val cameraProviderFuture = ProcessCameraProvider.getInstance(tempContext)
        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera

                val camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                if (camera.cameraInfo.hasFlashUnit()) {
                    imageCapture?.flashMode = flashMode
                }
                cameraControl = camera.cameraControl
                setFlashButton()
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun setFlashButton() {
        when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> {
                binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
            }
            ImageCapture.FLASH_MODE_ON -> {
                binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
            }
            ImageCapture.FLASH_MODE_AUTO -> {
                binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
            }
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    mViewModel?.setCapturedImage(savedUri)
                    galleryAddPic(savedUri)
                    Log.d(TAG, msg)
                }
            })
    }

    private fun galleryAddPic(uri: Uri) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = uri
            context?.sendBroadcast(mediaScanIntent)
        }
    }
}