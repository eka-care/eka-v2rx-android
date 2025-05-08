package com.eka.voice2rx_sdk.data.remote.models.requests


import androidx.annotation.Keep
import com.eka.voice2rx_sdk.data.local.models.FileInfo
import com.google.gson.annotations.SerializedName

@Keep
data class Voice2RxStopTransactionRequest(
    @SerializedName("audio_files")
    var audioFiles: List<String?>?,
    @SerializedName("chunks_info")
    var chunksInfo: Map<String, FileInfo>?,
)
