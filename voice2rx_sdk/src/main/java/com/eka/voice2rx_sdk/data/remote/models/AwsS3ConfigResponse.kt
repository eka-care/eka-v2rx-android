package com.eka.voice2rx.data.remote.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AwsS3ConfigResponse(
    @SerializedName("credentials")
    var credentials: Credentials?,
    @SerializedName("expiry")
    var expiry: Int?,
    @SerializedName("identity_id")
    var identityId: String?,
    @SerializedName("token")
    var token: String?
)