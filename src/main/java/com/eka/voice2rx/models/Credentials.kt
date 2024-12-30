package com.eka.voice2rx.models


import com.google.gson.annotations.SerializedName

data class Credentials(
    @SerializedName("AccessKeyId")
    var accessKeyId: String?,
    @SerializedName("Expiration")
    var expiration: String?,
    @SerializedName("SecretKey")
    var secretKey: String?,
    @SerializedName("SessionToken")
    var sessionToken: String?
)