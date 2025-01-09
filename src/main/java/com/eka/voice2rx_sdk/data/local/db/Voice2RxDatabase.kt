package com.eka.voice2rx_sdk.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eka.voice2rx_sdk.data.local.db.daos.VToRxSessionDao
import com.eka.voice2rx_sdk.data.local.db.entities.VToRxSession

@Database(
    entities = [
        VToRxSession::class
    ],
    version = 1,
    exportSchema = false
)
abstract class Voice2RxDatabase : RoomDatabase() {
    abstract fun getVoice2RxDao(): VToRxSessionDao
    companion object {
        private const val VOICE_TO_RX_DATABASE_NAME = "voice2rx_db"

        @Volatile
        private var INSTANCE : Voice2RxDatabase? = null

        fun getDatabase(context: Context) : Voice2RxDatabase {
            return (INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Voice2RxDatabase::class.java,
                    VOICE_TO_RX_DATABASE_NAME
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            })
        }
    }

}