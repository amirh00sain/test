package com.spider.vpn.ui.panel

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spider.vpn.ui.theme.BgDark
import com.spider.vpn.ui.theme.SpiderVPNTheme
import com.spider.vpn.ui.theme.SpiderRed

class WebViewActivity : ComponentActivity() {
    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_API_KEY = "extra_api_key"

        fun start(context: android.content.Context, domain: String, apiKey: String) {
            val intent = android.content.Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, if (domain.startsWith("http")) domain else "https://$domain")
                putExtra(EXTRA_API_KEY, apiKey)
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_URL) ?: "https://example.com"
        val apiKey = intent.getStringExtra(EXTRA_API_KEY) ?: ""

        setContent {
            SpiderVPNTheme {
                var isLoading by remember { mutableStateOf(true) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgDark)
                ) {
                    android.webkit.WebView(
                        this@WebViewActivity
                    ).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.allowFileAccess = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }

                        if (apiKey.isNotBlank()) {
                            evaluateJavascript(
                                "localStorage.setItem('api_token', '$apiKey');",
                                null
                            )
                        }
                        loadUrl(url)
                    }.also { webView ->
                        androidx.compose.ui.viewinterop.AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { webView }
                        )
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = SpiderRed
                        )
                    }
                }
            }
        }
    }
}
