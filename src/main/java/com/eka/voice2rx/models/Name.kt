package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class Name(
    @SerializedName("f")
    var f: String?,
    @SerializedName("l")
    var l: String?
)