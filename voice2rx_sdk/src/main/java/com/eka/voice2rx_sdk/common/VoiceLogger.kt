package com.eka.voice2rx_sdk.common

import android.util.Log
import com.eka.voice2rx_sdk.BuildConfig

object VoiceLogger {
    fun d(tag : String, msg : String) {
        if(BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun e(tag : String, msg : String) {
        if(BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg)
        }
    }
}