package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class ContextData(
    @SerializedName("doctor")
    var doctor: Doctor?,
    @SerializedName("patient")
    var patient: Patient?,
    @SerializedName("visitid")
    var visitid: String?
)