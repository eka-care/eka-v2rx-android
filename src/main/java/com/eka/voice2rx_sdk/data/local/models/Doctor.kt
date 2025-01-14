package com.eka.voice2rx_sdk.data.local.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Doctor(
    @SerializedName("_id")
    var id: String?,
    @SerializedName("profile")
    var profile: Profile?
)