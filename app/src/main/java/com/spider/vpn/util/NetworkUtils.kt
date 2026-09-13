package com.spider.vpn.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

object NetworkUtils {

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun getPublicIp(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.ipify.org?format=json")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            org.json.JSONObject(response).optString("ip", "Unknown")
        } catch (e: Exception) {
            "Unknown"
        }
    }

    suspend fun measurePing(host: String = "1.1.1.1", port: Int = 443): Long = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 3000)
            val ping = System.currentTimeMillis() - start
            socket.close()
            ping
        } catch (e: Exception) {
            -1L
        }
    }

    suspend fun measureDownloadSpeed(): Float = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://speedtest.tele2.net/10MB.zip")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val startTime = System.currentTimeMillis()
            var totalBytes = 0L
            val buffer = ByteArray(8192)
            val input = conn.inputStream

            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                totalBytes += read
                val elapsed = System.currentTimeMillis() - startTime
                if (elapsed > 5000) break
            }

            conn.disconnect()
            val elapsed = (System.currentTimeMillis() - startTime).toFloat() / 1000f
            if (elapsed > 0) (totalBytes * 8f / elapsed / 1_000_000f) else 0f
        } catch (e: Exception) {
            0f
        }
    }

    suspend fun measureUploadSpeed(): Float = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://speedtest.tele2.net/upload.php")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 10000

            val data = ByteArray(1024 * 1024) // 1MB
            val startTime = System.currentTimeMillis()

            conn.outputStream.use { it.write(data) }
            conn.inputStream.bufferedReader().readText()

            val elapsed = (System.currentTimeMillis() - startTime).toFloat() / 1000f
            conn.disconnect()
            if (elapsed > 0) (data.size * 8f / elapsed / 1_000_000f) else 0f
        } catch (e: Exception) {
            0f
        }
    }
}
