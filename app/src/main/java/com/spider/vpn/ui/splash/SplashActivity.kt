package com.spider.vpn.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.spider.vpn.ui.MainActivity
import com.spider.vpn.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpiderVPNTheme {
                SplashScreen(onFinished = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Spider logo scale animation - animate once on start
    var logoScaleTarget by remember { mutableFloatStateOf(0.3f) }
    LaunchedEffect(Unit) { logoScaleTarget = 1f }
    val logoScale by animateFloatAsState(
        targetValue = logoScaleTarget,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    // Logo opacity - animate once
    var logoAlphaTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) { logoAlphaTarget = 1f }
    val logoAlpha by animateFloatAsState(
        targetValue = logoAlphaTarget,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )

    // Rotating ring
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "ringRotation"
    )

    // Pulsing glow
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowScale"
    )

    // Blob morphing phase
    val blobPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2832f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "blobPhase"
    )

    // Exit animation
    var exitAnim by remember { mutableStateOf(false) }
    val exitScale by animateFloatAsState(
        targetValue = if (exitAnim) 1.5f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "exitScale"
    )
    val exitAlpha by animateFloatAsState(
        targetValue = if (exitAnim) 0f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "exitAlpha"
    )

    LaunchedEffect(Unit) {
        delay(2500)
        exitAnim = true
        delay(600)
        onFinished()
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .scale(exitScale)
            .alpha(exitAlpha),
        contentAlignment = Alignment.Center
    ) {
        // Background blobs
        Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
            val w = size.width
            val h = size.height

            // Blob 1 - top left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SpiderRed.copy(alpha = 0.3f),
                        SpiderRed.copy(alpha = 0f)
                    ),
                    center = Offset(
                        w * 0.3f + sin(blobPhase) * 50f,
                        h * 0.25f + cos(blobPhase * 0.7f) * 30f
                    ),
                    radius = 300f
                ),
                center = Offset(w * 0.3f, h * 0.25f),
                radius = 300f
            )

            // Blob 2 - bottom right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SpiderDarkRed.copy(alpha = 0.25f),
                        SpiderDarkRed.copy(alpha = 0f)
                    ),
                    center = Offset(
                        w * 0.7f + cos(blobPhase * 1.1f) * 40f,
                        h * 0.75f + sin(blobPhase * 0.9f) * 35f
                    ),
                    radius = 250f
                ),
                center = Offset(w * 0.7f, h * 0.75f),
                radius = 250f
            )

            // Blob 3 - center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SpiderLightRed.copy(alpha = 0.15f),
                        SpiderLightRed.copy(alpha = 0f)
                    ),
                    center = Offset(
                        w * 0.5f + sin(blobPhase * 1.3f) * 60f,
                        h * 0.5f + cos(blobPhase) * 40f
                    ),
                    radius = 200f
                ),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = 200f
            )
        }

        // Rotating outer ring
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .scale(glowScale)
        ) {
            rotate(ringRotation, pivot = Offset(size.width / 2, size.height / 2)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            SpiderRed.copy(alpha = 0.0f),
                            SpiderRed.copy(alpha = 0.6f),
                            SpiderLightRed.copy(alpha = 0.8f),
                            SpiderRed.copy(alpha = 0.0f),
                            SpiderDarkRed.copy(alpha = 0.4f),
                            SpiderRed.copy(alpha = 0.0f)
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Second counter-rotating ring
        Canvas(
            modifier = Modifier
                .size(200.dp)
                .scale(glowScale)
        ) {
            rotate(-ringRotation * 0.6f, pivot = Offset(size.width / 2, size.height / 2)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            SpiderDarkRed.copy(alpha = 0.0f),
                            SpiderDarkRed.copy(alpha = 0.4f),
                            SpiderRed.copy(alpha = 0.6f),
                            SpiderDarkRed.copy(alpha = 0.0f),
                        )
                    ),
                    startAngle = 90f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Central glow
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .blur(40.dp)
                .scale(glowScale)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SpiderRed.copy(alpha = 0.5f),
                        SpiderRed.copy(alpha = 0.0f)
                    )
                ),
                radius = size.minDimension / 2
            )
        }

        // Spider logo placeholder (circle with spider web pattern)
        Canvas(
            modifier = Modifier
                .size(120.dp)
                .scale(logoScale)
        ) {
            val cx = size.width / 2
            val cy = size.height / 2
            val r = size.minDimension / 2 - 4.dp.toPx()

            // Outer circle
            drawCircle(
                color = SpiderRed,
                radius = r,
                center = Offset(cx, cy)
            )

            // Inner dark circle
            drawCircle(
                color = BgDark,
                radius = r * 0.85f,
                center = Offset(cx, cy)
            )

            // Spider web lines
            val webColor = SpiderRed.copy(alpha = 0.7f)
            for (i in 0 until 8) {
                val angle = i * 45f
                val rad = Math.toRadians(angle.toDouble())
                drawLine(
                    color = webColor,
                    start = Offset(cx, cy),
                    end = Offset(
                        cx + (r * 0.8f * cos(rad)).toFloat(),
                        cy + (r * 0.8f * sin(rad)).toFloat()
                    ),
                    strokeWidth = 1.5f
                )
            }

            // Concentric arcs
            for (ring in 1..3) {
                val ringR = r * 0.8f * ring / 3.5f
                for (i in 0 until 8) {
                    val startAngle = i * 45f
                    drawArc(
                        color = webColor,
                        startAngle = startAngle,
                        sweepAngle = 40f,
                        useCenter = false,
                        style = Stroke(width = 1f),
                        topLeft = Offset(cx - ringR, cy - ringR),
                        size = androidx.compose.ui.geometry.Size(ringR * 2, ringR * 2)
                    )
                }
            }

            // Center dot
            drawCircle(
                color = SpiderRed,
                radius = r * 0.15f,
                center = Offset(cx, cy)
            )
        }
    }
}
