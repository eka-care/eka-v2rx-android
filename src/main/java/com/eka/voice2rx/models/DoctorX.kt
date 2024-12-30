package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class DoctorX(
    @SerializedName("_id")
    var id: String?,
    @SerializedName("profile")
    var profile: ProfileX?
)