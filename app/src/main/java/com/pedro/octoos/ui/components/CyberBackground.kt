package com.pedro.octoos.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoLime

/** Fundo com grade em deslocamento lento e uma faixa de varredura. Com animated=false fica estático. */
@Composable
fun CyberBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "octo-bg")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "octo-bg-drift"
    )
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "octo-bg-sweep"
    )
    val gridColor = OctoLime.copy(alpha = 0.07f)
    val sweepColor = OctoLime.copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .background(OctoBackground)
            .drawBehind {
                val step = 44.dp.toPx()
                val shift = if (animated) drift * step else 0f
                var x = shift - step
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += step
                }
                var y = shift - step
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
                if (animated) {
                    val band = 90.dp.toPx()
                    val center = sweep * (size.height + band * 2) - band
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, sweepColor, Color.Transparent),
                            startY = center - band,
                            endY = center + band
                        )
                    )
                }
            }
    ) {
        content()
    }
}
