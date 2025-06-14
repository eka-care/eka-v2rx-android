package com.eka.voice2rx_sdk.common.voicelogger

import android.util.Log

object VoiceLogger {
    var enableDebugLogs = false
    fun d(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.e(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.w(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (enableDebugLogs) {
            Log.i(tag, msg)
        }
    }
}