package com.yogeshpaliyal.chamber

import java.io.File


interface ChamberListener {
    fun getOutputFile() : File?
}