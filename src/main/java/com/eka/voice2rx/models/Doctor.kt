package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class Doctor(
    @SerializedName("_id")
    var id: String?,
    @SerializedName("profile")
    var profile: Profile?
)