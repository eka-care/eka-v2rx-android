package com.eka.voice2rx_sdk

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eka.voice2rx_sdk.common.ResponseState
import com.eka.voice2rx_sdk.common.UploadListener
import com.eka.voice2rx_sdk.common.Voice2RxUtils
import com.eka.voice2rx_sdk.common.VoiceLogger
import com.eka.voice2rx_sdk.data.local.db.Voice2RxDatabase
import com.eka.voice2rx_sdk.data.local.db.entities.VToRxSession
import com.eka.voice2rx_sdk.data.local.models.EndOfFileMessage
import com.eka.voice2rx_sdk.data.local.models.FileInfo
import com.eka.voice2rx_sdk.data.local.models.RecordingState
import com.eka.voice2rx_sdk.data.local.models.StartOfMessage
import com.eka.voice2rx_sdk.data.local.models.Voice2RxSessionStatus
import com.eka.voice2rx_sdk.data.local.models.Voice2RxType
import com.eka.voice2rx_sdk.data.remote.services.AwsS3UploadService
import com.eka.voice2rx_sdk.data.repositories.VToRxRepository
import com.eka.voice2rx_sdk.recorder.AudioCallback
import com.eka.voice2rx_sdk.recorder.VoiceRecorder
import com.eka.voice2rx_sdk.sdkinit.Voice2RxInit
import com.eka.voice2rx_sdk.sdkinit.Voice2RxInitConfig
import com.google.gson.Gson
import com.konovalov.vad.silero.Vad
import com.konovalov.vad.silero.VadSilero
import com.konovalov.vad.silero.config.FrameSize
import com.konovalov.vad.silero.config.Mode
import com.konovalov.vad.silero.config.SampleRate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class V2RxViewModel(
    val app: Application,
) : AndroidViewModel(app), AudioCallback, UploadListener {

    companion object {
        const val TAG = "VADViewModel"
    }
    var database: Voice2RxDatabase
    var repository : VToRxRepository
    init {
        database = Voice2RxDatabase.getDatabase(app)
        repository = VToRxRepository(database)
    }

    var bucketName = ""
    var folderName: String = ""
    lateinit var config : Voice2RxInitConfig

    private val DEFAULT_MODE = Mode.NORMAL
    private val DEFAULT_SILENCE_DURATION_MS = 300

    private val audioChunks = mutableListOf<ShortArray>()

    private var chunksInfo = mutableMapOf<String, FileInfo>()
    private var recordedFiles = ArrayList<String>()

    val sessionId = mutableStateOf(Voice2RxUtils.generateNewSessionId())

    private lateinit var recorder: VoiceRecorder
    private lateinit var audioHelper: AudioHelper
    private lateinit var uploadService: UploadService
    private lateinit var vad: VadSilero
    private var isRecording = false

    private val _recordingResponse = MutableStateFlow<String>("Analyzing...")
    val recordingResponse = _recordingResponse.asStateFlow()

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.INITIAL)
    val recordingState = _recordingState.asStateFlow()

    private val _sessionsByOwnerId = MutableStateFlow<List<VToRxSession>>(emptyList())
    val sessionsByOwnerId = _sessionsByOwnerId.asStateFlow()

    private val FULL_RECORDING_FILE_NAME =
        UUID.randomUUID().toString() + "_" + "Full_Recording.m4a_"

    private lateinit var fullRecordingFile: File
    private var sessionUploadStatus = true

    fun initValues() {
        bucketName = Voice2RxInit.getVoice2RxInitConfiguration().s3Config.bucketName
        folderName = Voice2RxUtils.getCurrentDateInYYMMDD()
        config = Voice2RxInit.getVoice2RxInitConfiguration()
        AwsS3UploadService.setUploadListener(this)
    }

    fun addValueToChunksInfo(fileName: String, fileInfo: FileInfo) {
        chunksInfo[fileName.split("_").last()] = fileInfo
        recordedFiles.add(fileName)
    }

    fun getUploadService(): UploadService {
        return uploadService
    }

    fun startRecording(mode : Voice2RxType) {
        viewModelScope.launch {
            sessionUploadStatus = true
            sessionId.value = Voice2RxInit.getVoice2RxInitConfiguration().sessionId
            recordedFiles.clear()

            vad = Vad.builder()
                .setContext(app)
                .setSampleRate(SampleRate.SAMPLE_RATE_16K)
                .setFrameSize(FrameSize.FRAME_SIZE_512)
                .setMode(DEFAULT_MODE)
                .setSilenceDurationMs(DEFAULT_SILENCE_DURATION_MS)
                .build()

            recorder = VoiceRecorder(this@V2RxViewModel)
            audioHelper = AudioHelper(app, this@V2RxViewModel, sessionId.value)
            uploadService = UploadService(app, audioHelper, sessionId.value)
            uploadService.FILE_INDEX = 0

            isRecording = true
            fullRecordingFile = File(app.filesDir, Voice2RxUtils.getFullRecordingFileName(sessionId = sessionId.value))
            chunksInfo = mutableMapOf<String, FileInfo>()
            recorder.start(app, fullRecordingFile, vad.sampleRate.value, vad.frameSize.value)
            sendStartOfMessage(mode = mode)
            config.onStart.invoke(sessionId.value)
        }
        _recordingState.value = RecordingState.STARTED
    }

    private fun sendStartOfMessage(mode : Voice2RxType) {
        val s3Url = "s3://$bucketName/$folderName/${sessionId.value}/"
        val som = StartOfMessage(
            contextData = Voice2RxInit.getVoice2RxInitConfiguration().contextData,
            date = Voice2RxUtils.convertTimestampToISO8601(System.currentTimeMillis()),
            docOid = Voice2RxInit.getVoice2RxInitConfiguration().docOid,
            docUuid = Voice2RxInit.getVoice2RxInitConfiguration().docUuid,
            files = listOf(),
            mode = mode.value,
            s3Url = s3Url,
            uuid = sessionId.value
        )
        VoiceLogger.d(TAG, "SOM : " + Gson().toJson(som))
        val somFile = saveJsonToFile("${sessionId.value}_som.json", Gson().toJson(som))
        recordedFiles.add(somFile.name)
        AwsS3UploadService.uploadFileToS3(app, "som.json", somFile, folderName, sessionId.value, isAudio = false)
    }

    fun saveJsonToFile(fileName: String, jsonContent: String): File {
        val file = File(app.filesDir, fileName)
        file.writeText(jsonContent)
        return file
    }

    fun stopRecording(mode : Voice2RxType) {
        viewModelScope.launch {
            isRecording = false
            if (::recorder.isInitialized) {
                recorder.stop()
            }
            audioHelper.uploadLastData()
            uploadWholeFileData()
            sendEndOfMessage()
            storeSessionInDatabase(mode)
            if(sessionUploadStatus) {
                config.onStop.invoke(sessionId.value, chunksInfo.size + 2)
            } else {
                repository.retrySessionUploading(
                    context = app,
                    sessionId = sessionId.value,
                    s3Config = Voice2RxInit.getVoice2RxInitConfiguration().s3Config,
                    onResponse = {
                        if(it is ResponseState.Success && it.isCompleted) {
                            config.onStop.invoke(sessionId.value, chunksInfo.size + 2)
                        } else {
                            config.onError.invoke(sessionId.value)
                        }
                    }
                )
            }
        }
        _recordingState.value = RecordingState.INITIAL
    }

    fun isRecording() : Boolean {
        return isRecording
    }

    fun dispose() {
        viewModelScope.launch {
            isRecording = false
            if (::recorder.isInitialized) {
                recorder.stop()
            }
        }
    }

    private fun storeSessionInDatabase(mode : Voice2RxType) {
        viewModelScope.launch(Dispatchers.IO) {
            VoiceLogger.d("VadViewModel", recordedFiles.toList().toString())
            repository.insertSession(
                session = VToRxSession(
                    sessionId = sessionId.value,
                    filePaths = recordedFiles.toList(),
                    createdAt = Voice2RxUtils.getCurrentUTCEpochMillis(),
                    fullAudioPath = Voice2RxUtils.getFullRecordingFileName(sessionId = sessionId.value),
                    ownerId = Voice2RxInit.getVoice2RxInitConfiguration().ownerId,
                    callerId = Voice2RxInit.getVoice2RxInitConfiguration().callerId,
                    patientId = Voice2RxInit.getVoice2RxInitConfiguration().contextData.patient?.id.toString(),
                    mode = mode,
                    updatedSessionId = sessionId.value,
                    status = Voice2RxSessionStatus.DRAFT
                )
            )
        }
    }

    fun getSessionsByOwnerId(ownerId : String) {
        viewModelScope.launch(Dispatchers.IO) {
            _sessionsByOwnerId.value = repository.getSessionsByOwnerId(
                ownerId = ownerId,
            )
        }
    }

    private fun sendEndOfMessage() {
        val s3Url = "s3://$bucketName/$folderName/${sessionId.value}/"
        val files = mutableListOf<String>()
        chunksInfo.forEach { entry ->
            files.add(entry.key)
        }
        val eof = EndOfFileMessage(
            contextData = Voice2RxInit.getVoice2RxInitConfiguration().contextData,
            date = Voice2RxUtils.convertTimestampToISO8601(System.currentTimeMillis()),
            docOid = Voice2RxInit.getVoice2RxInitConfiguration().docOid,
            docUuid = Voice2RxInit.getVoice2RxInitConfiguration().docUuid,
            files = files,
            s3Url = s3Url,
            uuid = sessionId.value,
            chunksInfo = chunksInfo
        )
        VoiceLogger.d(TAG, "EOF : " + Gson().toJson(eof))
        val eofFile = saveJsonToFile("${sessionId.value}_eof.json", Gson().toJson(eof))
        recordedFiles.add(eofFile.name)
        AwsS3UploadService.uploadFileToS3(app, "eof.json", eofFile, folderName, sessionId.value, isAudio = false)
    }

    private fun uploadWholeFileData() {
        if (fullRecordingFile != null) {
            AwsS3UploadService.uploadFileToS3(
                app,
                "full_audio.m4a_",
                fullRecordingFile,
                folderName,
                sessionId.value,
                isFullAudio = true
            )
        }
    }

    override fun onAudio(audioData: ShortArray, timeStamp: Long) {
//        VoiceLogger.d("ProcessVoice","onAudio")
        audioChunks.add(audioData)
        if (audioChunks.size > 300) {
            audioChunks.removeFirst()
        }
        var isSpeech = false
        if (audioData.size == 512) {
            isSpeech = vad.isSpeech(audioData)
        }

        if (isSpeech) {
            _recordingResponse.value = "speech detected!"
        } else {
            _recordingResponse.value = "no speech detected!"
        }

        audioHelper.process(
            AudioRecordModel(
                frameData = audioData,
                isClipped = false,
                isSilence = !isSpeech,
                timeStamp = timeStamp
            )
        )
    }

    fun updateSession(updatedSessionId : String, status : Voice2RxSessionStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSession(
                sessionId = updatedSessionId.removePrefix("P-PP-"),
                updatedSessionId = updatedSessionId,
                status = status
            )
        }
    }

    fun getCombinedAudio(): ShortArray {
        VoiceLogger.d("ViewModel", "AudioChunks size : " + audioChunks.size.toString())
        val totalSize = audioChunks.sumOf { it.size }
        val combinedAudio = ShortArray(totalSize)
        var currentIndex = 0


        for (chunk in audioChunks) {
            chunk.copyInto(combinedAudio, currentIndex)
            currentIndex += chunk.size
        }

        return combinedAudio
    }

    override fun onCleared() {
        super.onCleared()
        if (::recorder.isInitialized) {
            recorder.stop()
        }
        if (::vad.isInitialized) {
            vad.close()
        }
    }

    override fun onSuccess(sessionId: String, fileName: String) {
        VoiceLogger.d(TAG, "Upload Successful : ${sessionId} ${fileName}")
    }

    override fun onError(sessionId: String, fileName: String, errorMsg: String) {
        VoiceLogger.d(TAG, "Upload Failed : ${sessionId} ${fileName}")
        sessionUploadStatus = false
    }
}