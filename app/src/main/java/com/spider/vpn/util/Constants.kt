package com.spider.vpn.util

object Constants {
    const val RAILWAY_URL = "https://railway.com"
    const val TELEGRAM_URL = "https://t.me/"
    const val GITHUB_URL = "https://github.com/"
    const val YOUTUBE_URL = "https://youtube.com/"
    const val UPDATE_URL = "https://github.com/"

    const val NOTIFICATION_CHANNEL = "spider_vpn"
    const val NOTIFICATION_ID = 1

    const val VPN_MTU = 1500

    val DNS_SERVERS = mapOf(
        "google" to listOf("8.8.8.8", "8.8.4.4"),
        "cloudflare" to listOf("1.1.1.1", "1.0.0.1"),
    )
}
