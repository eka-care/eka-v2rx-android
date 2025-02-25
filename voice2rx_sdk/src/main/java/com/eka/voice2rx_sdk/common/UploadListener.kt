package com.eka.voice2rx_sdk.common

interface UploadListener {
    fun onSuccess(sessionId : String, fileName : String)
    fun onError(sessionId : String, fileName : String, errorMsg : String)
}