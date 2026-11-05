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
    TODOS,
    YOUTUBE,
    MUSIC,
    SPOTIFY,
    INSTAGRAM,
    GMAIL,
    CHROME,
    PHOTOS,
    CALENDAR,
    NOTES,
    TELEGRAM,
    X,
    PHONE,
    SETTINGS
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
            OctoIconType.YOUTUBE -> drawYoutube(tint)
            OctoIconType.MUSIC -> drawMusicNote(tint)
            OctoIconType.SPOTIFY -> drawSpotify(tint)
            OctoIconType.INSTAGRAM -> drawInstagram(tint)
            OctoIconType.GMAIL -> drawEnvelope(tint)
            OctoIconType.CHROME -> drawChromeRing(tint)
            OctoIconType.PHOTOS -> drawMountains(tint)
            OctoIconType.CALENDAR -> drawCalendar(tint)
            OctoIconType.NOTES -> drawNotes(tint)
            OctoIconType.TELEGRAM -> drawPaperPlane(tint)
            OctoIconType.X -> drawCross(tint)
            OctoIconType.PHONE -> drawPhone(tint)
            OctoIconType.SETTINGS -> drawGear(tint)
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

private fun DrawScope.drawYoutube(color: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.06f, h * 0.20f),
        size = Size(w * 0.88f, h * 0.60f),
        cornerRadius = CornerRadius(w * 0.18f),
        style = Stroke(width = w * 0.11f)
    )
    val play = Path().apply {
        moveTo(w * 0.42f, h * 0.36f)
        lineTo(w * 0.64f, h * 0.50f)
        lineTo(w * 0.42f, h * 0.64f)
        close()
    }
    drawPath(play, color)
}

private fun DrawScope.drawMusicNote(color: Color) {
    val w = size.width
    val h = size.height
    val stem = w * 0.10f
    drawCircle(color, w * 0.13f, Offset(w * 0.32f, h * 0.72f))
    drawCircle(color, w * 0.13f, Offset(w * 0.70f, h * 0.60f))
    drawLine(color, Offset(w * 0.45f, h * 0.72f), Offset(w * 0.45f, h * 0.20f), strokeWidth = stem)
    drawLine(color, Offset(w * 0.83f, h * 0.60f), Offset(w * 0.83f, h * 0.20f), strokeWidth = stem)
    drawLine(color, Offset(w * 0.45f, h * 0.20f), Offset(w * 0.83f, h * 0.20f), strokeWidth = stem * 1.3f)
}

private fun DrawScope.drawSpotify(color: Color) {
    val w = size.width
    val h = size.height
    val c = Offset(w * 0.5f, h * 0.5f)
    val s = w * 0.09f
    drawCircle(color, w * 0.44f, c, style = Stroke(width = s))
    listOf(0.14f, 0.23f, 0.32f).forEach { r ->
        drawArc(
            color = color,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(c.x - w * r, c.y - w * r),
            size = Size(w * r * 2f, w * r * 2f),
            style = Stroke(width = s * 0.85f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawInstagram(color: Color) {
    val w = size.width
    val h = size.height
    val s = w * 0.09f
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.10f, h * 0.10f),
        size = Size(w * 0.80f, h * 0.80f),
        cornerRadius = CornerRadius(w * 0.22f),
        style = Stroke(width = s)
    )
    drawCircle(color, w * 0.20f, Offset(w * 0.5f, h * 0.5f), style = Stroke(width = s))
    drawCircle(color, w * 0.05f, Offset(w * 0.72f, h * 0.28f))
}

private fun DrawScope.drawEnvelope(color: Color) {
    val w = size.width
    val h = size.height
    val s = w * 0.09f
    drawRect(
        color = color,
        topLeft = Offset(w * 0.08f, h * 0.20f),
        size = Size(w * 0.84f, h * 0.60f),
        style = Stroke(width = s)
    )
    val flap = Path().apply {
        moveTo(w * 0.08f, h * 0.20f)
        lineTo(w * 0.50f, h * 0.55f)
        lineTo(w * 0.92f, h * 0.20f)
    }
    drawPath(flap, color, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawChromeRing(color: Color) {
    val w = size.width
    val h = size.height
    val c = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color, w * 0.42f, c, style = Stroke(width = w * 0.10f))
    drawCircle(color, w * 0.14f, c)
}

private fun DrawScope.drawMountains(color: Color) {
    val w = size.width
    val h = size.height
    drawCircle(color, w * 0.09f, Offset(w * 0.74f, h * 0.28f))
    val back = Path().apply {
        moveTo(w * 0.06f, h * 0.84f)
        lineTo(w * 0.38f, h * 0.36f)
        lineTo(w * 0.60f, h * 0.84f)
        close()
    }
    val front = Path().apply {
        moveTo(w * 0.34f, h * 0.84f)
        lineTo(w * 0.60f, h * 0.48f)
        lineTo(w * 0.94f, h * 0.84f)
        close()
    }
    drawPath(back, color)
    drawPath(front, color)
}

private fun DrawScope.drawCalendar(color: Color) {
    val w = size.width
    val h = size.height
    val s = w * 0.09f
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.10f, h * 0.18f),
        size = Size(w * 0.80f, h * 0.72f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = s)
    )
    drawRect(color, Offset(w * 0.10f, h * 0.18f), Size(w * 0.80f, h * 0.20f))
    drawLine(color, Offset(w * 0.32f, h * 0.06f), Offset(w * 0.32f, h * 0.24f), strokeWidth = s, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.68f, h * 0.06f), Offset(w * 0.68f, h * 0.24f), strokeWidth = s, cap = StrokeCap.Round)
    val cell = w * 0.14f
    listOf(0.28f to 0.50f, 0.50f to 0.50f, 0.28f to 0.70f, 0.50f to 0.70f).forEach { (x, y) ->
        drawRect(color, Offset(w * x, h * y), Size(cell, cell))
    }
}

