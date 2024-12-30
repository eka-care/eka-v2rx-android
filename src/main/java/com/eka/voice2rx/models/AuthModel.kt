package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class AuthModel(
    @SerializedName("credentials")
    var credentials: Credentials?,
    @SerializedName("expiry")
    var expiry: Int?,
    @SerializedName("identity_id")
    var identityId: String?,
    @SerializedName("token")
    var token: String?
)