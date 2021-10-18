package com.yogeshpaliyal.chamber

import android.net.Uri
import androidx.camera.core.ImageCaptureException




sealed class ChamberResult{
    class Success(var uri: Uri?) : ChamberResult()
    class Error(var exception : ImageCaptureException) : ChamberResult()
}
