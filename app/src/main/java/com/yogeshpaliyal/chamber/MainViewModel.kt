package com.yogeshpaliyal.chamber

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel(application: Application) : AndroidViewModel(application) {
}