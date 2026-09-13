package com.spider.vpn.util

import android.content.Context
import com.spider.vpn.data.model.Config
import com.spider.vpn.data.model.DnsSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object XrayManager {
    private var process: Process? = null
    private var isRunning = false

    val isConnected: Boolean get() = isRunning

    suspend fun startXray(
        context: Context,
        config: Config,
        dnsSettings: DnsSettings
    ) = withContext(Dispatchers.IO) {
        try {
            stopXray()

            val configJson = XrayConfig.buildConfig(config, dnsSettings)
            val configFile = File(context.filesDir, "xray_config.json")
            configFile.writeText(configJson)

            val xrayBin = File(context.filesDir, "xray")
            if (!xrayBin.exists()) {
                copyAsset(context, "xray", xrayBin)
                xrayBin.setExecutable(true)
            }

            val pb = ProcessBuilder(
                xrayBin.absolutePath,
                "run",
                "-c", configFile.absolutePath
            )
            pb.directory(context.filesDir)
            pb.redirectErrorStream(true)

            process = pb.start()
            isRunning = true

            // Read output in background
            try {
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                while (isRunning) {
                    val line = reader.readLine() ?: break
                    // Log xray output
                }
            } catch (_: Exception) {}
        } catch (e: Exception) {
            isRunning = false
            e.printStackTrace()
        }
    }

    fun stopXray() {
        isRunning = false
        try {
            process?.destroy()
            process?.waitFor()
        } catch (_: Exception) {}
        process = null

        // Kill any remaining xray processes
        try {
            Runtime.getRuntime().exec(arrayOf("pkill", "-f", "xray")).waitFor()
        } catch (_: Exception) {}
    }

    private fun copyAsset(context: Context, assetName: String, dest: File) {
        context.assets.open(assetName).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
    }

    suspend fun downloadConfig(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.connect()
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()
            body
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseSubConfigs(raw: String, subId: Long): List<Config> {
        val configs = mutableListOf<Config>()
        val lines = raw.lines().filter { it.isNotBlank() }

        for (line in lines) {
            val config = when {
                line.startsWith("vless://") -> parseVless(line, subId)
                line.startsWith("vmess://") -> parseVmess(line, subId)
                line.startsWith("trojan://") -> parseTrojan(line, subId)
                line.startsWith("ss://") -> parseShadowsocks(line, subId)
                else -> null
            }
            config?.let { configs.add(it) }
        }
        return configs
    }

    private fun parseVless(line: String, subId: Long): Config? {
        return try {
            val withoutProtocol = line.removePrefix("vless://")
            val hashIndex = withoutProtocol.indexOf('#')
            val remark = if (hashIndex >= 0) {
                java.net.URLDecoder.decode(withoutProtocol.substring(hashIndex + 1), "UTF-8")
            } else ""

            val mainPart = if (hashIndex >= 0) withoutProtocol.substring(0, hashIndex) else withoutProtocol
            val atIndex = mainPart.indexOf('@')
            val uuid = mainPart.substring(0, atIndex)
            val serverPart = mainPart.substring(atIndex + 1)

            val questionIndex = serverPart.indexOf('?')
            val serverPort = if (questionIndex >= 0) serverPart.substring(0, questionIndex) else serverPart
            val params = if (questionIndex >= 0) parseParams(serverPart.substring(questionIndex + 1)) else emptyMap()

            val dotIndex = serverPort.lastIndexOf(':')
            val server = serverPort.substring(0, dotIndex)
            val port = serverPort.substring(dotIndex + 1).toIntOrNull() ?: 443

            Config(
                subId = subId,
                name = remark,
                server = server,
                port = port,
                protocol = "vless",
                uuid = uuid,
                sni = params["sni"] ?: params["host"] ?: "",
                security = params["security"] ?: "tls",
                fingerprint = params["fp"] ?: "chrome",
                fragment = params["frag"] ?: "",
                network = params["type"] ?: "tcp",
                remark = remark,
                rawConfig = line
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseVmess(line: String, subId: Long): Config? {
        return try {
            val encoded = line.removePrefix("vmess://")
            val decoded = String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT))
            val json = org.json.JSONObject(decoded)
            Config(
                subId = subId,
                name = json.optString("ps", ""),
                server = json.optString("add", ""),
                port = json.optInt("port", 443),
                protocol = "vmess",
                uuid = json.optString("id", ""),
                sni = json.optString("sni", ""),
                security = json.optString("tls", ""),
                fingerprint = json.optString("fp", "chrome"),
                network = json.optString("net", "tcp"),
                remark = json.optString("ps", ""),
                rawConfig = line
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseTrojan(line: String, subId: Long): Config? {
        return try {
            val withoutProtocol = line.removePrefix("trojan://")
            val hashIndex = withoutProtocol.indexOf('#')
            val remark = if (hashIndex >= 0) {
                java.net.URLDecoder.decode(withoutProtocol.substring(hashIndex + 1), "UTF-8")
            } else ""

            val mainPart = if (hashIndex >= 0) withoutProtocol.substring(0, hashIndex) else withoutProtocol
            val atIndex = mainPart.indexOf('@')
            val password = mainPart.substring(0, atIndex)
            val serverPart = mainPart.substring(atIndex + 1)

            val questionIndex = serverPart.indexOf('?')
            val serverPort = if (questionIndex >= 0) serverPart.substring(0, questionIndex) else serverPart
            val params = if (questionIndex >= 0) parseParams(serverPart.substring(questionIndex + 1)) else emptyMap()

            val dotIndex = serverPort.lastIndexOf(':')
            val server = serverPort.substring(0, dotIndex)
            val port = serverPort.substring(dotIndex + 1).toIntOrNull() ?: 443

            Config(
                subId = subId,
                name = remark,
                server = server,
                port = port,
                protocol = "trojan",
                uuid = password,
                sni = params["sni"] ?: params["host"] ?: "",
                security = params["security"] ?: "tls",
                fingerprint = params["fp"] ?: "chrome",
                network = params["type"] ?: "tcp",
                remark = remark,
                rawConfig = line
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseShadowsocks(line: String, subId: Long): Config? {
        return try {
            val withoutProtocol = line.removePrefix("ss://")
            val hashIndex = withoutProtocol.indexOf('#')
            val remark = if (hashIndex >= 0) {
                java.net.URLDecoder.decode(withoutProtocol.substring(hashIndex + 1), "UTF-8")
            } else ""

            val mainPart = if (hashIndex >= 0) withoutProtocol.substring(0, hashIndex) else withoutProtocol
            val atIndex = mainPart.indexOf('@')
            val serverPart = mainPart.substring(atIndex + 1)
            val dotIndex = serverPart.lastIndexOf(':')
            val server = serverPart.substring(0, dotIndex)
            val port = serverPart.substring(dotIndex + 1).toIntOrNull() ?: 443

            Config(
                subId = subId,
                name = remark,
                server = server,
                port = port,
                protocol = "shadowsocks",
                remark = remark,
                rawConfig = line
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseParams(query: String): Map<String, String> {
        return query.split("&").associate {
            val (key, value) = it.split("=", limit = 2)
            key to java.net.URLDecoder.decode(value, "UTF-8")
        }
    }
}
