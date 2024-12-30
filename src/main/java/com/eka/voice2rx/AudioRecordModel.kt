package com.eka.voice2rx

data class AudioRecordModel(
    val frameData: ShortArray,
    val isSilence: Boolean,
    var isClipped: Boolean,
    var timeStamp: Long
)