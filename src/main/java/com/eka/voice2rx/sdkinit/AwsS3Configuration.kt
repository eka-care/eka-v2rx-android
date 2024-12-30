package com.eka.voice2rx.sdkinit

data class AwsS3Configuration(
    val bucketName: String,
    val sessionToken: String,
    val accessKey: String,
    val secretKey: String
)
