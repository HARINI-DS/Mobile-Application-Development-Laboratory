package com.harini.yours.ui1.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harini.yours.ui.theme.*

@Composable
fun MessageBubble(text: String, isUser: Boolean) {

    // Fade-in on first composition
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280),
        label = "bubble_alpha"
    )
    LaunchedEffect(Unit) { visible = true }

    val userShape = RoundedCornerShape(
        topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp
    )
    val botShape = RoundedCornerShape(
        topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {

        if (!isUser) {
            // Bot avatar dot
            Box(
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 4.dp)
                    .size(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Crimson, CrimsonDark),
                            center = Offset(14f, 14f),
                            radius = 28f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Y",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(if (isUser) userShape else botShape)
                .then(
                    if (isUser) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(CrimsonBright, Crimson),
                                start = Offset(0f, 0f),
                                end = Offset(300f, 80f)
                            )
                        )
                    } else {
                        Modifier.background(Black500)
                    }
                )
                .then(
                    if (isUser) Modifier.drawBehind {
                        // subtle glow under user bubble
                        drawCircle(
                            color = CrimsonGlow,
                            radius = size.width * 0.6f,
                            center = Offset(size.width / 2, size.height)
                        )
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 11.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) TextOnCrimson else TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }
    }
}