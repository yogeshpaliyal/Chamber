 package com.yogeshpaliyal.chamber.preview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.yogeshpaliyal.chamber.MainViewModel
import com.yogeshpaliyal.chamber.R
import com.yogeshpaliyal.chamber.databinding.FragmentPreviewBinding
import dagger.hilt.android.AndroidEntryPoint

 @AndroidEntryPoint
 class PreviewFragment : Fragment() {

    private lateinit var binding: FragmentPreviewBinding

     private val mViewModel by lazy {
         activity?.viewModels<MainViewModel>()?.value
     }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPreviewBinding.inflate(inflater,container,false)
        binding.mViewModel = mViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.photoView.setOnClickListener {
            binding.toolbar.isVisible = !binding.toolbar.isVisible
        }

        return binding.root
    }

}