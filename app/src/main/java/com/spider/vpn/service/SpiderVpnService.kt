package com.spider.vpn.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.spider.vpn.R
import com.spider.vpn.data.model.Config
import com.spider.vpn.data.model.DnsSettings
import com.spider.vpn.ui.MainActivity
import com.spider.vpn.util.Constants
import com.spider.vpn.util.XrayManager
import kotlinx.coroutines.*

class SpiderVpnService : VpnService() {

    companion object {
        private const val ACTION_START = "com.spider.vpn.START"
        private const val ACTION_STOP = "com.spider.vpn.STOP"

        fun start(context: Context, config: Config, dnsSettings: DnsSettings) {
            val intent = Intent(context, SpiderVpnService::class.java).apply {
                action = ACTION_START
                putExtra("server", config.server)
                putExtra("port", config.port)
                putExtra("uuid", config.uuid)
                putExtra("sni", config.sni)
                putExtra("security", config.security)
                putExtra("protocol", config.protocol)
                putExtra("dns_type", dnsSettings.type.name)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SpiderVpnService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        startForeground(Constants.NOTIFICATION_ID, buildNotification("Starting VPN..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val server = intent.getStringExtra("server") ?: ""
                val port = intent.getIntExtra("port", 443)
                val uuid = intent.getStringExtra("uuid") ?: ""
                val sni = intent.getStringExtra("sni") ?: ""
                val security = intent.getStringExtra("security") ?: "tls"
                val protocol = intent.getStringExtra("protocol") ?: "vless"
                val dnsType = intent.getStringExtra("dns_type") ?: "GOOGLE"

                val config = Config(
                    server = server,
                    port = port,
                    uuid = uuid,
                    sni = sni,
                    security = security,
                    protocol = protocol
                )
                val dns = DnsSettings(
                    type = com.spider.vpn.data.model.DnsType.valueOf(dnsType)
                )

                serviceScope.launch {
                    startVpn(config, dns)
                }
            }
            ACTION_STOP -> {
                stopVpn()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private suspend fun startVpn(config: Config, dnsSettings: DnsSettings) {
        try {
            // Build VPN interface
            val builder = Builder()
                .setSession("Spider VPN")
                .setMtu(Constants.VPN_MTU)
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("8.8.4.4")

            vpnInterface = builder.establish()

            updateNotification("Connected to ${config.server}")

            // Start xray core
            XrayManager.startXray(this, config, dnsSettings)
        } catch (e: Exception) {
            updateNotification("Connection failed: ${e.message}")
            stopVpn()
        }
    }

    private fun stopVpn() {
        XrayManager.stopXray()
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
        updateNotification("Disconnected")
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, Constants.NOTIFICATION_CHANNEL)
            .setContentTitle("Spider VPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = buildNotification(text)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(Constants.NOTIFICATION_ID, notification)
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }
}
