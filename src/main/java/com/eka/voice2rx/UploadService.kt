package com.eka.voice2rx

import android.content.Context
import android.util.Log
import com.eka.voice2rx.sdkinit.Voice2RxInit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class UploadService(
    private val context: Context,
    private val audioHelper: AudioHelper,
    private val sessionId: String
) {
    companion object {
        const val TAG = "UploadService"
    }

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val audioCombiner = AudioCombiner()

    var FILE_INDEX = 0

    fun processAndUpload(lastClipIndex1: Int, currentClipIndex: Int) {
        if (!audioHelper.isHavingClippedComponent()) {
            return
        }
        coroutineScope.launch {
            try {
                val audioData = audioHelper.getAudioRecordData()
                val clippedAudioData = ArrayList<ShortArray>()

                val clipIndex = currentClipIndex
                val lastClipIndex = lastClipIndex1
                if (clipIndex == -1) {
//                    return@launch
                } else {
                    clippedAudioData.addAll(
                        audioData.subList(lastClipIndex + 1, clipIndex + 1).map { it.frameData })
                    var startTimeStamp = audioData.first().timeStamp
                    if (lastClipIndex > 0) {
                        startTimeStamp = audioData[lastClipIndex - 1].timeStamp
                    }
                    val endTimeStamp = audioData[clipIndex - 1].timeStamp
                    generateAudioFileFromAudioData(
                        clippedAudioData,
                        getCombinedAudio(clippedAudioData),
                        startTimeStamp,
                        endTimeStamp
                    )
                    audioHelper.removeData(0, clipIndex)
                }
            } catch (e: Exception) {
                Log.d(TAG, e.printStackTrace().toString())
            }
        }
    }

    fun getCombinedAudio(audioChunks: ArrayList<ShortArray>): ShortArray {
        val totalSize = audioChunks.sumOf { it.size }
        val combinedAudio = ShortArray(totalSize)
        var currentIndex = 0

        for (chunk in audioChunks) {
            chunk.copyInto(combinedAudio, currentIndex)
            currentIndex += chunk.size
        }

        return combinedAudio
    }

    fun generateAudioFileFromAudioData(
        audioDataList: List<ShortArray>,
        audioData: ShortArray,
        start: Long,
        end: Long
    ) {
        FILE_INDEX += 1
        val fileName = "${sessionId + "_" + FILE_INDEX}.m4a"
        val wavFileName = "${sessionId + "_" + FILE_INDEX}.wav"
        val outputFile = File(context.filesDir, fileName)

        audioHelper.onNewFileCreated(fileName, end, start)
        audioCombiner.writeWavFile(
            context,
            File(context.filesDir, wavFileName),
            outputFile,
            audioData,
            Voice2RxInit.getVoice2RxInitConfiguration().sampleRate,
            VADViewModel.folderName,
            sessionId
        )
    }
}