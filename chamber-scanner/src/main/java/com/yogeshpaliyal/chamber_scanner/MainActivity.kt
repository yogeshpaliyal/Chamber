package com.yogeshpaliyal.chamber_scanner

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.yogeshpaliyal.chamber_scanner.databinding.ActivityMainBinding
import java.lang.Math.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val barcodeScanner by lazy {
        BarcodeScanning.getClient()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chamberView.bindToLifecycle(this)

        val analyzer = ImageAnalysis.Builder()
            .build()


        // Initialize our background executor
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analyzer.setAnalyzer(cameraExecutor){
            processImageProxy(it)
        }

        binding.chamberView.setImageAnalyzer(analyzer)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        imageProxy: ImageProxy
    ) {
        imageProxy.image?.let { image ->
                InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
        }?.let { inputImage ->
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    val barcode = barcodes.getOrNull(0)
                    barcode?.rawValue?.let { code ->
                        Log.d("ScanResult","=> $code")
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }.addOnCompleteListener {
                    imageProxy.close()
                }
        }

    }

}