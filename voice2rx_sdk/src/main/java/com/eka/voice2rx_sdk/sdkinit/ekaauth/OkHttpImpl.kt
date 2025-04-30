package com.eka.voice2rx_sdk.sdkinit.ekaauth

import com.eka.network.IOkHttpSetup
import com.eka.voice2rx_sdk.BuildConfig
import kotlinx.coroutines.runBlocking

class OkHttpImpl(
    val ekaAuthConfig: EkaAuthConfig?,
    val authorizationToken: String,
) : IOkHttpSetup {
    override fun getDefaultHeaders(url: String): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["auth"] = authorizationToken
        headers["Authorization"] = "Bearer $authorizationToken"
        headers["flavour"] = "android"
        headers["sdk_version"] = BuildConfig.SDK_VERSION_NAME
        headers["sdk_build_number"] = BuildConfig.SDK_BUILD_NUMBER
        return headers
    }

    override fun onSessionExpire() {
        ekaAuthConfig?.sessionExpired()
    }

    override fun refreshAuthToken(url: String): Map<String, String>? {
        return runBlocking {
            val sessionToken = ekaAuthConfig?.refreshToken()
            if (sessionToken.isNullOrBlank()) {
                null
            } else {
                getDefaultHeaders(url)
            }
        }
    }
}