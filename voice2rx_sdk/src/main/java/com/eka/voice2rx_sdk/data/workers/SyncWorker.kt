package com.eka.voice2rx_sdk.data.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.eka.voice2rx_sdk.common.VoiceLogger
import com.eka.voice2rx_sdk.sdkinit.Voice2Rx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncWorker(
    context : Context,
    params : WorkerParameters
) : Worker(context,params) {
    override fun doWork(): Result {
        CoroutineScope(Dispatchers.IO).launch {
            Voice2Rx.updateAllSessions()
            Voice2Rx.getSessions()?.forEach {
                val sessionStatus = Voice2Rx.getVoice2RxSessionStatus(sessionId = it.sessionId)
                VoiceLogger.d("SyncWorker", "Session Status: $sessionStatus")
            }
        }
        return Result.success()
    }
}