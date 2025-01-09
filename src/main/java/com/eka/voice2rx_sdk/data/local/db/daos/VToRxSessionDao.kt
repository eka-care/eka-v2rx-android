package com.eka.voice2rx_sdk.data.local.db.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.eka.voice2rx_sdk.common.DatabaseConstants
import com.eka.voice2rx_sdk.data.local.db.entities.VToRxSession

@Dao
interface VToRxSessionDao {
    @Insert
    suspend fun insertSession(session: VToRxSession)

    @Query("SELECT * FROM ${DatabaseConstants.V2RX_SESSION_TABLE_NAME}")
    suspend fun getAllVoice2RxSessions() : List<VToRxSession>

    @Query("SELECT * FROM ${DatabaseConstants.V2RX_SESSION_TABLE_NAME} WHERE owner_id = :ownerId")
    suspend fun getAllSessionByOwnerId(ownerId : String) : List<VToRxSession>

    @Delete
    suspend fun deleteSession(session: VToRxSession)
}