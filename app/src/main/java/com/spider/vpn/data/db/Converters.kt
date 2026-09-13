package com.spider.vpn.data.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromLong(value: Long?): String = value?.toString() ?: ""

    @TypeConverter
    fun toLong(value: String): Long? = value.toLongOrNull()
}
