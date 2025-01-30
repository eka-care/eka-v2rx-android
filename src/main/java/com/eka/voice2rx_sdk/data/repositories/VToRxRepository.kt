package com.eka.voice2rx_sdk.data.repositories

import com.eka.voice2rx_sdk.data.local.db.Voice2RxDatabase
import com.eka.voice2rx_sdk.data.local.db.entities.VToRxSession
import com.eka.voice2rx_sdk.data.local.models.Voice2RxSessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VToRxRepository(
    private val vToRxDatabase: Voice2RxDatabase
) {
    suspend fun insertSession(session: VToRxSession) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao().insertSession(session = session)
            }
            catch (_ : Exception) {
            }
        }
    }

    suspend fun getSessionBySessionId(sessionId : String) : VToRxSession? {
        return withContext(Dispatchers.IO) {
            try {
                val session = vToRxDatabase.getVoice2RxDao().getSessionBySessionId(sessionId = sessionId)
                session
            }
            catch (_ : Exception) {
                null
            }
        }
    }

    suspend fun updateSession(sessionId : String, updatedSessionId : String, status : Voice2RxSessionStatus) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao().updateSession(sessionId = sessionId, updatedSessionId = updatedSessionId, status = status)
            }
            catch (_ : Exception) {
            }
        }
    }

    suspend fun getAllSessions() : List<VToRxSession> {
        return withContext(Dispatchers.IO) {
            try {
                val sessions = vToRxDatabase.getVoice2RxDao().getAllVoice2RxSessions()
                sessions
            }
            catch (_ : Exception) {
                emptyList<VToRxSession>()
            }
        }
    }

    suspend fun getSessionsByOwnerId(ownerId : String) : List<VToRxSession> {
        return withContext(Dispatchers.IO) {
            try {
                val sessions = vToRxDatabase.getVoice2RxDao().getAllSessionByOwnerId(ownerId = ownerId)
                sessions
            }
            catch (_ : Exception) {
                emptyList<VToRxSession>()
            }
        }
    }
}