package com.eka.voice2rx.sdkinit

import android.content.Context
import android.content.Intent
import com.eka.voice2rx.MainActivity

object Voice2RxInit {
    private var configuration: Voice2RxInitConfig? = null

    fun initialize(
        config: Voice2RxInitConfig,
        context: Context
    ) {
        configuration = config
        context.startActivity(Intent(context, MainActivity::class.java))
    }

    fun getVoice2RxInitConfiguration(): Voice2RxInitConfig {
        if (configuration == null) {
            throw IllegalStateException("Voice2Rx Init configuration not initialized")
        }
        return configuration!!
    }
}