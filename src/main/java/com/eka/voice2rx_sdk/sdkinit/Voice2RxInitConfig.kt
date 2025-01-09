package com.eka.voice2rx_sdk.sdkinit

import androidx.compose.runtime.Composable
import com.eka.voice2rx_sdk.data.local.models.ContextData

data class Voice2RxInitConfig(
    val onStart: (sessionId: String) -> Unit,
    val onStop: (sessionId: String) -> Unit,
    val s3Config: AwsS3Configuration,
    val docOid: String,
    val docUuid: String,
    val ownerId : String,
    val callerId : String,
    val contextData: ContextData,
    val sampleRate: Int,
    val frameSize: Int,
    val prefCutDuration: Int,
    val despCutDuration: Int,
    val maxCutDuration: Int,
    val sessionId : String,
    val voice2RxScreen: @Composable (onStart: () -> Unit, onStop: () -> Unit) -> Unit
)
