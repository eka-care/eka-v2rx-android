package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class ContextDataX(
    @SerializedName("doctor")
    var doctor: DoctorX?,
    @SerializedName("patient")
    var patient: PatientX?,
    @SerializedName("visitid")
    var visitid: String?
)