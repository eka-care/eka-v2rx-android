package com.eka.voice2rx_sdk.data.local.models


import com.google.gson.annotations.SerializedName

data class Name(
    @SerializedName("f")
    var f: String?,
    @SerializedName("l")
    var l: String?
)