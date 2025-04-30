package com.eka.voice2rx_sdk.sdkinit.ekaauth

interface EkaAuthConfig {
    fun refreshToken(): String
    fun sessionExpired()
}