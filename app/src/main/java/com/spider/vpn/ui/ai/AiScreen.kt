package com.spider.vpn.ui.ai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spider.vpn.data.model.ChatMessage
import com.spider.vpn.data.repository.Repository
import com.spider.vpn.ui.components.*
import com.spider.vpn.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { Repository(context) }
    val chatMessages = repository.chatMessages

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var showFilePicker by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showVideoPicker by remember { mutableStateOf(false) }
    var showVoiceRecorder by remember { mutableStateOf(false) }
    var modelName by remember { mutableStateOf("gpt-4") }
    var filePath by remember { mutableStateOf<String?>(null) }
    var fileType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        repository.chatMessages.collect { messages = it }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat messages list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(message = msg)
            }
        }

        // AI Model selector
        LiquidCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Model:", color = TextSecondary, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("gpt-4", "claude", "llama", "mistral").forEach { name ->
                        Box(
                            modifier = Modifier
                                .clickable { modelName = name }
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (modelName == name) SpiderRed.copy(alpha = 0.3f) else GlassBg.copy(alpha = 0.05f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text(name, color = TextPrimary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Input area
        LiquidCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Media buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { /* Voice */ showVoiceRecorder = true }) {
                        Text("🎤", fontSize = 20.sp)
                    }
                    IconButton(onClick = { /* Video */ showVideoPicker = true }) {
                        Text("🎥", fontSize = 20.sp)
                    }
                    IconButton(onClick = { /* Picture */ showImagePicker = true }) {
                        Text("🖼", fontSize = 20.sp)
                    }
                    IconButton(onClick = { /* File */ showFilePicker = true }) {
                        Text("📎", fontSize = 20.sp)
                    }
                }

                Divider(modifier = Modifier.height(40.dp).width(1.dp).background(GlassStroke))

                // Text input
                ExpandedInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f)
                )

                // Send button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SpiderRed, SpiderLightRed)
                            )
                        )
                        .clickable {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    repository.saveChatMessage(
                                        ChatMessage(role = "user", content = inputText, filePath = filePath, fileType = fileType)
                                    )
                                    // Simulate AI response
                                    inputText = ""
                                    filePath = null
                                    fileType = null
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val bgColor = if (isUser) SpiderRed.copy(alpha = 0.2f) else BgCard.copy(alpha = 0.8f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // File indicator
        message.filePath?.let { path ->
            Text(
                "📄 ${message.fileType ?: "file"}",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            bgColor,
                            if (isUser) SpiderRed.copy(alpha = 0.3f) else BgSurface
                        )
                    )
                )
                .border(
                    1.dp,
                    GlassStroke.copy(alpha = 0.15f),
                    RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                message.content,
                color = TextPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ExpandedInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    GlassTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = if (expanded) "Type message..." else "Send message...",
        singleLine = !expanded
    )
}

// File pickers (stubs - actual implementation needs permissions)
@Composable
fun VoiceRecorderDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Voice Message", color = TextPrimary) },
        text = { Text("Recording feature coming soon", color = TextSecondary) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = TextPrimary) }
        },
        containerColor = BgCard
    )
}

@Composable
fun ImagePickerDialog(
    context: Context,
    onPick: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let { onPick(it) } }
    )

    LaunchedEffect(Unit) {
        launcher.launch("image/*")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Image", color = TextPrimary) },
        text = { Text("Choose an image to share", color = TextSecondary) },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextTertiary) } },
        containerColor = BgCard
    )
}
