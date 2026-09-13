package com.spider.vpn.util

import com.spider.vpn.data.model.Config
import com.spider.vpn.data.model.DnsSettings
import com.spider.vpn.data.model.DnsType
import org.json.JSONArray
import org.json.JSONObject

object XrayConfig {

    fun buildConfig(
        config: Config,
        dnsSettings: DnsSettings,
        socksPort: Int = 10808,
        httpPort: Int = 10809,
        statsPort: Int = 10810
    ): String {
        val dns = buildDns(dnsSettings, config.sni)
        val outbound = buildOutbound(config)
        val routing = buildRouting()

        return JSONObject().apply {
            put("log", JSONObject().apply {
                put("loglevel", "warning")
                put("access", "none")
                put("error", "none")
            })
            put("dns", dns)
            put("inbounds", buildInbounds(socksPort, httpPort, statsPort))
            put("outbounds", JSONArray().put(outbound))
            put("routing", routing)
            put("stats", JSONObject())
            put("policy", JSONObject().apply {
                put("system", JSONObject().apply {
                    put("statsInboundUplink", true)
                    put("statsInboundDownlink", true)
                    put("statsOutboundUplink", true)
                    put("statsOutboundDownlink", true)
                })
            })
        }.toString(2)
    }

    private fun buildDns(dns: DnsSettings, sni: String): JSONObject {
        val servers = when (dns.type) {
            DnsType.GOOGLE -> JSONArray().apply {
                put("8.8.8.8")
                put("8.8.4.4")
            }
            DnsType.CLOUDFLARE -> JSONArray().apply {
                put("1.1.1.1")
                put("1.0.0.1")
            }
            DnsType.CUSTOM -> JSONArray().apply {
                if (dns.primaryDns.isNotEmpty()) put(dns.primaryDns)
                if (dns.secondaryDns.isNotEmpty()) put(dns.secondaryDns)
            }
        }

        return JSONObject().apply {
            put("servers", servers)
            if (sni.isNotEmpty()) {
                put("queryStrategy", "UseIPv4")
            }
        }
    }

    private fun buildInbounds(socksPort: Int, httpPort: Int, statsPort: Int): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("tag", "socks-in")
                put("port", socksPort)
                put("listen", "127.0.0.1")
                put("protocol", "socks")
                put("settings", JSONObject().apply {
                    put("auth", "noauth")
                    put("udp", true)
                })
                put("sniffing", JSONObject().apply {
                    put("enabled", true)
                    put("destOverride", JSONArray().apply {
                        put("http")
                        put("tls")
                        put("quic")
                    })
                })
            })
            put(JSONObject().apply {
                put("tag", "http-in")
                put("port", httpPort)
                put("listen", "127.0.0.1")
                put("protocol", "http")
                put("settings", JSONObject())
            })
        }
    }

    private fun buildOutbound(config: Config): JSONObject {
        val streamSettings = JSONObject().apply {
            put("network", config.network.ifEmpty { "tcp" })

            val securityObj = JSONObject()
            when (config.security) {
                "tls" -> {
                    securityObj.put("serverName", config.sni.ifEmpty { config.server })
                    securityObj.put("allowInsecure", false)
                    securityObj.put("fingerprint", config.fingerprint.ifEmpty { "chrome" })
                    if (config.fragment.isNotEmpty()) {
                        val fragParts = config.fragment.split(",")
                        if (fragParts.size >= 3) {
                            securityObj.put("fragment", JSONObject().apply {
                                put("packets", fragParts[0].ifEmpty { "tlshello" })
                                put("length", fragParts[1].ifEmpty { "100-200" })
                                put("interval", fragParts[2].ifEmpty { "10-20" })
                            })
                        }
                    }
                }
                "reality" -> {
                    securityObj.put("serverName", config.sni)
                    securityObj.put("fingerprint", config.fingerprint.ifEmpty { "chrome" })
                }
            }
            if (securityObj.length() > 0) put("securitySettings", securityObj)
        }

        val settings = JSONObject().apply {
            put("vnext", JSONArray().apply {
                put(JSONObject().apply {
                    put("address", config.server)
                    put("port", config.port)
                    put("users", JSONArray().apply {
                        put(JSONObject().apply {
                            put("id", config.uuid)
                            put("encryption", "none")
                        })
                    })
                })
            })
        }

        return JSONObject().apply {
            put("tag", "proxy")
            put("protocol", config.protocol.ifEmpty { "vless" })
            put("settings", settings)
            put("streamSettings", streamSettings)
        }
    }

    private fun buildRouting(): JSONObject {
        return JSONObject().apply {
            put("domainStrategy", "AsIs")
            put("rules", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "field")
                    put("inboundTag", JSONArray().apply { put("api") })
                    put("outboundTag", "api")
                })
            })
        }
    }
}
