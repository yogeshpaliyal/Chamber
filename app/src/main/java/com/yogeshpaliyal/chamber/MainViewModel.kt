package com.yogeshpaliyal.chamber

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yogeshpaliyal.chamber.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val _capturedImage = MutableLiveData<Event<Uri>>()
    val capturedImage : LiveData<Event<Uri>> = _capturedImage

    fun setCapturedImage(path: Uri){
        _capturedImage.value = Event(path)
    }

}