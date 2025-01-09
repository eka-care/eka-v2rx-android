package com.eka.voice2rx_sdk.data.local.models


import com.google.gson.annotations.SerializedName

data class StartOfMessage(
    @SerializedName("context_data")
    var contextData: ContextData?,
    @SerializedName("date")
    var date: String?,
    @SerializedName("doc_oid")
    var docOid: String?,
    @SerializedName("doc_uuid")
    var docUuid: String?,
    @SerializedName("files")
    var files: List<Any?>?,
    @SerializedName("mode")
    var mode: String?,
    @SerializedName("s3_url")
    var s3Url: String?,
    @SerializedName("uuid")
    var uuid: String?
)