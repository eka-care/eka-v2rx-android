package com.eka.voice2rx_sdk.data.local.models


import com.google.gson.annotations.SerializedName

data class Personal(
    @SerializedName("name")
    var name: Name?
)