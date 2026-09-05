package com.donttap.game.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import com.donttap.game.data.ShapeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Draws any of the game's shapes into a square canvas of the given size. */
@Composable
fun ShapeCanvas(shape: ShapeType, color: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w / 2f * 0.92f
        when (shape) {
            ShapeType.CIRCLE -> drawCircle(color = color, radius = radius, center = center)
            ShapeType.SQUARE -> {
                val side = radius * 1.6f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(center.x - side / 2f, center.y - side / 2f),
                    size = androidx.compose.ui.geometry.Size(side, side),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(side * 0.15f)
                )
            }
            ShapeType.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(center.x, center.y - radius)
                    lineTo(center.x - radius * 0.95f, center.y + radius * 0.8f)
                    lineTo(center.x + radius * 0.95f, center.y + radius * 0.8f)
                    close()
                }
                drawPath(path, color = color)
            }
            ShapeType.STAR -> drawPath(starPath(center, radius, radius * 0.45f, 5), color = color)
            ShapeType.HEXAGON -> drawPath(polygonPath(center, radius, 6), color = color)
        }
    }
}

private fun polygonPath(center: Offset, radius: Float, sides: Int): Path {
    val path = Path()
    for (i in 0 until sides) {
        val angle = (PI * 2 * i / sides - PI / 2).toFloat()
        val point = Offset(center.x + radius * cos(angle), center.y + radius * sin(angle))
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

private fun starPath(center: Offset, outerRadius: Float, innerRadius: Float, points: Int): Path {
    val path = Path()
    val total = points * 2
    for (i in 0 until total) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (PI * i / points - PI / 2).toFloat()
        val point = Offset(center.x + r * cos(angle), center.y + r * sin(angle))
        if (i == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}
