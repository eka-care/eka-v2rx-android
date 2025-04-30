package com.eka.voice2rx_sdk.data.remote.services

import com.eka.voice2rx.data.remote.models.AwsS3ConfigResponse
import com.eka.voice2rx_sdk.data.remote.models.Voice2RxStatusResponse
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Url

interface Voice2RxService {
    @GET
    suspend fun getS3Config(
        @Url url: String,
    ): NetworkResponse<AwsS3ConfigResponse, AwsS3ConfigResponse>

    @GET("voice-record/api/status/{session_id}")
    suspend fun getVoice2RxStatus(
        @Path("session_id") sessionId: String,
    ): NetworkResponse<Voice2RxStatusResponse, Voice2RxStatusResponse>
}