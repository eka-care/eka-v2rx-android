package com.eka.voice2rx_sdk.data.repositories

import android.util.Log
import com.eka.network.ConverterFactoryType
import com.eka.network.Networking
import com.eka.voice2rx.data.remote.models.AwsS3ConfigResponse
import com.eka.voice2rx_sdk.BuildConfig
import com.eka.voice2rx_sdk.common.VoiceLogger
import com.eka.voice2rx_sdk.data.local.db.Voice2RxDatabase
import com.eka.voice2rx_sdk.data.local.db.entities.VToRxSession
import com.eka.voice2rx_sdk.data.local.db.entities.VoiceFile
import com.eka.voice2rx_sdk.data.local.db.entities.VoiceFileType
import com.eka.voice2rx_sdk.data.local.db.entities.VoiceTransactionStage
import com.eka.voice2rx_sdk.data.local.db.entities.VoiceTransactionState
import com.eka.voice2rx_sdk.data.local.models.Voice2RxSessionStatus
import com.eka.voice2rx_sdk.data.remote.models.requests.Voice2RxInitTransactionRequest
import com.eka.voice2rx_sdk.data.remote.models.requests.Voice2RxStopTransactionRequest
import com.eka.voice2rx_sdk.data.remote.models.responses.Voice2RxInitTransactionResponse
import com.eka.voice2rx_sdk.data.remote.models.responses.Voice2RxStopTransactionResponse
import com.eka.voice2rx_sdk.data.remote.models.responses.Voice2RxTransactionResult
import com.eka.voice2rx_sdk.data.remote.services.Voice2RxService
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class VToRxRepository(
    private val vToRxDatabase: Voice2RxDatabase
) {

    private val remoteDataSource: Voice2RxService =
        Networking.create(
            clazz = Voice2RxService::class.java,
            curlLoggingEnabled = true,
            baseUrl = BuildConfig.DEVELOPER_URL,
            converterFactoryType = ConverterFactoryType.GSON
        )

    suspend fun initVoice2RxTransaction(
        sessionId: String,
        request: Voice2RxInitTransactionRequest
    ): NetworkResponse<Voice2RxInitTransactionResponse, Voice2RxInitTransactionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    remoteDataSource.initTransaction(sessionId = sessionId, request = request)
                Log.d(
                    "Voice2Rx",
                    "Init Transaction: $sessionId request: $request response : $response"
                )
                if (response is NetworkResponse.Success) {
                    updateSessionUploadStage(
                        sessionId = sessionId,
                        uploadStage = VoiceTransactionStage.STOP
                    )
                    checkUploadingStageAndProgress(sessionId = sessionId)
                }
                response
            } catch (e: Exception) {
                NetworkResponse.UnknownError(error = e, response = null)
            }
        }
    }

    suspend fun stopVoice2RxTransaction(
        sessionId: String,
        request: Voice2RxStopTransactionRequest
    ): NetworkResponse<Voice2RxStopTransactionResponse, Voice2RxStopTransactionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    remoteDataSource.stopTransaction(sessionId = sessionId, request = request)
                Log.d(
                    "Voice2Rx",
                    "Stop Transaction: $sessionId request: $request response : $response"
                )
                if (response is NetworkResponse.Success) {
                    updateSessionUploadStage(
                        sessionId = sessionId,
                        uploadStage = VoiceTransactionStage.COMMIT
                    )
                    goToCommitStep(sessionId = sessionId)
                }
                response
            } catch (e: Exception) {
                NetworkResponse.UnknownError(error = e, response = null)
            }
        }
    }

    private fun checkUploadingStageAndProgress(
        sessionId: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val session = getSessionBySessionId(sessionId = sessionId)
            if (session == null) {
                VoiceLogger.e("Voice2Rx", "Session not found for sessionId: $sessionId")
                return@launch
            }
            if (session.voiceTransactionState != VoiceTransactionState.STOPPED) {
                VoiceLogger.e("Voice2Rx", "Session is not stopped yet!")
                return@launch
            }
            when (session.uploadStage) {
                VoiceTransactionStage.INIT -> {
                    val request = Gson().fromJson<Voice2RxInitTransactionRequest>(
                        session.sessionMetadata,
                        Voice2RxInitTransactionRequest::class.java
                    )
                    initVoice2RxTransaction(
                        sessionId = sessionId,
                        request = request
                    )
                }

                VoiceTransactionStage.STOP -> {
                    goToStopStep(sessionId = sessionId)
                }

                VoiceTransactionStage.COMMIT -> {
                    goToCommitStep(sessionId = sessionId)
                }

                else -> {}
            }
        }
    }

    private fun goToStopStep(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val voiceFiles = getAllFiles(sessionId = sessionId)
            stopVoice2RxTransaction(
                sessionId = sessionId,
                request = Voice2RxStopTransactionRequest(
                    audioFiles = voiceFiles.filter { it.fileType == VoiceFileType.CHUNK_AUDIO }
                        .map { it.fileName }
                        .toList(),
                    chunksInfo = null
                )
            )
        }
    }

    private fun goToCommitStep(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val session = getSessionBySessionId(sessionId = sessionId)
            if (session == null) {
                VoiceLogger.e("Voice2Rx", "Session not found for sessionId: $sessionId")
                return@launch
            }
            if (session.uploadStage != VoiceTransactionStage.COMMIT) {
                return@launch
            }
            val voiceFiles = getAllFiles(sessionId = sessionId)
            val isAllUploaded =
                voiceFiles.filter { it.fileType == VoiceFileType.CHUNK_AUDIO }.all { it.isUploaded }
            if (isAllUploaded) {
                commitVoice2RxTransaction(
                    sessionId = sessionId,
                    request = Voice2RxStopTransactionRequest(
                        audioFiles = voiceFiles.filter { it.fileType == VoiceFileType.CHUNK_AUDIO }
                            .map { it.fileName }
                            .toList(),
                        chunksInfo = null
                    )
                )
            } else {
                VoiceLogger.e("Voice2Rx", "Not all audio files are uploaded")
            }
        }
    }

    suspend fun commitVoice2RxTransaction(
        sessionId: String,
        request: Voice2RxStopTransactionRequest
    ): NetworkResponse<Voice2RxStopTransactionResponse, Voice2RxStopTransactionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    remoteDataSource.commitTransaction(sessionId = sessionId, request = request)
                Log.d(
                    "Voice2Rx",
                    "Commit Transaction: $sessionId request: $request response : $response"
                )
                if (response is NetworkResponse.Success) {
                    updateSessionUploadStage(
                        sessionId = sessionId,
                        uploadStage = VoiceTransactionStage.COMPLETED
                    )
                }
                response
            } catch (e: Exception) {
                NetworkResponse.UnknownError(error = e, response = null)
            }
        }
    }

    suspend fun getVoice2RxStatus(sessionId: String): NetworkResponse<Voice2RxTransactionResult, Voice2RxTransactionResult> {
        return withContext(Dispatchers.IO) {
            try {
                goToCommitStep(sessionId = sessionId)
                val response = remoteDataSource.getVoice2RxTransactionResult(sessionId = sessionId)
                response
            } catch (e: Exception) {
                NetworkResponse.UnknownError(error = e, response = null)
            }
        }
    }

    suspend fun insertSession(session: VToRxSession) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao().insertSession(session = session)
            }
            catch (_ : Exception) {
            }
        }
    }

    suspend fun insertVoiceFile(voiceFile: VoiceFile) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao().insertVoiceFile(voiceFile = voiceFile)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun updateVoiceFile(fileId: String, isUploaded: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao()
                    .updateVoiceFile(fileId = fileId, isUploaded = isUploaded)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun getAllFiles(sessionId: String): List<VoiceFile> {
        return withContext(Dispatchers.IO) {
            try {
                val files = vToRxDatabase.getVoice2RxDao().getAllFiles(sessionId = sessionId)
                files
            } catch (_: Exception) {
                emptyList<VoiceFile>()
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

    suspend fun updateSessionState(sessionId: String, updatedState: VoiceTransactionState) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao()
                    .updateSessionState(sessionId = sessionId, updatedState = updatedState)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun updateSessionUploadStage(sessionId: String, uploadStage: VoiceTransactionStage) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao()
                    .updateSessionUploadStage(sessionId = sessionId, uploadStage = uploadStage)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun updateSession(session: VToRxSession) {
        withContext(Dispatchers.IO) {
            try {
                vToRxDatabase.getVoice2RxDao().updateSession(session = session)
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

    suspend fun getAwsS3Config(): NetworkResponse<AwsS3ConfigResponse, AwsS3ConfigResponse> {
        val url = BuildConfig.COG_URL + "credentials"
        return withContext(Dispatchers.IO) {
            try {
                val response = remoteDataSource.getS3Config(url)
                response
            } catch (e: Exception) {
                NetworkResponse.UnknownError(error = e, response = null)
            }
        }
    }

    //TODO

//    fun retrySessionUploading(
//        context : Context,
//        sessionId : String,
//        s3Config : AwsS3Configuration?,
//        onResponse : (ResponseState) -> Unit,
//    ) {
//        if(!Voice2RxUtils.isNetworkAvailable(context)) {
//            onResponse(ResponseState.Error("Network not available!"))
//            return
//        }
//        if(s3Config == null) {
//            onResponse(ResponseState.Error("Credentials not available!"))
//            return
//        }
//        CoroutineScope(Dispatchers.IO).launch {
//            val session = getSessionBySessionId(sessionId = sessionId)
//            try {
//                val results = session?.filePaths
//                    ?.map { filePath ->
//                        uploadFileFlow(filePath, session.createdAt, sessionId, s3Config, context)
//                            .toList()
//                    }?.flatten()
//
//                if(results != null && results.all { it }) {
//                    onResponse(ResponseState.Success(true))
//                } else {
//                    onResponse(ResponseState.Error("Audio file upload failed!"))
//                }
//            } catch (error : Exception) {
//                onResponse(ResponseState.Error(error?.message ?: "Something went wrong!"))
//            }
//        }
//    }
//
//    fun uploadFileFlow(
//        filePath: String,
//        createdAt: Long,
//        sessionId: String,
//        s3Config: AwsS3Configuration,
//        context: Context
//    ) = callbackFlow {
//        if (!Voice2RxUtils.isNetworkAvailable(context)) {
//            trySend(false)
//            close()
//            return@callbackFlow
//        }
//
//        val file = File(context.filesDir, filePath)
//        if (!file.exists()) {
//            VoiceLogger.e("Retry Session", "File Not Found!")
//            trySend(true)
//            close()
//            return@callbackFlow
//        }
//
//        try {
//            AwsS3UploadService.uploadFileToS3(
//                context = context,
//                fileName = filePath.split("_").last(),
//                folderName = Voice2RxUtils.getTimeStampInYYMMDD(createdAt),
//                file = file,
//                sessionId = sessionId,
//                isAudio = isAudioFile(filePath),
//                s3Config = s3Config,
//                onResponse = { response ->
//                    if(response is ResponseState.Success && response.isCompleted) {
//                        trySend(true)
//                    } else {
//                        trySend(false)
//                    }
//                    close()
//                }
//            )
//        } catch (e: Exception) {
//            VoiceLogger.e("Retry Session", "Upload failed: ${e.message}")
//            trySend(false)
//            close()
//        }
//
//        awaitClose {}
//    }

    private fun isAudioFile(fileName : String) : Boolean {
        return !fileName.lowercase().endsWith(".json")
    }
}