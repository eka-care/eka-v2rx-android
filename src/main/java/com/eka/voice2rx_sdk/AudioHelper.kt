package com.eka.voice2rx_sdk

import android.content.Context
import com.eka.voice2rx_sdk.common.Voice2RxUtils
import com.eka.voice2rx_sdk.common.VoiceLogger
import com.eka.voice2rx_sdk.data.local.models.FileInfo

class AudioHelper(
    private val context: Context,
    private val viewModel: V2RxViewModel,
    private val sessionId: String,
    prefLength: Int = 10,
    despLength: Int = 20,
    maxLength: Int = 25,
    sampleRate: Int = 16000
) {

    companion object {
        private const val TAG = "AudioHelper"
        private const val FRAME_SIZE = 512 // Frame size for processing
    }

    private val audioRecordData = mutableListOf<AudioRecordModel>()
    private val clipPoints = mutableListOf(0)
    private val clipTimeStamps = mutableListOf(0L)

    private var silenceDuration = 0
    private var lastClipIndex = 0
    private var currentClipIndex = 0
    private var isClipping = false

    private val prefLengthSamples = prefLength * sampleRate
    private val despLengthSamples = despLength * sampleRate
    private val maxLengthSamples = maxLength * sampleRate
    private val shortThreshold = (0.1 * sampleRate).toInt()
    private val longThreshold = (0.5 * sampleRate).toInt()

    var chunksInfo: Map<String, FileInfo> = mutableMapOf()
        private set

    fun process(audioRecordModel: AudioRecordModel) {
        updateSilenceDuration(audioRecordModel)

        val samplesPassed = (audioRecordData.size * FRAME_SIZE) - (currentClipIndex * FRAME_SIZE)
        var isClipPointFrame = false
        var clipTime = -1L

        when {
            samplesPassed > prefLengthSamples && silenceDuration > longThreshold -> {
                isClipPointFrame = handleClipPoint()
                clipTime = audioRecordData.last().timeStamp
            }
            samplesPassed > despLengthSamples && silenceDuration > shortThreshold -> {
                isClipPointFrame = handleClipPoint()
                clipTime = audioRecordData.last().timeStamp
            }
            samplesPassed >= maxLengthSamples -> {
                isClipPointFrame = handleClipPoint()
                clipTime = audioRecordData.last().timeStamp
            }
        }

        audioRecordModel.isClipped = isClipPointFrame
        audioRecordData.add(audioRecordModel)

        if (isClipPointFrame) {
            silenceDuration = 0
            clipTimeStamps.add(clipTime)
            viewModel.getUploadService().processAndUpload(lastClipIndex, currentClipIndex)
        }
    }

    private fun updateSilenceDuration(audioRecordModel: AudioRecordModel) {
        silenceDuration = if (audioRecordModel.isSilence) {
            silenceDuration + FRAME_SIZE
        } else {
            0
        }
    }

    private fun handleClipPoint(): Boolean {
        lastClipIndex = currentClipIndex
        currentClipIndex = audioRecordData.size
        clipPoints.add(currentClipIndex)
        isClipping = true
        VoiceLogger.d(TAG, "Clip detected at index: $currentClipIndex")
        return true
    }

    fun isClipping() = isClipping

    fun getAudioRecordData(): List<AudioRecordModel> = audioRecordData

    fun removeData(startIndex: Int, endIndex: Int) {
        isClipping = false
        lastClipIndex = currentClipIndex
    }

    fun uploadLastData() {
        lastClipIndex = currentClipIndex
        currentClipIndex = audioRecordData.size - 1
        isClipping = true
        viewModel.getUploadService().processAndUpload(lastClipIndex, currentClipIndex)
    }

    fun onNewFileCreated(fileName: String, endTimeStamp: Long, startTimeStamp: Long) {
        val firstTimeStamp = audioRecordData.firstOrNull()?.timeStamp ?: 0L
        val startTime = Voice2RxUtils.calculateDuration(firstTimeStamp, startTimeStamp)
        val endTime = Voice2RxUtils.calculateDuration(firstTimeStamp, endTimeStamp)
        viewModel.addValueToChunksInfo(fileName, FileInfo(st = startTime.toString(), et = endTime.toString()))
    }
}
