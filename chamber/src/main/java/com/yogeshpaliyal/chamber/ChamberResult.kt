package com.yogeshpaliyal.chamber

import android.net.Uri
import androidx.camera.core.ImageCaptureException


interface ChamberResultListener{
    fun result(result: ChamberResult)
}


sealed class ChamberResult{
    class Success(uri: Uri?) : ChamberResult()
    class Error(exception : ImageCaptureException) : ChamberResult()
}
