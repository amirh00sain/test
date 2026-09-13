package com.spider.vpn.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.spider.vpn.ui.theme.*
import kotlin.math.sin

@Composable
fun LiquidCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        GlassBg.copy(alpha = 0.12f + shimmer * 0.06f),
                        GlassBgLight.copy(alpha = 0.08f + shimmer * 0.04f),
                        GlassBg.copy(alpha = 0.10f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GlassStroke.copy(alpha = 0.3f + shimmer * 0.15f),
                        GlassHighlight.copy(alpha = 0.1f),
                        GlassStroke.copy(alpha = 0.2f),
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun LiquidBlob(
    modifier: Modifier = Modifier,
    color: Color = SpiderRed,
    animating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blob")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2832f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "phase"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    val blobBrush = remember(phase) {
        Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.8f),
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.1f),
            ),
            center = Offset(
                50f + sin(phase) * 20f,
                50f + sin(phase * 1.3f) * 20f
            ),
            radius = 120f
        )
    }

    Box(
        modifier = modifier
            .scale(if (animating) scale else 1f)
            .clip(CircleShape)
            .background(blobBrush)
    )
}

@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = SpiderRed,
    content: @Composable RowScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "btn")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = 0.7f),
                        color.copy(alpha = 0.9f),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = color.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
fun LiquidTabIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tab")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    if (isSelected) {
        Box(
            modifier = modifier
                .size(6.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(SpiderRed)
        )
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    androidx.compose.material3.TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GlassBg.copy(alpha = 0.08f)),
        placeholder = {
            androidx.compose.material3.Text(
                text = placeholder,
                color = TextTertiary
            )
        },
        colors = androidx.compose.material3.TextFieldDefaults.colors(
            focusedContainerColor = GlassBg.copy(alpha = 0.12f),
            unfocusedContainerColor = GlassBg.copy(alpha = 0.06f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = SpiderRed,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
        singleLine = singleLine,
        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary)
    )
}

@androidx.compose.material3.OptIn(annotation = androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GlassDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = androidx.compose.ui.Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(BgCard.copy(alpha = 0.95f))
            .padding(24.dp),
        content = content
    )
}
