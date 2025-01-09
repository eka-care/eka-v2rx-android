package com.eka.voice2rx_sdk.data.local.db.entities

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eka.voice2rx_sdk.common.DatabaseConstants
import com.eka.voice2rx_sdk.data.local.models.Voice2RxType

@Keep
@Entity(tableName = DatabaseConstants.V2RX_SESSION_TABLE_NAME, primaryKeys = ["session_id"])
data class VToRxSession(
    @PrimaryKey
    @ColumnInfo(name = "session_id") val sessionId : String,
    @ColumnInfo(name = "file_paths") val filePaths : List<String>,
    @ColumnInfo(name = "created_at") val createdAt : Long,
    @ColumnInfo(name = "full_audio_path") val fullAudioPath : String,
    @ColumnInfo(name = "owner_id") val ownerId : String,
    @ColumnInfo(name = "caller_id") val callerId : String,
    @ColumnInfo(name = "patient_id") val patientId : String,
    @ColumnInfo(name = "mode") val mode : Voice2RxType
)