private fun DrawScope.drawNotes(color: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.16f, h * 0.10f),
        size = Size(w * 0.68f, h * 0.80f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = Stroke(width = w * 0.08f)
    )
    listOf(0.36f, 0.52f, 0.68f).forEach { y ->
        drawLine(color, Offset(w * 0.30f, h * y), Offset(w * 0.70f, h * y), strokeWidth = w * 0.07f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawPaperPlane(color: Color) {
    val w = size.width
    val h = size.height
    val plane = Path().apply {
        moveTo(w * 0.08f, h * 0.48f)
        lineTo(w * 0.92f, h * 0.12f)
        lineTo(w * 0.74f, h * 0.88f)
        lineTo(w * 0.48f, h * 0.66f)
        lineTo(w * 0.36f, h * 0.84f)
        lineTo(w * 0.36f, h * 0.60f)
        lineTo(w * 0.78f, h * 0.26f)
        lineTo(w * 0.32f, h * 0.56f)
        close()
    }
    drawPath(plane, color)
}

private fun DrawScope.drawCross(color: Color) {
    val w = size.width
    val h = size.height
    val s = w * 0.13f
    drawLine(color, Offset(w * 0.18f, h * 0.18f), Offset(w * 0.82f, h * 0.82f), strokeWidth = s, cap = StrokeCap.Round)
    drawLine(color, Offset(w * 0.82f, h * 0.18f), Offset(w * 0.18f, h * 0.82f), strokeWidth = s, cap = StrokeCap.Round)
}

private fun DrawScope.drawPhone(color: Color) {
    val w = size.width
    val h = size.height
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.28f, h * 0.08f),
        size = Size(w * 0.44f, h * 0.84f),
        cornerRadius = CornerRadius(w * 0.09f),
        style = Stroke(width = w * 0.08f)
    )
    drawCircle(color, w * 0.05f, Offset(w * 0.5f, h * 0.78f))
}

private fun DrawScope.drawGear(color: Color) {
    val w = size.width
    val h = size.height
    val c = Offset(w * 0.5f, h * 0.5f)
    val s = w * 0.13f
    drawCircle(color, w * 0.26f, c, style = Stroke(width = s))
    drawCircle(color, w * 0.09f, c)
    for (deg in 0 until 360 step 45) {
        val a = Math.toRadians(deg.toDouble())
        val start = Offset(c.x + (w * 0.30f * cos(a)).toFloat(), c.y + (w * 0.30f * sin(a)).toFloat())
        val end = Offset(c.x + (w * 0.46f * cos(a)).toFloat(), c.y + (w * 0.46f * sin(a)).toFloat())
        drawLine(color, start, end, strokeWidth = s, cap = StrokeCap.Round)
    }
}
