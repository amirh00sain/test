package com.spider.vpn.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.spider.vpn.data.model.*

@Database(
    entities = [Session::class, Subscription::class, Config::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun configDao(): ConfigDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spider_vpn.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
