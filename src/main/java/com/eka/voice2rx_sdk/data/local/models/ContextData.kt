package com.eka.voice2rx_sdk.data.local.models


import com.google.gson.annotations.SerializedName

data class ContextData(
    @SerializedName("doctor")
    var doctor: Doctor?,
    @SerializedName("patient")
    var patient: Patient?,
    @SerializedName("visitid")
    var visitid: String?
)