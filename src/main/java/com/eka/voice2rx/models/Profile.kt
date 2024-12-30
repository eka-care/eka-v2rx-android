package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("personal")
    var personal: Personal?
)