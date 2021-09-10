package com.yogeshpaliyal.chamber.extensions

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("loadImage")
fun ImageView.loadImage(image: Any){
    Glide.with(this).load(image).into(this)
}