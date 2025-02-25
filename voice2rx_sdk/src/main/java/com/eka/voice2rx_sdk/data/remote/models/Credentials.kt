package com.eka.voice2rx.data.remote.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
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