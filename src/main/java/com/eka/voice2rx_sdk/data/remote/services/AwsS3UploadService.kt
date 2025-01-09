package com.eka.voice2rx_sdk.data.remote.services

import android.content.Context
import android.util.Log
import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.eka.voice2rx_sdk.sdkinit.Voice2RxInit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AwsS3UploadService {

    companion object {
        const val TAG = "AwsS3UploadService"
        private var transferUtility: TransferUtility? = null

        fun uploadFileToS3(
            context: Context,
            fileName: String,
            file: File,
            folderName: String,
            sessionId: String,
            isAudio : Boolean = true
        ) {
            TransferNetworkLossHandler.getInstance(context.applicationContext)
            val voice2RxInitConfig = Voice2RxInit.getVoice2RxInitConfiguration()

            val sessionCredentials: AWSSessionCredentials = BasicSessionCredentials(
                voice2RxInitConfig.s3Config.accessKey,
                voice2RxInitConfig.s3Config.secretKey,
                voice2RxInitConfig.s3Config.sessionToken
            )

            val s3Client = AmazonS3Client(sessionCredentials)
            transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build()

            val key = "$folderName/$sessionId/${fileName}"

            val metadata = ObjectMetadata()
            if(isAudio) {
                metadata.contentType = "audio/wav"
            } else {
                metadata.contentType = "application/json"
            }

            val uploadObserver =
                transferUtility?.upload(voice2RxInitConfig.s3Config.bucketName, key, file,metadata)

            uploadObserver?.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState?) {
                    Log.d("AudioUploadService", "Upload state: ${state?.name}")

                    when(state) {
                        TransferState.COMPLETED -> {
                            deleteFile(file)
                        }
                        TransferState.FAILED -> {
                            deleteFile(file)
                        }
                        TransferState.CANCELED -> {
                            deleteFile(file)
                        }
                        else -> {

                        }
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    val percentDone = (bytesCurrent.toFloat() / bytesTotal.toFloat() * 100).toInt()
                    Log.d("AudioUploadService", "Upload progress: $percentDone%")
                }

                override fun onError(id: Int, ex: Exception?) {
                    Log.e("AudioUploadService", "Error during upload: ${ex?.message}")
                }
            })
        }

        fun deleteFile(file: File) {
            CoroutineScope(Dispatchers.IO).launch {
                if (file.exists()) {
                    try {
                        file.delete()
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }
}