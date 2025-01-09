package com.eka.voice2rx_sdk

data class AudioRecordModel(
    val frameData: ShortArray,
    val isSilence: Boolean,
    var isClipped: Boolean,
    var timeStamp: Long
)