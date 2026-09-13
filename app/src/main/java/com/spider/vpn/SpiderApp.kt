package com.spider.vpn

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.spider.vpn.data.db.AppDatabase

class SpiderApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "spider_vpn", "Spider VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "VPN Connection Status" }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }
}
