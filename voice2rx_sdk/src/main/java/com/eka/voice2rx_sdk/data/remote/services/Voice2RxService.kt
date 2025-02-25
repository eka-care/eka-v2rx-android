package com.eka.voice2rx_sdk.data.remote.services

import com.eka.voice2rx.data.remote.models.AwsS3ConfigResponse
import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.GET
import retrofit2.http.Url

interface Voice2RxService {
    @GET
    suspend fun getS3Config(
        @Url url: String,
    ): NetworkResponse<AwsS3ConfigResponse, AwsS3ConfigResponse>
}