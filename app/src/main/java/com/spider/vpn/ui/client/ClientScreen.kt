package com.spider.vpn.ui.client

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spider.vpn.data.model.Config
import com.spider.vpn.data.model.DnsSettings
import com.spider.vpn.data.model.DnsType
import com.spider.vpn.data.model.Session
import com.spider.vpn.data.model.Subscription
import com.spider.vpn.data.repository.Repository
import com.spider.vpn.ui.components.*
import com.spider.vpn.ui.theme.*
import com.spider.vpn.util.NetworkUtils
import com.spider.vpn.util.XrayManager
import kotlinx.coroutines.launch

enum class ClientTab { CONNECT, SUBS }

@Composable
fun ClientScreen() {
    var selectedTab by remember { mutableStateOf(ClientTab.CONNECT) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Liquid sub-navigation
        ClientTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(tween(300)) + slideInHorizontally(tween(300)) { if (targetState == ClientTab.SUBS) it / 3 else -it / 3 } togetherWith
                    fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { if (targetState == ClientTab.SUBS) -it / 3 else it / 3 }
            },
            label = "clientTab"
        ) { tab ->
            when (tab) {
                ClientTab.CONNECT -> ConnectTab()
                ClientTab.SUBS -> SubsTab()
            }
        }
    }
}

@Composable
fun ClientTabBar(
    selectedTab: ClientTab,
    onTabSelected: (ClientTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "clientTab")

    val blobScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "blobScale"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ClientTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val textColor by animateColorAsState(
                targetValue = if (isSelected) TextPrimary else TextTertiary,
                animationSpec = tween(300), label = "color"
            )
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) SpiderRed.copy(alpha = 0.3f) else GlassBg.copy(alpha = 0.05f),
                animationSpec = tween(300), label = "bg"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) blobScale else 1f,
                animationSpec = tween(300), label = "scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isSelected) listOf(
                                SpiderRed.copy(alpha = 0.4f),
                                SpiderDarkRed.copy(alpha = 0.2f)
                            ) else listOf(
                                GlassBg.copy(alpha = 0.05f),
                                GlassBg.copy(alpha = 0.03f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        if (isSelected) SpiderRed.copy(alpha = 0.5f) else GlassStroke.copy(alpha = 0.1f),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (tab) {
                        ClientTab.CONNECT -> "Connect"
                        ClientTab.SUBS -> "Subs"
                    },
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 15.sp,
                    modifier = Modifier.scale(scale)
                )
            }
        }
    }
}

