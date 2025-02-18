package com.eka.voice2rx_sdk.sdkinit

import android.content.Context
import com.eka.voice2rx_sdk.common.ResponseState
import com.eka.voice2rx_sdk.data.local.db.Voice2RxDatabase
import com.eka.voice2rx_sdk.data.repositories.VToRxRepository

object Voice2RxInit {
    private var configuration: Voice2RxInitConfig? = null
    private var v2RxRepository : VToRxRepository? = null

    fun initialize(
        config: Voice2RxInitConfig,
        context: Context
    ) {
        configuration = config
//        context.startActivity(Intent(context, MainActivity::class.java))
    }

    fun getVoice2RxInitConfiguration(): Voice2RxInitConfig {
        if (configuration == null) {
            throw IllegalStateException("Voice2Rx Init configuration not initialized")
        }
        return configuration!!
    }

    fun retrySession(
        context : Context,
        sessionId : String,
        s3Config : AwsS3Configuration,
        onResponse : (ResponseState) -> Unit,
    ) {
        v2RxRepository = VToRxRepository(
            Voice2RxDatabase.getDatabase(context.applicationContext)
        )
        v2RxRepository?.retrySessionUploading(
            context = context,
            sessionId = sessionId,
            s3Config = s3Config,
            onResponse = onResponse
        )
    }
}