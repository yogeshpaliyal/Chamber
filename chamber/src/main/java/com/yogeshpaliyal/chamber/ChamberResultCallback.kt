package com.yogeshpaliyal.chamber

import android.net.Uri

interface ChamberResultCallback {
    fun onCaptured(uri: Uri)
    fun onErrorInCapture()
}