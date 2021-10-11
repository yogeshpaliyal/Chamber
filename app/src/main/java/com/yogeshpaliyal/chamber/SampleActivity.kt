package com.yogeshpaliyal.chamber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yogeshpaliyal.chamber.databinding.ActivitySampleBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@AndroidEntryPoint
class SampleActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chamberView.bindToLifecycle(this)
    }
}