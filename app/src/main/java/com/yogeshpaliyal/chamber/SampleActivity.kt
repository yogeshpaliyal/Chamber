package com.yogeshpaliyal.chamber

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import com.yogeshpaliyal.chamber.databinding.ActivitySampleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chamberView.bindToLifecycle(this)

        binding.cameraCaptureButton.setOnClickListener {
            binding.chamberView.takePhoto()
        }

        binding.btnFlipCamera.setOnClickListener {
            binding.chamberView.switchCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        }

        binding.btnFlash.setOnClickListener {
            when(binding.chamberView.getFlashMode()){
                ImageCapture.FLASH_MODE_ON -> {
                    binding.chamberView.changeFlashMode(ImageCapture.FLASH_MODE_OFF)
                }
                ImageCapture.FLASH_MODE_OFF -> {
                    binding.chamberView.changeFlashMode(ImageCapture.FLASH_MODE_AUTO)
                }
                ImageCapture.FLASH_MODE_AUTO -> {
                    binding.chamberView.changeFlashMode(ImageCapture.FLASH_MODE_ON)
                }
            }
        }

        binding.chamberView.setFlashModeChangeListener {
            when (it) {
                ImageCapture.FLASH_MODE_ON -> {
                    binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_on_24)
                }
                ImageCapture.FLASH_MODE_OFF -> {
                    binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_off_24)
                }
                ImageCapture.FLASH_MODE_AUTO -> {
                    binding.btnFlash.setImageResource(R.drawable.ic_baseline_flash_auto_24)
                }
            }
        }

        binding.chamberView.setResultListener {
            when (it) {
                is ChamberResult.Error -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                is ChamberResult.Success -> {
                    Toast.makeText(this, "${it.uri}", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }
}