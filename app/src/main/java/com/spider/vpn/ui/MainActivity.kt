package com.spider.vpn.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spider.vpn.data.repository.Repository
import com.spider.vpn.ui.ai.AiScreen
import com.spider.vpn.ui.client.ClientScreen
import com.spider.vpn.ui.panel.PanelScreen
import com.spider.vpn.ui.settings.SettingsScreen
import com.spider.vpn.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpiderVPNTheme {
                MainScreen()
            }
        }
    }
}

enum class Tab(val label: String, val icon: String) {
    PANEL("Panel", "P"),
    CLIENT("Client", "C"),
    AI("AI", "A"),
    SETTINGS("Settings", "S")
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(Tab.CLIENT) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .statusBarsPadding()
    ) {
        // Top liquid tab bar
        LiquidTabBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Tab content
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) + slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it / 20 }
                ) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "tabContent"
        ) { tab ->
            when (tab) {
                Tab.PANEL -> PanelScreen()
                Tab.CLIENT -> ClientScreen()
                Tab.AI -> AiScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}

@Composable
fun LiquidTabBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tabBar")

    // Animated blob behind selected tab
    var tabPositions by remember { mutableStateOf<Map<Tab, Offset>>(emptyMap()) }
    var tabSizes by remember { mutableStateOf<Map<Tab, Size>>(emptyMap()) }

    val blobPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2832f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "blobPhase"
    )

    val blobScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "blobScale"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        // Glass background
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .blur(0.5.dp)
        ) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassBg.copy(alpha = 0.08f),
                        GlassBgLight.copy(alpha = 0.05f),
                        GlassBg.copy(alpha = 0.08f),
                    )
                ),
                cornerRadius = CornerRadius(24f),
                size = size
            )

            // Subtle border
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassStroke.copy(alpha = 0.15f),
                        GlassHighlight.copy(alpha = 0.05f),
                        GlassStroke.copy(alpha = 0.10f),
                    )
                ),
                cornerRadius = CornerRadius(24f),
                size = size,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }

        // Active tab blob indicator
        tabPositions[selectedTab]?.let { pos ->
            tabSizes[selectedTab]?.let { sz ->
                Canvas(
                    modifier = Modifier
                        .offset(x = pos.x.dp - 10.dp, y = pos.y.dp - 4.dp)
                        .size(
                            width = (sz.width + 20).dp,
                            height = (sz.height + 8).dp
                        )
                        .scale(blobScale)
                ) {
                    // Liquid blob shape
                    val path = Path()
                    val w = size.width
                    val h = size.height
                    val cx = w / 2
                    val cy = h / 2
                    val rx = w / 2
                    val ry = h / 2

                    path.moveTo(cx + rx, cy)
                    for (angle in 0..360 step 5) {
                        val rad = Math.toRadians(angle.toDouble())
                        val wobble = 1f + 0.08f * sin(blobPhase + rad * 3).toFloat()
                        val x = cx + (rx * cos(rad) * wobble).toFloat()
                        val y = cy + (ry * sin(rad) * wobble).toFloat()
                        if (angle == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                SpiderRed.copy(alpha = 0.6f),
                                SpiderLightRed.copy(alpha = 0.4f),
                                SpiderDarkRed.copy(alpha = 0.3f),
                            )
                        )
                    )
                }
            }
        }

        // Tab row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                TabItem(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { onTabSelected(tab) },
                    onPositionMeasured = { offset, size ->
                        tabPositions = tabPositions + (tab to offset)
                        tabSizes = tabSizes + (tab to size)
                    }
                )
            }
        }
    }
}

@Composable
fun TabItem(
    tab: Tab,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPositionMeasured: (Offset, Size) -> Unit
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val textColor by animateColorAsState(
        targetValue = if (isSelected) TextPrimary else TextTertiary,
        animationSpec = tween(300), label = "tabColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing), label = "tabScale"
    )

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val size = coords.size
                onPositionMeasured(
                    Offset(pos.x / density.density, pos.y / density.density),
                    Size(size.width / density.density, size.height / density.density)
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale)
        ) {
            // Tab icon (using text as placeholder for vector icons)
            Text(
                text = when (tab) {
                    Tab.PANEL -> "■"
                    Tab.CLIENT -> "▲"
                    Tab.AI -> "♡"
                    Tab.SETTINGS -> "⚙"
                },
                fontSize = 18.sp,
                color = if (isSelected) SpiderRed else TextTertiary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}
