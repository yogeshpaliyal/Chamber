package com.yogeshpaliyal.chamber

import android.net.Uri
import androidx.camera.core.ImageCaptureException


fun interface ChamberResultListener{
    fun result(result: ChamberResult)
}


sealed class ChamberResult{
    class Success(uri: Uri?) : ChamberResult()
    class Error(exception : ImageCaptureException) : ChamberResult()
}
