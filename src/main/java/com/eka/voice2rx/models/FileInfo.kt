package com.eka.voice2rx.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FileInfo(
    @SerializedName("et")
    var et: String?,
    @SerializedName("st")
    var st: String?
)