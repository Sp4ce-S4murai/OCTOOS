package com.pedro.octoos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Custom cyber-brutal shape with a 45-degree chamfer on the top-right corner (default 12.dp)
 * and sharp 90-degree corners everywhere else. No cross marks.
 */
class ChamferedShape(
    private val topRightChamfer: Dp = 12.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cutPx = with(density) { topRightChamfer.toPx() }
            .coerceAtMost(minOf(size.width, size.height))

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width - cutPx, 0f)
            lineTo(size.width, cutPx)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

val DefaultChamferedShape = ChamferedShape(12.dp)

@Composable
fun ChamferedSurface(
    modifier: Modifier = Modifier,
    chamfer: Dp = 12.dp,
    backgroundColor: Color = OctoTile,
    borderColor: Color = OctoBorder,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = ChamferedShape(chamfer)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color = backgroundColor, shape = shape)
            .border(width = borderWidth, color = borderColor, shape = shape),
        content = content
    )
}

