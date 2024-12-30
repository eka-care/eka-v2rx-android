package com.eka.voice2rx

import android.content.Context
import android.util.Log
import com.eka.voice2rx.common.Utils
import com.eka.voice2rx.models.FileInfo

class AudioHelper(
    private val context: Context,
    private val viewModel: VADViewModel,
    private val sessionId: String,
    prefLength: Int = 10,
    despLength: Int = 20,
    maxLength: Int = 25,
    sampleRate: Int = 16000
) {

    companion object {
        const val TAG = "AudioHelper"
    }

    private val audioRecordData = mutableListOf<AudioRecordModel>()
    private var lastClipIndex = 0
    private var currentClipIndex = 0
    private val clipPoints = mutableListOf(0)
    private var silenceDuration = 0
    private val prefLengthSamples = prefLength * sampleRate
    private val despLengthSamples = despLength * sampleRate
    private val maxLengthSamples = maxLength * sampleRate
    private val shortThreshold = (0.1 * sampleRate).toInt()
    private val longThreshold = (0.5 * sampleRate).toInt()
    private var isHavingClippedComponent = false
    private var clipTimeStamps = mutableListOf(0L)
    var chunksInfo: Map<String, FileInfo> = mutableMapOf()

    fun process(audioRecordModel: AudioRecordModel) {
        var isClipPointFrame = false
        var clipTime = -1L

        if (audioRecordData.isNotEmpty()) {
            if (audioRecordModel.isSilence) {
                silenceDuration += 512
            }
            if (!audioRecordModel.isSilence) {
                silenceDuration = 0
            }
        }
//        Log.d(TAG,"pref : " + prefLengthSamples.toString() + "desp : " + despLengthSamples.toString() + "max : " + maxLengthSamples.toString())

        val samplesPassed = (audioRecordData.size * 512) - (currentClipIndex * 512)
        if (samplesPassed > prefLengthSamples) {
            if (silenceDuration > longThreshold) {
//                currentClipIndex = audioRecordData.size - ((silenceDuration / 512) / 2)
                clipTime = audioRecordData.last().timeStamp
                currentClipIndex = audioRecordData.size
                clipPoints.add(lastClipIndex)
                isClipPointFrame = true
                Log.d(
                    TAG,
                    "prefLengthSamples Clip Index : ${lastClipIndex} ${currentClipIndex} ${samplesPassed} ${silenceDuration}"
                )
            }
        }

        if (samplesPassed > despLengthSamples) {
            if (silenceDuration > shortThreshold) {
//                currentClipIndex = audioRecordData.size - ((silenceDuration / 512) / 2)
                currentClipIndex = audioRecordData.size
                clipTime = audioRecordData.last().timeStamp
                clipPoints.add(lastClipIndex)
                isClipPointFrame = true
                Log.d(
                    TAG,
                    "despLengthSamples Clip Index : ${lastClipIndex} ${currentClipIndex} ${samplesPassed} ${silenceDuration}"
                )
            }
        }

        if (samplesPassed >= maxLengthSamples) {
            clipTime = audioRecordData.last().timeStamp
            currentClipIndex = audioRecordData.size
            clipPoints.add(lastClipIndex)
            isClipPointFrame = true
            Log.d(
                TAG,
                "MaxLengthSamples Clip Index : ${lastClipIndex} ${currentClipIndex} ${samplesPassed} ${silenceDuration}"
            )
        }
        audioRecordModel.isClipped = isClipPointFrame
        if (isClipPointFrame) {
            silenceDuration = 0
//            Log.d(TAG,"Clip Index : ${lastClipIndex} ${currentClipIndex}")
        }

        if (isHavingClippedComponent == false && isClipPointFrame) {
            isHavingClippedComponent = true
        }

        audioRecordData.add(audioRecordModel)
        if (isClipPointFrame) {
            clipTimeStamps.add(clipTime)
            viewModel.getUploadService().processAndUpload(lastClipIndex, currentClipIndex)
        }
    }

    fun isHavingClippedComponent() = isHavingClippedComponent

    fun getAudioRecordData(): List<AudioRecordModel> = audioRecordData

    fun removeData(startIndex: Int, endIndex: Int) {
        isHavingClippedComponent = false
        lastClipIndex = currentClipIndex
    }

    fun uploadLastData() {
        lastClipIndex = currentClipIndex
        currentClipIndex = audioRecordData.size - 1
        isHavingClippedComponent = true
        viewModel.getUploadService().processAndUpload(lastClipIndex, currentClipIndex)
    }

    fun onNewFileCreated(fileName: String, endTimeStamp: Long, startTimeStamp: Long) {
        val startTime = Utils.calculateDuration(audioRecordData.first().timeStamp, startTimeStamp)
        val endTime = Utils.calculateDuration(audioRecordData.first().timeStamp, endTimeStamp)
        viewModel.addValueToChunksInfo(fileName, FileInfo(startTime.toString(), endTime.toString()))
    }
}