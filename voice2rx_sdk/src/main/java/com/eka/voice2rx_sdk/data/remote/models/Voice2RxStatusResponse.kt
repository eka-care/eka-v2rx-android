package com.eka.voice2rx_sdk.data.remote.models


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Voice2RxStatusResponse(
    @SerializedName("data")
    var data: Data?,
    @SerializedName("error")
    var error: Error?,
    @SerializedName("status")
    var status: Voice2RxStatus = Voice2RxStatus.QUEUED,
)

@Keep
enum class Voice2RxStatus {
    @SerializedName("queued")
    QUEUED,

    @SerializedName("inprogress")
    IN_PROGRESS,

    @SerializedName("completed")
    COMPLETED,

    @SerializedName("deleted")
    DELETED,

    @SerializedName("error")
    ERROR,

    @SerializedName("partial_completed")
    PARTIAL_COMPLETED;

    companion object {
        fun fromValue(value: String): Voice2RxStatus {
            return Voice2RxStatus.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: QUEUED
        }
    }
}