package com.eka.voice2rx_sdk.data.remote.services

import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.eka.voice2rx_sdk.common.ResponseState
import com.eka.voice2rx_sdk.common.UploadListener
import com.eka.voice2rx_sdk.common.Voice2RxUtils
import com.eka.voice2rx_sdk.common.VoiceLogger
import com.eka.voice2rx_sdk.sdkinit.AwsS3Configuration
import com.eka.voice2rx_sdk.sdkinit.V2RxInternal
import com.eka.voice2rx_sdk.sdkinit.Voice2Rx
import com.eka.voice2rx_sdk.sdkinit.Voice2RxInitConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object AwsS3UploadService {

    const val TAG = "AwsS3UploadService"
    private var transferUtility: TransferUtility? = null
    private var uploadListener : UploadListener? = null

    fun setUploadListener(listener: UploadListener) {
        uploadListener = listener
    }

    fun uploadFileToS3(
        context: Context,
        fileName: String,
        file: File,
        folderName: String,
        sessionId: String,
        isAudio : Boolean = true,
        isFullAudio : Boolean = false,
        s3Config : AwsS3Configuration? = null,
        onResponse : (ResponseState) -> Unit = {},
    ) {
        val config = V2RxInternal.s3Config
        if(config == null) {
            VoiceLogger.d("AwsS3UploadService", "Credential is null!")
            onResponse(ResponseState.Error("Credential is null!"))
            return
        }
        TransferNetworkLossHandler.getInstance(context.applicationContext)
        var voice2RxInitConfig : Voice2RxInitConfig? = null
        try {
            voice2RxInitConfig = Voice2Rx.getVoice2RxInitConfiguration()
        } catch (e : Exception) {
        }

        if(!Voice2RxUtils.isNetworkAvailable(context)) {
            onResponse(ResponseState.Error("No Internet!"))
            uploadListener?.onError(sessionId = sessionId, fileName = fileName, errorMsg = "No Internet!")
            return
        }
        val sessionCredentials: AWSSessionCredentials = BasicSessionCredentials(
            config.accessKey,
            config.secretKey,
            config.sessionToken
        )

        val clientConfiguration = ClientConfiguration().apply {
            retryPolicy = ClientConfiguration.DEFAULT_RETRY_POLICY
        }

        val s3Client = AmazonS3Client(sessionCredentials, clientConfiguration)

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
            transferUtility?.upload(config.bucketName, key, file,metadata)

        uploadObserver?.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                when(state) {
                    TransferState.COMPLETED -> {
                        deleteFile(file,!isFullAudio && isAudio)
                        onResponse(ResponseState.Success(true))
                        uploadListener?.onSuccess(sessionId = sessionId, fileName)
                    }
                    TransferState.FAILED -> {
                        onResponse(ResponseState.Error("FAILED"))
                        uploadListener?.onError(sessionId = sessionId, fileName = fileName, errorMsg = "FAILED")
                    }
                    TransferState.CANCELED -> {
                        onResponse(ResponseState.Error("CANCELED"))
                        uploadListener?.onError(sessionId = sessionId, fileName = fileName, errorMsg = "CANCELED")
                    }
                    else -> {
                    }
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDone = (bytesCurrent.toFloat() / bytesTotal.toFloat() * 100).toInt()
            }

            override fun onError(id: Int, ex: Exception?) {
                uploadListener?.onError(sessionId = sessionId, fileName = fileName, errorMsg = "CANCELED")
                onResponse(ResponseState.Error("FAILED"))
            }
        })
    }

    fun deleteFile(file: File, isAudioChunk : Boolean) {
        if(!isAudioChunk){
            return
        }
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