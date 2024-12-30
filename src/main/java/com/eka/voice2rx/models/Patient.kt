package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class Patient(
    @SerializedName("id")
    var id: String?
)