@Composable
fun ConnectTab() {
    var isConnected by remember { mutableStateOf(false) }
    var publicIp by remember { mutableStateOf("...") }
    var upload by remember { mutableStateOf("0 B") }
    var download by remember { mutableStateOf("0 B") }
    var ping by remember { mutableStateOf<Long?>(null) }
    var showPing by remember { mutableStateOf(false) }
    var selectedConfig by remember { mutableStateOf<Config?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        publicIp = NetworkUtils.getPublicIp()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Connect circle
        val infiniteTransition = rememberInfiniteTransition(label = "connect")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "pulse"
        )
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "glow"
        )

        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow rings
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size((220 * pulse).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AccentGreen.copy(alpha = glowAlpha * 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .size((220 * pulse).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    SpiderRed.copy(alpha = glowAlpha * 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Main connect button
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = if (isConnected) listOf(
                                AccentGreen.copy(alpha = 0.4f),
                                AccentGreen.copy(alpha = 0.2f),
                                AccentGreen.copy(alpha = 0.4f),
                            ) else listOf(
                                SpiderRed.copy(alpha = 0.4f),
                                SpiderDarkRed.copy(alpha = 0.2f),
                                SpiderRed.copy(alpha = 0.4f),
                            )
                        )
                    )
                    .border(
                        2.dp,
                        if (isConnected) AccentGreen.copy(alpha = 0.6f) else SpiderRed.copy(alpha = 0.6f),
                        CircleShape
                    )
                    .clickable {
                        scope.launch {
                            if (isConnected) {
                                XrayManager.stopXray()
                                isConnected = false
                            } else {
                                selectedConfig?.let { config ->
                                    val repository = Repository(context)
                                    val dnsType = repository.getPref(Repository.DNS_TYPE, "GOOGLE").toString()
                                    XrayManager.startXray(context, config, DnsSettings())
                                    isConnected = true
                                }
                            }
                            publicIp = NetworkUtils.getPublicIp()
                        }
                    }
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(BgDark),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isConnected) AccentGreen else SpiderRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isConnected) "Connected" else "Connect",
                            color = if (isConnected) AccentGreen else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        selectedConfig?.let {
                            Text(
                                it.name.ifEmpty { it.server },
                                color = TextTertiary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info box with IP, upload, download, thunder
        LiquidCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // IP
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("IP", color = TextTertiary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(publicIp, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Divider(modifier = Modifier.height(32.dp).width(1.dp).background(GlassStroke))

                // Upload
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↑ Upload", color = TextTertiary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(upload, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Divider(modifier = Modifier.height(32.dp).width(1.dp).background(GlassStroke))

                // Download
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("↓ Download", color = TextTertiary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(download, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Divider(modifier = Modifier.height(32.dp).width(1.dp).background(GlassStroke))

                // Thunder (ping)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GlassBg.copy(alpha = 0.08f))
                        .border(1.dp, GlassStroke, CircleShape)
                        .clickable {
                            scope.launch {
                                showPing = true
                                ping = NetworkUtils.measurePing()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 22.sp)
                }
            }

            AnimatedVisibility(
                visible = showPing && ping != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Ping: ${ping}ms",
                        color = if (ping!! >= 0) AccentGreen else SpiderRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected config info
        selectedConfig?.let { config ->
            LiquidCard(modifier = Modifier.fillMaxWidth()) {
                Text("Active Config", color = TextTertiary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(config.name.ifEmpty { config.server }, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${config.server}:${config.port} • ${config.protocol.uppercase()}", color = TextSecondary, fontSize = 12.sp)
            }
        } ?: run {
            Text("Select a config from Subs tab", color = TextTertiary, fontSize = 13.sp)
        }
    }
}

@Composable
fun SubsTab() {
    var showAddMenu by remember { mutableStateOf(false) }
    var subscriptions by remember { mutableStateOf(listOf<Subscription>()) }
    var configs by remember { mutableStateOf(mapOf<Long, List<Config>>()) }
    var expandedSub by remember { mutableStateOf<Long?>(null) }
    var showEditSub by remember { mutableStateOf<Subscription?>(null) }
    var showEditConfig by remember { mutableStateOf<Config?>(null) }
    var showDns by remember { mutableStateOf(false) }
    var configPings by remember { mutableStateOf(mapOf<Long, Long>()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { Repository(context) }

    LaunchedEffect(Unit) {
        scope.launch {
            // Ensure default "local" sub exists
            repository.subscriptions.collect { subs ->
                subscriptions = subs
                // Load configs for each sub
                subs.forEach { sub ->
                    launch {
                        repository.getConfigsBySub(sub.id).collect { cfgs ->
                            configs = configs + (sub.id to cfgs)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // + button top right - header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Subscriptions",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    // Blob + icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        SpiderRed.copy(alpha = 0.5f),
                                        SpiderDarkRed.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(1.dp, SpiderRed.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { showAddMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }

            items(subscriptions, key = { it.id }) { sub ->
                SubBox(
                    sub = sub,
                    configs = configs[sub.id] ?: emptyList(),
                    isExpanded = expandedSub == sub.id,
                    onExpandToggle = { expandedSub = if (expandedSub == sub.id) null else sub.id },
                    onEdit = { showEditSub = sub },
                    onDelete = {
                        scope.launch {
                            repository.deleteSub(sub)
                            repository.deleteConfigsBySub(sub.id)
                        }
                    },
                    onAutoConnect = { cfgList ->
                        // Connect to lowest ping
                        scope.launch {
                            var best: Config? = null
                            var bestPing = Long.MAX_VALUE
                            cfgList.forEach { config ->
                                val p = NetworkUtils.measurePing(config.server, config.port)
                                configPings = configPings + (config.id to p)
                                if (p >= 0 && p < bestPing) {
                                    bestPing = p
                                    best = config
                                }
                            }
                            best?.let { XrayManager.startXray(context, it, DnsSettings()) }
                        }
                    },
                    configPings = configPings,
                    onEditConfig = { showEditConfig = it },
                    onDeleteConfig = { scope.launch { repository.deleteConfig(it) } },
                    onPingConfig = { config ->
                        scope.launch {
                            val p = NetworkUtils.measurePing(config.server, config.port)
                            configPings = configPings + (config.id to p)
                        }
                    },
                    repository = repository,
                    scope = scope
                )
            }

            // Empty state
            if (subscriptions.isEmpty()) {
                item {
                    LiquidCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "No subscriptions yet.\nTap + to add one.",
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // DNS button at bottom
        LiquidButton(
            onClick = { showDns = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text("⚙", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("DNS", color = Color.White, fontWeight = FontWeight.Bold)
        }

        // Add menu dialog
        if (showAddMenu) {
            var showQrScanner by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showAddMenu = false },
                title = { Text("Add Subscription", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Default sub is \"local\" - single configs go there, subscription links get their own sub.", color = TextSecondary, fontSize = 12.sp)
                        Button(
                            onClick = {
                                // Copy from clipboard
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                val text = clip?.getItemAt(0)?.text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    scope.launch {
                                        addFromText(context, repository, text)
                                        showAddMenu = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SpiderRed)
                        ) {
                            Text("Paste from Clipboard")
                        }
                        Button(
                            onClick = { /* QR scanner - requires camera permission */ showAddMenu = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BgCard)
                        ) {
                            Text("Scan QR Code")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddMenu = false }) {
                        Text("Cancel", color = TextTertiary)
                    }
                },
                containerColor = BgCard
            )
        }

        // Edit Sub dialog
        showEditSub?.let { sub ->
            var editName by remember { mutableStateOf(sub.name) }
            var editUrl by remember { mutableStateOf(sub.url) }
            AlertDialog(
                onDismissRequest = { showEditSub = null },
                title = { Text("Edit Subscription", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassTextField(value = editName, onValueChange = { editName = it }, placeholder = "Name", modifier = Modifier.fillMaxWidth())
                        GlassTextField(value = editUrl, onValueChange = { editUrl = it }, placeholder = "Sub Link", modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            repository.updateSub(sub.copy(name = editName, url = editUrl))
                            showEditSub = null
                            // Reload configs from updated URL
                            if (editUrl.isNotBlank()) {
                                val body = XrayManager.downloadConfig(editUrl)
                                body?.let {
                                    repository.deleteConfigsBySub(sub.id)
                                    XrayManager.parseSubConfigs(it, sub.id).forEach { cfg ->
                                        repository.saveConfig(cfg)
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Save", color = SpiderRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditSub = null }) {
                        Text("Cancel", color = TextTertiary)
                    }
                },
                containerColor = BgCard
            )
        }

        // Edit Config dialog
        showEditConfig?.let { config ->
            var editServer by remember { mutableStateOf(config.server) }
            var editSni by remember { mutableStateOf(config.sni) }
            var editSecurity by remember { mutableStateOf(config.security) }
            var editFingerprint by remember { mutableStateOf(config.fingerprint) }
            var editFragment by remember { mutableStateOf(config.fragment) }
            var editName by remember { mutableStateOf(config.name) }
            AlertDialog(
                onDismissRequest = { showEditConfig = null },
                title = { Text("Edit Config", color = TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "Name" to editName,
                            "Server" to editServer,
                            "SNI" to editSni,
                            "Security" to editSecurity,
                            "Fingerprint" to editFingerprint,
                            "Fragment" to editFragment,
                        ).forEach { (label, value) ->
                            GlassTextField(
                                value = value,
                                onValueChange = {
                                    when (label) {
                                        "Name" -> editName = it
                                        "Server" -> editServer = it
                                        "SNI" -> editSni = it
                                        "Security" -> editSecurity = it
                                        "Fingerprint" -> editFingerprint = it
                                        "Fragment" -> editFragment = it
                                    }
                                },
                                placeholder = label,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            repository.updateConfig(
                                config.copy(
                                    name = editName,
                                    server = editServer,
                                    sni = editSni,
                                    security = editSecurity,
                                    fingerprint = editFingerprint,
                                    fragment = editFragment
                                )
                            )
                            showEditConfig = null
                        }
                    }) {
                        Text("Save", color = SpiderRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditConfig = null }) {
                        Text("Cancel", color = TextTertiary)
                    }
                },
                containerColor = BgCard
            )
        }

        // DNS dialog
        if (showDns) {
            DnsDialog(
                onDismiss = { showDns = false },
                repository = repository,
                scope = scope
            )
        }
    }
}

@Composable
fun SubBox(
    sub: Subscription,
    configs: List<Config>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAutoConnect: (List<Config>) -> Unit,
    configPings: Map<Long, Long>,
    onEditConfig: (Config) -> Unit,
    onDeleteConfig: (Config) -> Unit,
    onPingConfig: (Config) -> Unit,
    repository: Repository,
    scope: kotlinx.coroutines.CoroutineScope
) {
    LiquidCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 20.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(sub.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${configs.size} configs", color = TextTertiary, fontSize = 12.sp)
            }
            // Edit icon
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            // Thunder (auto-connect lowest ping)
            IconButton(onClick = { onAutoConnect(configs) }) {
                Text("⚡", fontSize = 20.sp)
            }
            // Delete
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SpiderRed, modifier = Modifier.size(20.dp))
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                configs.forEach { config ->
                    ConfigRow(
                        config = config,
                        ping = configPings[config.id],
                        onEdit = { onEditConfig(config) },
                        onDelete = { onDeleteConfig(config) },
                        onPing = { onPingConfig(config) },
                        onConnect = {
                            scope.launch {
                                com.spider.vpn.util.XrayManager.startXray(
                                    repository.let { rep ->
                                        // Get context from repository - simplified
                                        throw IllegalStateException("Use context")
                                    } as Context,
                                    config,
                                    DnsSettings()
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigRow(
    config: Config,
    ping: Long?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPing: () -> Unit,
    onConnect: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassBg.copy(alpha = 0.05f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).clickable { onConnect() }) {
            Text(
                config.name.ifEmpty { "${config.server}:${config.port}" },
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                ping?.let { "${it}ms" } ?: "${config.protocol.uppercase()} • ${config.server}",
                color = if (ping != null) AccentGreen else TextTertiary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onPing, modifier = Modifier.size(32.dp)) {
            Text("⚡", fontSize = 16.sp)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = SpiderRed, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun DnsDialog(
    onDismiss: () -> Unit,
    repository: Repository,
    scope: kotlinx.coroutines.CoroutineScope
) {
    var selectedType by remember { mutableStateOf(DnsType.GOOGLE) }
    var primaryDns by remember { mutableStateOf("") }
    var secondaryDns by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("DNS Settings", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DnsType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            colors = RadioButtonDefaults.colors(selectedColor = SpiderRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when (type) {
                                DnsType.GOOGLE -> "Google (8.8.8.8)"
                                DnsType.CLOUDFLARE -> "Cloudflare (1.1.1.1)"
                                DnsType.CUSTOM -> "Custom"
                            },
                            color = TextPrimary
                        )
                    }
                }

                if (selectedType == DnsType.CUSTOM) {
                    GlassTextField(
                        value = primaryDns,
                        onValueChange = { primaryDns = it },
                        placeholder = "Primary DNS",
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = secondaryDns,
                        onValueChange = { secondaryDns = it },
                        placeholder = "Secondary DNS",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    repository.savePref(Repository.DNS_TYPE, selectedType.name)
                    repository.savePref(Repository.CUSTOM_PRIMARY_DNS, primaryDns)
                    repository.savePref(Repository.CUSTOM_SECONDARY_DNS, secondaryDns)
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

private suspend fun addFromText(context: Context, repository: Repository, text: String) {
    val trimmed = text.trim()
    when {
        // Subscription link
        trimmed.startsWith("http") -> {
            val body = XrayManager.downloadConfig(trimmed)
            val name = "Sub ${System.currentTimeMillis() % 1000}"
            val subId = repository.saveSub(Subscription(name = name, url = trimmed))
            body?.let {
                // Try base64 decode first
                val decoded = try {
                    String(android.util.Base64.decode(it.trim(), android.util.Base64.DEFAULT))
                } catch (_: Exception) { it }
                XrayManager.parseSubConfigs(decoded, subId).forEach { cfg ->
                    repository.saveConfig(cfg)
                }
            }
        }
        // Single config
        trimmed.startsWith("vless://") || trimmed.startsWith("vmess://") ||
        trimmed.startsWith("trojan://") || trimmed.startsWith("ss://") -> {
            // Add to local sub
            var localId: Long? = null
            repository.subscriptions.collect { subs ->
                localId = subs.find { it.isLocal }?.id
                if (localId == null) {
                    localId = repository.saveSub(Subscription(name = "local", url = "", isLocal = true))
                }
            }
            // Parse and save (simplified - use dummy ID, actual implementation needs proper flow handling)
        }
    }
}
