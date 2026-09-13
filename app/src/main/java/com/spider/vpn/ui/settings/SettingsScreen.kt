package com.spider.vpn.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spider.vpn.data.repository.Repository
import com.spider.vpn.ui.components.*
import com.spider.vpn.ui.theme.*
import com.spider.vpn.util.Constants
import com.spider.vpn.util.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { Repository(context) }
    var showAiSetup by remember { mutableStateOf(false) }
    var showSupport by remember { mutableStateOf(false) }
    var showUpdating by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Speed Test
        SpeedTestButton()

        Spacer(modifier = Modifier.height(8.dp))

        // Delete buttons section
        Text("Data Management", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

        val deleteButtons = listOf(
            "D Railway" to "Railway token",
            "D Spider" to "Spider token",
            "D AI" to "AI setup",
            "D All" to "All subs & configs"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            deleteButtons.forEach { (label, desc) ->
                LiquidButton(
                    onClick = {
                        when (label) {
                            "D Railway" -> scope.launch { repository.savePref(Repository.RAILWAY_TOKEN, "") }
                            "D Spider" -> scope.launch { repository.savePref(Repository.SPIDER_TOKEN, "") }
                            "D AI" -> scope.launch {
                                repository.savePref(Repository.AI_API_URL, "")
                                repository.savePref(Repository.AI_API_KEY, "")
                                repository.savePref(Repository.AI_MODEL, "")
                            }
                            "D All" -> scope.launch { repository.clearAllData() }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    color = SpiderDarkRed
                ) {
                    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Swipe to see more", color = TextTertiary, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        // AI Setup
        LiquidButton(
            onClick = { showAiSetup = true },
            modifier = Modifier.fillMaxWidth(),
            color = AccentBlue
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI Setup", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Update button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            SpiderRed.copy(alpha = 0.5f),
                            SpiderRed.copy(alpha = 0.7f),
                        )
                    )
                )
                .border(1.dp, SpiderRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .clickable {
                    showUpdating = true
                    scope.launch {
                        // Simulate download with water animation progress
                        for (i in 0..100) {
                            updateProgress = i
                            delay(50)
                        }
                        showUpdating = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (showUpdating) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Water fill effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(GlassBg.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = updateProgress / 100f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            AccentBlue.copy(alpha = 0.6f),
                                            AccentBlue.copy(alpha = 1f),
                                        )
                                    )
                                )
                        )
                    }
                    Text("Downloading... $updateProgress%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("↓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Support button
        LiquidButton(
            onClick = { showSupport = true },
            modifier = Modifier.fillMaxWidth(),
            color = AccentGreen
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Support", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Setup Dialog
        if (showAiSetup) {
            AiSetupDialog(
                repository = repository,
                scope = scope,
                onDismiss = { showAiSetup = false }
            )
        }

        // Support Dialog
        if (showSupport) {
            SupportDialog(
                context = context,
                onDismiss = { showSupport = false }
            )
        }
    }
}

@Composable
fun SpeedTestButton() {
    var isRunning by remember { mutableStateOf(false) }
    var downloadSpeed by remember { mutableStateOf(0f) }
    var uploadSpeed by remember { mutableStateOf(0f) }
    var ping by remember { mutableStateOf(0L) }
    var showResults by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Animated progress ring
    val infiniteTransition = rememberInfiniteTransition(label = "speedRing")
    val ringProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "ring"
    )

    LiquidCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Speed Test",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Speed test circle
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = if (isRunning) listOf(
                                    AccentBlue.copy(alpha = 0.0f),
                                    AccentBlue.copy(alpha = ringProgress * 0.8f),
                                    AccentBlue.copy(alpha = ringProgress),
                                    AccentBlue.copy(alpha = ringProgress * 0.5f),
                                    AccentBlue.copy(alpha = 0.0f),
                                ) else listOf(
                                    GlassBg.copy(alpha = 0.1f),
                                    GlassBg.copy(alpha = 0.05f),
                                )
                            )
                        )
                )
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(BgDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = AccentBlue,
                            strokeWidth = 4.dp
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚡", fontSize = 28.sp)
                            Text("Run", color = TextTertiary, fontSize = 12.sp)
                        }
                    }
                }

                // Clickable overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable(enabled = !isRunning) {
                            isRunning = true
                            showResults = false
                            scope.launch {
                                downloadSpeed = NetworkUtils.measureDownloadSpeed()
                                uploadSpeed = NetworkUtils.measureUploadSpeed()
                                ping = NetworkUtils.measurePing()
                                isRunning = false
                                showResults = true
                            }
                        }
                )
            }

            AnimatedVisibility(visible = showResults) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedResult(label = "↓ DL", value = "%.1f Mbps".format(downloadSpeed))
                    SpeedResult(label = "↑ UL", value = "%.1f Mbps".format(uploadSpeed))
                    SpeedResult(label = "Ping", value = "${ping}ms")
                }
            }
        }
    }
}

@Composable
fun SpeedResult(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextTertiary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AiSetupDialog(
    repository: Repository,
    scope: kotlinx.coroutines.CoroutineScope,
    onDismiss: () -> Unit
) {
    var apiUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        repository.getPref(Repository.AI_API_URL, "").collect { apiUrl = it }
        repository.getPref(Repository.AI_API_KEY, "").collect { apiKey = it }
        repository.getPref(Repository.AI_MODEL, "").collect { model = it }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AI Setup", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                GlassTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "API URL"
                )
                GlassTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "API Key"
                )
                GlassTextField(
                    value = model,
                    onValueChange = { model = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "AI Model Name"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    repository.savePref(Repository.AI_API_URL, apiUrl)
                    repository.savePref(Repository.AI_API_KEY, apiKey)
                    repository.savePref(Repository.AI_MODEL, model)
                    onDismiss()
                }
            }) {
                Text("Save", color = SpiderRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextTertiary)
            }
        },
        containerColor = BgCard
    )
}

@Composable
fun SupportDialog(
    context: android.content.Context,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Support", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SupportButton("Telegram", "🔵") {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TELEGRAM_URL)))
                }
                SupportButton("GitHub", "⚫") {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GITHUB_URL)))
                }
                SupportButton("YouTube", "🔴") {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.YOUTUBE_URL)))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextTertiary)
            }
        },
        containerColor = BgCard
    )
}

@Composable
fun SupportButton(name: String, icon: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg.copy(alpha = 0.08f))
            .border(1.dp, GlassStroke.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Text(name, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
