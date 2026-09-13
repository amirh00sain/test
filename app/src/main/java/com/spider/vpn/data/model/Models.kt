package com.spider.vpn.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val domain: String,
    val apiKey: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val url: String,
    val isLocal: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "configs")
data class Config(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subId: Long = 0,
    val name: String = "",
    val server: String = "",
    val port: Int = 443,
    val protocol: String = "vless",
    val uuid: String = "",
    val sni: String = "",
    val security: String = "tls",
    val fingerprint: String = "chrome",
    val fragment: String = "",
    val network: String = "tcp",
    val address: String = "",
    val remark: String = "",
    val rawConfig: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val filePath: String? = null,
    val fileType: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class DnsSettings(
    val type: DnsType = DnsType.GOOGLE,
    val primaryDns: String = "",
    val secondaryDns: String = ""
)

enum class DnsType { GOOGLE, CLOUDFLARE, CUSTOM }

data class SpeedTestResult(
    val downloadSpeed: Float = 0f,
    val uploadSpeed: Float = 0f,
    val ping: Long = 0L
)

data class TrafficInfo(
    val uploadBytes: Long = 0L,
    val downloadBytes: Long = 0L,
    val isConnected: Boolean = false
)
