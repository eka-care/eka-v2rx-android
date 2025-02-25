package com.eka.voice2rx_sdk.sdkinit

import androidx.annotation.Keep

@Keep
data class AwsS3Configuration(
    val bucketName: String,
    val sessionToken: String,
    val accessKey: String,
    val secretKey: String
)
