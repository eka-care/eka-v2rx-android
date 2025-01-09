package com.eka.voice2rx_sdk.data.local.models


import com.google.gson.annotations.SerializedName

data class EndOfFileMessage(
    @SerializedName("chunks_info")
    var chunksInfo: Map<String, FileInfo>?,
    @SerializedName("context_data")
    var contextData: ContextData?,
    @SerializedName("date")
    var date: String?,
    @SerializedName("doc_oid")
    var docOid: String?,
    @SerializedName("doc_uuid")
    var docUuid: String?,
    @SerializedName("files")
    var files: List<String>?,
    @SerializedName("s3_url")
    var s3Url: String?,
    @SerializedName("uuid")
    var uuid: String?
)