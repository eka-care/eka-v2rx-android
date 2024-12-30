package com.eka.voice2rx.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.amazonaws.auth.AWSSessionCredentials
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.eka.voice2rx.common.UploadServiceConstants
import com.eka.voice2rx.sdkinit.Voice2RxInit
import java.io.File

class AwsS3UploadService : Service() {

    companion object {
        const val TAG = "AwsS3UploadService"
        private var transferUtility: TransferUtility? = null

        fun uploadFileToS3(
            context: Context,
            fileName: String,
            file: File,
            folderName: String,
            sessionId: String
        ) {
//            if(transferUtility == null){
            TransferNetworkLossHandler.getInstance(context.applicationContext)
            val voice2RxInitConfig = Voice2RxInit.getVoice2RxInitConfiguration()
//                val authModel = Gson().fromJson("{\n" +
//                        "    \"token\": \"eyJraWQiOiJhcC1zb3V0aC0xLTYiLCJ0eXAiOiJKV1MiLCJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhcC1zb3V0aC0xOjgyNGJhZmQwLTc1M2EtY2QzYi0xNDJlLWM4YjBiZjM4ZTQ4MiIsImF1ZCI6ImFwLXNvdXRoLTE6YmU3ZTY5OTQtNGViOC00YWFhLWI1YTktN2NjMTc1N2MzMmUxIiwiYW1yIjpbImF1dGhlbnRpY2F0ZWQiLCJwaW5wb2ludCIsInBpbnBvaW50OmFwLXNvdXRoLTE6YmU3ZTY5OTQtNGViOC00YWFhLWI1YTktN2NjMTc1N2MzMmUxOnRlc3QiXSwiaXNzIjoiaHR0cHM6Ly9jb2duaXRvLWlkZW50aXR5LmFtYXpvbmF3cy5jb20iLCJodHRwczovL2NvZ25pdG8taWRlbnRpdHkuYW1hem9uYXdzLmNvbS9pZGVudGl0eS1wb29sLWFybiI6ImFybjphd3M6Y29nbml0by1pZGVudGl0eTphcC1zb3V0aC0xOjU1OTYxNTU2MTg0NTppZGVudGl0eXBvb2wvYXAtc291dGgtMTpiZTdlNjk5NC00ZWI4LTRhYWEtYjVhOS03Y2MxNzU3YzMyZTEiLCJleHAiOjE3MzUxOTY0NDEsImlhdCI6MTczNTE5Mjg0MX0.cBszJvIfZNuj6WaJGlFEPT4PvUOYjKO43E22Ddgsf_HTFchlJODIjj5nSDTYsCXGwnidA9iWprI8yhbE_juPG_hUMysKCxGykBUasFe6sWlt3XodBwg6CVhYDWg6V8lBXrFNUlG8c-7em4PWYRjP99sBAXd_YG_aGaokMHZpTkwBlmOWcSim4kgj_0kqbqM6OCTRjrYzX-__m4l8hwvZVk4oBbKYft7nIt4XNkXAIPA_bq0LNdCeWFvN7PaicC1cMlaO7h_GrWCi5PeWvXtj2UFQZ60e0AGhD-yYr_UJw-PL8mKvDoEacQUaJ2fzL2M1Bw-oBwQqj5nmKo7ry6mREQ\",\n" +
//                        "    \"identity_id\": \"ap-south-1:824bafd0-753a-cd3b-142e-c8b0bf38e482\",\n" +
//                        "    \"expiry\": 3600,\n" +
//                        "    \"credentials\": {\n" +
//                        "        \"AccessKeyId\": \"ASIAYES5P2B2357GMK4D\",\n" +
//                        "        \"SecretKey\": \"+U9e4JoEcyK973lUOeJkvV/OD06UxyrFGgXYizXI\",\n" +
//                        "        \"SessionToken\": \"IQoJb3JpZ2luX2VjEE4aCmFwLXNvdXRoLTEiRzBFAiEA0mgIknYiQ+bt+N54IlY3J257HG/bbXw7u9U/J68B5gICIA2hniCCuAFJ6w1LFLFJom5d0P38zw2N5oUnKXkm0FxYKuoDCCcQARoMNTU5NjE1NTYxODQ1IgyJelkyCrBte7je7cMqxwOTZqU2nlFhjPZHsq37sNvdWYm37ijrQhtn81g/kIIcddUch5hMzl3Pz9RF32nX6vz/h3Uud878LaMA80jCcF8WLLYrn8Nc7/pDsCaWbFrUukgG9gWeDFKA7EwQlJzm4hvKTRXz6G/nWSp5WnHUk5JewcV7Ep23E1chbWRCNDBB/KOYj1cxppvH39czvj0Bt/gK1deUg0SFIAUT/xm9/DWZ+RBh0nrJmFmyysuCK6P0/sWXXwDnTPgrJyYeDfFCpAhfZhnyahWM/DOSraBKoGX4R5MuiXEPS0GwqTAdzN1tJ5ar1IeiyhEbqZg8k0u7osLfFY6HbaxRhR7lFhq3J80beB7ITjO7KGM7ybDQ1PVzCSfQPhLe9NawoL4ujdFLo5ouq8qMkQthFUBN6raqLsuy+r5i8nItiqdY+4t0ylmgqnyVkMamlRcapH0XVRpIK9Cjk0zy7W3cXzHBnK/OeFNSP1lq7fxvRC3DKEVwyj6IC/GOXf4/cAosnxLEUy5TBAznFQpxxwE6u3Zebz+jUtnCN7zOfEwzlWljS0Tgm1h5TDtjrlGAwqEiZzSNwo8848YH0ihqaVhPQSOFCwGRy8lPJwdBWbytyzCJ4rO7BjqEApSwP4+VNYJQKL2jqNgjsZ+2VPegfyMs0tKCSmYR7DgixFq6J3i89MXDu+qXnceR1HpIpc+BrmI5j0Dwwf6qLdD2dqOcLxjRyKAJKGcT7TlXFG6zLAA0qNgV9YRx79/jg2BL99gCN1irZgVz86/rWPlWPQ1b1/3IZEqgiBbkAuAqq8Or0N+CVPwOsaAErqBqGe5d9T2XKdWBxd+TS1gEfANS+Tpy5ORiyKI+5eBNde3+VpVRnkV/5LiAIpLwUF3e4k52fktQlWvgbItH2K3kPnVTdYknpaUe4tvZpk/qaaS2MPM+8gs/O6THC0dSGdC/FpN0jtJxqPvnkTgDcaTMmKkIIMFm\",\n" +
//                        "        \"Expiration\": \"2024-12-26 07:00:41+00:00\"\n" +
//                        "    }\n" +
//                        "}", AuthModel::class.java)

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
//            }

            val key = "$folderName/$sessionId/${fileName}"

            val uploadObserver =
                transferUtility?.upload(voice2RxInitConfig.s3Config.bucketName, key, file)

            uploadObserver?.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState?) {
                    if (state == TransferState.COMPLETED) {
                        Log.d("AudioUploadService", "Upload completed")
                    } else if (state == TransferState.FAILED) {
                        Log.e("AudioUploadService", "Upload failed")
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
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
        intent?.let {
            when (it.action) {
                UploadServiceConstants.ACTION_START_OR_RESUME_SERVICE -> {
                    startUploadService()
                }

                UploadServiceConstants.ACTION_PAUSE_SERVICE -> {
                    pauseUploadService()
                }

                UploadServiceConstants.ACTION_STOP_SERVICE -> {
                    stopUploadService()
                }

                else -> {}
            }
        }
        return START_STICKY
    }

    private fun stopUploadService() {
        stopSelf()
    }

    private fun pauseUploadService() {
    }

    private fun startUploadService() {
        val sessionCredentials: AWSSessionCredentials = BasicSessionCredentials(
            "ASIAYES5P2B2Q4JIX5J3",
            "hxQ6DCS9la0L4OYz8nHcpJI9/5mdZ9k+PTGtA8Hw",
            "IQoJb3JpZ2luX2VjECUaCmFwLXNvdXRoLTEiRzBFAiEAxTnFOKrAY4mOs7XIsnL4hDcVggEV9ocMswMUYxEkNocCIDEgeqJ1I9o/lfFJooZrw89Bfzaf7oMF/d1iyFHqEMjRKvMDCO7//////////wEQARoMNTU5NjE1NTYxODQ1IgxicEhfjdr/67BUYkwqxwMzXpEDL2M8A2rCBO3tB+ZNHNxV5yXsxfttf43a2uAzPE+M3s9KkxbDmRoSA8IE0nPsnyVk0iYtIekoI8KtrYhVjhGuDLes9dqta88phwaRsW/21fyE5PUMBNJIjBxudWjsbIBYGl589o1rXKAeM+rm/BbE7L8aXwWLV2cgD6nSbAzDsYIPSLjEESN0EJcWH7A6NnJfQyxGf7DDnxW/p8b1vpBdp8LLjZPY0nnLEeK5nyAU+x/CenyZmpBraecbWdoMKWHJJsEotRe09YzjCk5ZwpcVaDHmwDdayYJMQeVu/V59lX3w4Gix0n4hR1TGe/IMPd8vm43oIOsS8imYQrOt0YUiVx/rtqVSTYGcDqb0DhDBvMMubw21UZanR9gkrI4s7oHrlG/G8tFKqZo6x9z8IIimWkihKcytSbXGivP7JyzrqzN3j2Bhuw/DshwW6HsCy5YjCjR8hFxqGZoT1p8U5+hq6HP5zPmJmd1u317z2t1wZB75bgYDXrwYZB1J8k0cRuu37kis3t73zcOalNk81qMoLmeSf3FtiRXragxfu31rl52hS6LQNzHrfw38bZc0VTkBQtvhJIlvdfxnV4oAYjVTcfy22TDJ2qq7BjqEArnp0GhG+ieLax+5N+d6ZUWNlZ35d51qEJvoCDf/TID1xbiZq40EEoT45VJu6fyjZ0jZxUCCw6MKyOeIg3XinwbvA4Iogbpsg5UBctHBPyI6hWiAtFMRs2O1iPTsaqTpgQQ6S5XrFZ4wZ2wt0UAUgz0l6EBsieVyGCBJy3UQAqaIINqruKCFYew/G4Co4NFd3YtBwTlty8opAsoiV8A50p7mxyWT/3NBXKPKew4zQfL3u6SpSg/FPzgblH+ZgY5fKEeWZ18M8CDyyCM5x9mRD6UvclnyAgASRiYuHeUwe5tOIre8669Pm+WLZHcFiqT7UByGsWkVGeI6INqRfoOyuGjoeBKz"
        )

        val s3Client = AmazonS3Client(sessionCredentials)
        transferUtility = TransferUtility.builder()
            .context(applicationContext)
            .s3Client(s3Client)
            .build()
    }
}