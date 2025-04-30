package com.eka.voice2rx_sdk.data.remote.models

import androidx.annotation.Keep

@Keep
data class SessionStatus(
    val sessionId: String,
    val status: Voice2RxStatus? = null,
    val error: Error? = null
)