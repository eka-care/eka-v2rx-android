package com.eka.voice2rx_sdk.common

import androidx.annotation.Keep
import com.eka.voice2rx_sdk.data.local.db.entities.VToRxSession
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.nio.charset.StandardCharsets
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal object Voice2RxInternalUtils {
    const val BUCKET_NAME = "m-prod-voice-record"
    fun getFolderPathForSession(session : VToRxSession) : String {
        val folder = Voice2RxUtils.getTimeStampInYYMMDD(session.createdAt)
        val path = "${folder}/${session.sessionId}"
        return path
    }

    fun getFileIdForSession(sessionId: String, fileName: String): String {
        val fileId = "${sessionId}_${fileName}"
        return fileId
    }

    fun getS3FilePath(session: VToRxSession, fileName: String): String {
        return "s3://${BUCKET_NAME}/${getFolderPathForSession(session = session)}/" + fileName
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun getUserTokenData(sessionToken: String): UserTokenData? {
        try {
            val userData =
                String(
                    Base64.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
                        .decode(sessionToken.split(".")[1]), StandardCharsets.UTF_8
                )
            val userTokenData = Gson().fromJson(userData, UserTokenData::class.java)
            VoiceLogger.d("getUserTokenData", userTokenData.toString())
            return userTokenData
        } catch (e: Exception) {
            VoiceLogger.e("getUserTokenData", e.message.toString())
        }
        return null
    }
}


@Keep
internal data class UserTokenData(
    @SerializedName("uuid")
    val uuid: String?,
    @SerializedName("oid")
    val oid: String,
    @SerializedName("fn")
    val name: String?,
    @SerializedName("gen")
    val gender: String?,
    @SerializedName("s")
    val salutation: String?,
    @SerializedName("is-p")
    val isP: Boolean?,
    @SerializedName("is-d")
    val isD: Boolean?,
    @SerializedName("dob")
    val dob: String?,
    @SerializedName("mob")
    val mob: String?,
    @SerializedName("type")
    val type: Int?,
    @SerializedName("doc-id")
    val docId: String?,
    @SerializedName("b-id")
    val businessId: String?,
    @SerializedName("p")
    val passType: String?,
    @SerializedName("pp")
    val passDetails: PassDetails?,
    @SerializedName("exp")
    val exp: Int?,
    @SerializedName("iat")
    val iat: Int?,
)

@Keep
internal data class PassDetails(
    @SerializedName("c")
    val c: String,
    @SerializedName("e")
    val e: String,
    @SerializedName("t")
    val t: String,
)