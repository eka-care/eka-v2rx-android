package com.eka.voice2rx

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.eka.voice2rx.common.UploadServiceConstants
import com.eka.voice2rx.common.Utils
import com.eka.voice2rx.models.EndOfFileMessage
import com.eka.voice2rx.models.FileInfo
import com.eka.voice2rx.models.StartOfMessage
import com.eka.voice2rx.recorder.AudioCallback
import com.eka.voice2rx.recorder.VoiceRecorder
import com.eka.voice2rx.sdkinit.Voice2RxInit
import com.eka.voice2rx.services.AwsS3UploadService
import com.google.gson.Gson
import com.konovalov.vad.silero.Vad
import com.konovalov.vad.silero.VadSilero
import com.konovalov.vad.silero.config.FrameSize
import com.konovalov.vad.silero.config.Mode
import com.konovalov.vad.silero.config.SampleRate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID

class VADViewModel(
    val app: Application,
) : AndroidViewModel(app), AudioCallback {

    companion object {
        const val TAG = "VADViewModel"
        val bucketName = Voice2RxInit.getVoice2RxInitConfiguration().s3Config.bucketName
        val folderName: String = Utils.getCurrentDateInYYMMDD()
        val config = Voice2RxInit.getVoice2RxInitConfiguration()
//        var chunksInfo : Map<String,FileInfo> = mutableMapOf()
    }

    private val DEFAULT_MODE = Mode.NORMAL
    private val DEFAULT_SILENCE_DURATION_MS = 300

    private val audioChunks = mutableListOf<ShortArray>()

    private var chunksInfo = mutableMapOf<String, FileInfo>()

    val sessionId = mutableStateOf(UUID.randomUUID().toString() + "_" + System.currentTimeMillis())

    private lateinit var recorder: VoiceRecorder
    private lateinit var audioHelper: AudioHelper
    private lateinit var uploadService: UploadService
    private lateinit var vad: VadSilero
    private var isRecording = false

    private val _recordingResponse = MutableStateFlow<String>("Analyzing...")
    val recordingResponse = _recordingResponse.asStateFlow()

    private val FULL_RECORDING_FILE_NAME =
        UUID.randomUUID().toString() + "_" + "Full_Recording.m4a_"

    private lateinit var fullRecordingFile: File

    fun addValueToChunksInfo(fileName: String, fileInfo: FileInfo) {
        chunksInfo[fileName.split("_").last()] = fileInfo
    }

    fun getUploadService(): UploadService {
        return uploadService
    }

    fun startRecording() {
        startUploadService()
        sessionId.value = UUID.randomUUID().toString() + "_" + System.currentTimeMillis()

        vad = Vad.builder()
            .setContext(app)
            .setSampleRate(SampleRate.valueOf(config.sampleRate.toString()))
            .setFrameSize(FrameSize.valueOf(config.frameSize.toString()))
            .setMode(DEFAULT_MODE)
            .setSilenceDurationMs(DEFAULT_SILENCE_DURATION_MS)
            .build()

        recorder = VoiceRecorder(this)
        audioHelper = AudioHelper(app, this, sessionId.value)
        uploadService = UploadService(app, audioHelper, sessionId.value)
        uploadService.FILE_INDEX = 0

        isRecording = true
        fullRecordingFile = File(app.filesDir, FULL_RECORDING_FILE_NAME)
        chunksInfo = mutableMapOf<String, FileInfo>()
        recorder.start(app, fullRecordingFile, vad.sampleRate.value, vad.frameSize.value)
        Log.d(TAG, "Recording started")
        Log.d(TAG, "FolderName : $folderName" + " SessionId : $sessionId")
        sendStartOfMessage()
        config.onStart.invoke(sessionId.value)
    }

    private fun sendStartOfMessage() {
        val s3Url = "s3://$bucketName/$folderName/${sessionId.value}/"
        val som = StartOfMessage(
            contextData = Voice2RxInit.getVoice2RxInitConfiguration().contextData,
            date = Utils.convertTimestampToISO8601(System.currentTimeMillis()),
            docOid = Voice2RxInit.getVoice2RxInitConfiguration().docOid,
            docUuid = Voice2RxInit.getVoice2RxInitConfiguration().docUuid,
            files = listOf(),
            mode = "dictation",
            s3Url = s3Url,
            uuid = sessionId.value
        )
        Log.d(TAG, "SOM : " + Gson().toJson(som))
        val somFile = saveJsonToFile("som.json", Gson().toJson(som))
        AwsS3UploadService.uploadFileToS3(app, "som.json", somFile, folderName, sessionId.value)
        somFile.deleteOnExit()
    }

    fun saveJsonToFile(fileName: String, jsonContent: String): File {
        val file = File(app.filesDir, fileName)
        file.writeText(jsonContent)
        return file
    }

    private fun startUploadService() {
        val intent = Intent(app, AwsS3UploadService::class.java).apply {
            this.action = UploadServiceConstants.ACTION_START_OR_RESUME_SERVICE
        }
        app.startService(intent)
    }

    fun stopRecording() {
        isRecording = false
        recorder.stop()
        audioHelper.uploadLastData()
        uploadWholeFileData()
        sendEndOfMessage()
        stopUploadService()
        config.onStop.invoke(sessionId.value)
    }

    private fun sendEndOfMessage() {
        val s3Url = "s3://$bucketName/$folderName/${sessionId.value}/"
        val files = mutableListOf<String>()
        chunksInfo.forEach { entry ->
            files.add(entry.key)
        }
        val eof = EndOfFileMessage(
            contextData = Voice2RxInit.getVoice2RxInitConfiguration().contextData,
            date = Utils.convertTimestampToISO8601(System.currentTimeMillis()),
            docOid = Voice2RxInit.getVoice2RxInitConfiguration().docOid,
            docUuid = Voice2RxInit.getVoice2RxInitConfiguration().docUuid,
            files = files,
            s3Url = s3Url,
            uuid = sessionId.value,
            chunksInfo = chunksInfo
        )
        Log.d(TAG, "EOF : " + Gson().toJson(eof))
        val eofFile = saveJsonToFile("eof.json", Gson().toJson(eof))
        AwsS3UploadService.uploadFileToS3(app, "eof.json", eofFile, folderName, sessionId.value)
        eofFile.deleteOnExit()
    }

    private fun uploadWholeFileData() {
        if (fullRecordingFile != null) {
            AwsS3UploadService.uploadFileToS3(
                app,
                "full_audio.m4a_",
                fullRecordingFile,
                folderName,
                sessionId.value
            )
        }
    }

    private fun stopUploadService() {
        val intent = Intent(app, AwsS3UploadService::class.java).apply {
            this.action = UploadServiceConstants.ACTION_STOP_SERVICE
        }
        app.startService(intent)
    }

    override fun onAudio(audioData: ShortArray, timeStamp: Long) {
//        Log.d("ProcessVoice","onAudio")
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

    fun getCombinedAudio(): ShortArray {
        Log.d("ViewModel", "AudioChunks size : " + audioChunks.size.toString())
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
        recorder.stop()
        vad.close()
    }
}