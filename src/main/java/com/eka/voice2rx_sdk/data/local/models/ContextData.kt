package com.eka.voice2rx_sdk.data.local.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ContextData(
    @SerializedName("doctor")
    var doctor: Doctor?,
    @SerializedName("patient")
    var patient: Patient?,
    @SerializedName("visitid")
    var visitid: String?
)