package com.eka.voice2rx_sdk.data.local.models


import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("personal")
    var personal: Personal?
)