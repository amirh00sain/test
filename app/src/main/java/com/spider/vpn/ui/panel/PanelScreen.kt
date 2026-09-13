package com.spider.vpn.ui.panel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.border
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spider.vpn.data.model.Session
import com.spider.vpn.data.repository.Repository
import com.spider.vpn.ui.components.*
import com.spider.vpn.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PanelScreen() {
    var showTokenInput by remember { mutableStateOf(false) }
    var showSessionSetup by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<Session?>(null) }
    var showWebView by remember { mutableStateOf<Session?>(null) }
    var token by remember { mutableStateOf("") }
    var sessions by remember { mutableStateOf(listOf<Session>()) }
    var tokenSaved by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { Repository(context) }

    LaunchedEffect(Unit) {
        repository.sessions.collect { sessions = it }
    }

    // Fade animation for token state
    val contentAlpha by animateFloatAsState(
        targetValue = if (showTokenInput) 0f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "contentAlpha"
    )

    if (showWebView) {
        WebViewActivity.start(context, showWebView!!.domain, showWebView!!.apiKey)
        showWebView = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Token Section
            item {
                AnimatedVisibility(
                    visible = !showTokenInput && !tokenSaved,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)),
                    exit = fadeOut(tween(300)) + slideOutVertically(tween(300))
                ) {
                    LiquidBlob(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        color = SpiderRed,
                        animating = true
                    )
                    LiquidButton(
                        onClick = {
                            // Open Railway link
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://railway.com"))
                            context.startActivity(intent)
                            showTokenInput = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Tokens", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Token Input
            item {
                AnimatedVisibility(
                    visible = showTokenInput && !tokenSaved,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
                    exit = fadeOut(tween(400)) + scaleOut(tween(400)) + slideOutVertically(tween(400)) { it / 3 }
                ) {
                    LiquidCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Enter Token",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassTextField(
                            value = token,
                            onValueChange = { token = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "Paste your Railway token..."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LiquidButton(
                            onClick = {
                                if (token.isNotBlank()) {
                                    scope.launch {
                                        repository.savePref(Repository.RAILWAY_TOKEN, token)
                                        tokenSaved = true
                                        showTokenInput = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = SpiderRed
                        ) {
                            Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Token Saved + Sessions Circle
            item {
                AnimatedVisibility(
                    visible = tokenSaved || sessions.isNotEmpty(),
                    enter = fadeIn(tween(600)),
                    exit = fadeOut(tween(300))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Sessions",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // PlayStation-style session circle
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            SpiderRed.copy(alpha = 0.3f),
                                            SpiderDarkRed.copy(alpha = 0.2f),
                                            SpiderRed.copy(alpha = 0.3f),
                                            SpiderDarkRed.copy(alpha = 0.2f),
                                            SpiderRed.copy(alpha = 0.3f),
                                        )
                                    )
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Inner circle
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BgDark)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sessions.isEmpty()) {
                                    Text(
                                        "No Sessions",
                                        color = TextTertiary,
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // Session items in a circle
                                    sessions.forEachIndexed { index, session ->
                                        val angle = 360f / sessions.size * index - 90f
                                        val radius = 80.dp
                                        val rad = Math.toRadians(angle.toDouble())

                                        Box(
                                            modifier = Modifier
                                                .offset(
                                                    x = (radius * cos(rad).toFloat()),
                                                    y = (radius * sin(rad).toFloat())
                                                )
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selectedSession?.id == session.id) SpiderRed
                                                    else BgCard
                                                )
                                                .border(1.dp, GlassStroke, CircleShape)
                                                .clickable {
                                                    selectedSession = session
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                session.name.take(1).uppercase(),
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Center profile button
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        SpiderRed.copy(alpha = 0.8f),
                                                        SpiderDarkRed.copy(alpha = 0.6f),
                                                    )
                                                )
                                            )
                                            .clickable {
                                                selectedSession?.let { showSessionSetup = true }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Profile",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Session Setup
            item {
                AnimatedVisibility(
                    visible = showSessionSetup && selectedSession != null,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 },
                    exit = fadeOut(tween(300)) + slideOutVertically(tween(300)) { it / 3 }
                ) {
                    selectedSession?.let { session ->
                        var apiKey by remember { mutableStateOf(session.apiKey) }
                        var domain by remember { mutableStateOf(session.domain) }

                        LiquidCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Session Setup",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            GlassTextField(
                                value = apiKey,
                                onValueChange = { apiKey = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "API Key"
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            GlassTextField(
                                value = domain,
                                onValueChange = { domain = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = "Domain (e.g. example.com)"
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LiquidButton(
                                onClick = {
                                    scope.launch {
                                        repository.saveSession(
                                            session.copy(apiKey = apiKey, domain = domain)
                                        )
                                        showSessionSetup = false
                                        if (domain.isNotBlank()) {
                                            showWebView = session.copy(domain = domain, apiKey = apiKey)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save & Open", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
