package com.eka.voice2rx_sdk.common

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

object Voice2RxUtils {
    fun getCurrentUTCEpochMillis(): Long {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
    }
    fun getCurrentDateInYYMMDD(): String {
        val date = Date()
        val formatter = SimpleDateFormat("yyMMdd", Locale.getDefault())
        return formatter.format(date)
    }

    fun calculateDuration(start: Long, end: Long): Double {
        val durationMillis = end - start
        val durationSeconds = durationMillis / 1000.0
        return String.format("%.2f", durationSeconds).toDouble()
    }

    fun convertTimestampToISO8601(timestamp: Long): String {
        val date = Date(timestamp)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC") // Set to UTC
        return sdf.format(date)
    }

    fun generateNewSessionId() : String {
        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis()
    }

    fun getFullRecordingFileName(sessionId : String) : String {
        return "${sessionId}_Full_Recording.m4a_"
    }

    fun getFilePath(context: Context, sessionId : String) : String {
        return context.applicationContext.filesDir.absolutePath + "/" + getFullRecordingFileName(sessionId)
    }
}