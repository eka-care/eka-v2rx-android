package com.eka.voice2rx_sdk.audio_converters

class ConversionResult {
    val convertCode: ConversionCode
    var errorMessage: String? = null

    constructor() {
        convertCode = ConversionCode.SUCCESS
    }

    constructor(errorMessage: String?) {
        convertCode = ConversionCode.FAILED

        this.errorMessage = errorMessage
    }

    enum class ConversionCode {
        SUCCESS,
        FAILED
    }
}