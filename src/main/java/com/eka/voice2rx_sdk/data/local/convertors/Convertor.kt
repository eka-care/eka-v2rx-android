package com.eka.voice2rx_sdk.data.local.convertors

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Convertor {
    private val gson = Gson()

    @TypeConverter
    fun fromList(value: List<Int>?): String? {
        return value?.joinToString(",") // Convert List<Int> to String
    }

    @TypeConverter
    fun toList(value: String?): List<Int>? {
        return value?.split(",")?.map { it.toInt() } // Convert String back to List<Int>
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
}