package com.pedro.octoos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pedro.octoos.ui.theme.OctoBackground
import com.pedro.octoos.ui.theme.OctoText
import kotlin.math.cos
import kotlin.math.sin

enum class OctoIconType {
    CLAUDE,
    MAPA,
    WHATSAPP,
    MSGS,
    CAMERA,
    GIT,
    TODOS
}

@Composable
fun OctoSolidIcon(
    type: OctoIconType,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = OctoBackground
) {
    Canvas(modifier = modifier.size(size)) {
        when (type) {
            OctoIconType.CLAUDE -> drawClaudeAsterisk(tint)
            OctoIconType.MAPA -> drawMapPin(tint)
            OctoIconType.WHATSAPP, OctoIconType.MSGS -> drawChatBubble(tint)
            OctoIconType.CAMERA -> drawCamera(tint)
            OctoIconType.GIT -> drawGitBranch(tint)
            OctoIconType.TODOS -> drawAllAppsGrid(tint)
        }
    }
}

@Composable
fun OctoCloudIcon(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = OctoText
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            color = tint,
            radius = h * 0.28f,
            center = Offset(w * 0.34f, h * 0.56f)
        )
        drawCircle(
            color = tint,
            radius = h * 0.36f,
            center = Offset(w * 0.54f, h * 0.46f)
        )
        drawCircle(
            color = tint,
            radius = h * 0.25f,
            center = Offset(w * 0.74f, h * 0.58f)
        )
        drawRect(
            color = tint,
            topLeft = Offset(w * 0.32f, h * 0.52f),
            size = Size(w * 0.44f, h * 0.32f)
        )
    }
}

private fun DrawScope.drawClaudeAsterisk(color: Color) {
    val radius = size.minDimension * 0.42f
    val stroke = size.minDimension * 0.11f
    for (i in 0 until 3) {
        val angleRad = Math.toRadians((i * 60.0) - 90.0)
        val dx = (cos(angleRad) * radius).toFloat()
        val dy = (sin(angleRad) * radius).toFloat()
        drawLine(
            color = color,
            start = Offset(center.x - dx, center.y - dy),
            end = Offset(center.x + dx, center.y + dy),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawMapPin(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.11f
    val pinPath = Path().apply {
        moveTo(w * 0.5f, h * 0.90f)
        cubicTo(
            w * 0.20f, h * 0.62f,
            w * 0.18f, h * 0.15f,
            w * 0.5f, h * 0.12f
        )
        cubicTo(
            w * 0.82f, h * 0.15f,
            w * 0.80f, h * 0.62f,
            w * 0.5f, h * 0.90f
        )
        close()
    }
    drawPath(
        path = pinPath,
        color = color,
        style = Stroke(width = stroke, join = StrokeJoin.Round)
    )
    drawCircle(
        color = color,
        radius = w * 0.10f,
        center = Offset(w * 0.5f, h * 0.42f)
    )
}

private fun DrawScope.drawChatBubble(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.12f
    drawCircle(
        color = color,
        radius = w * 0.34f,
        center = Offset(w * 0.52f, h * 0.48f),
        style = Stroke(width = stroke)
    )
    val tail = Path().apply {
        moveTo(w * 0.26f, h * 0.68f)
        lineTo(w * 0.16f, h * 0.86f)
        lineTo(w * 0.42f, h * 0.78f)
        close()
    }
    drawPath(path = tail, color = color)
}

private fun DrawScope.drawCamera(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.10f
    // Top viewfinder bump
    drawRect(
        color = color,
        topLeft = Offset(w * 0.34f, h * 0.16f),
        size = Size(w * 0.32f, h * 0.12f)
    )
    // Camera body outline
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.12f, h * 0.28f),
        size = Size(w * 0.76f, h * 0.54f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = Stroke(width = stroke)
    )
    // Center lens
    drawCircle(
        color = color,
        radius = w * 0.13f,
        center = Offset(w * 0.50f, h * 0.55f)
    )
}

private fun DrawScope.drawGitBranch(color: Color) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.11f
    val nodeRadius = w * 0.10f

    val topLeftNode = Offset(w * 0.32f, h * 0.22f)
    val bottomLeftNode = Offset(w * 0.32f, h * 0.78f)
    val rightNode = Offset(w * 0.72f, h * 0.44f)

    // Vertical main trunk
    drawLine(
        color = color,
        start = topLeftNode,
        end = bottomLeftNode,
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )

    // Branch curve from trunk to right node
    val branchPath = Path().apply {
        moveTo(bottomLeftNode.x, h * 0.64f)
        cubicTo(
            w * 0.55f, h * 0.64f,
            rightNode.x, h * 0.60f,
            rightNode.x, rightNode.y
        )
    }
    drawPath(
        path = branchPath,
        color = color,
        style = Stroke(width = stroke, cap = StrokeCap.Round)
    )

    drawCircle(color = color, radius = nodeRadius, center = topLeftNode)
    drawCircle(color = color, radius = nodeRadius, center = bottomLeftNode)
    drawCircle(color = color, radius = nodeRadius, center = rightNode)
}

private fun DrawScope.drawAllAppsGrid(color: Color) {
    val w = size.width
    val h = size.height
    val boxLeft = w * 0.14f
    val boxTop = h * 0.14f
    val boxSize = w * 0.72f

    // Solid outer square with 3x3 negative cutouts inside
    drawRect(
        color = color,
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxSize, boxSize)
    )

    val dotSize = boxSize * 0.13f
    val step = boxSize / 3f
    for (row in 0 until 3) {
        for (col in 0 until 3) {
            val cx = boxLeft + step * (col + 0.5f) - dotSize / 2f
            val cy = boxTop + step * (row + 0.5f) - dotSize / 2f
            drawRect(
                color = Color(0xFFD4FF3A),
                topLeft = Offset(cx, cy),
                size = Size(dotSize, dotSize)
            )
        }
    }
}

