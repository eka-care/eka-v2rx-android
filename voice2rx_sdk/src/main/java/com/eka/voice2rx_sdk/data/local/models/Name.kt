package com.eka.voice2rx_sdk.data.local.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Name(
    @SerializedName("f")
    var f: String?,
    @SerializedName("l")
    var l: String?
)