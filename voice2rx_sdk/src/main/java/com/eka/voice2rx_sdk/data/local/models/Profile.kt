package com.eka.voice2rx_sdk.data.local.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Profile(
    @SerializedName("personal")
    var personal: Personal?
)