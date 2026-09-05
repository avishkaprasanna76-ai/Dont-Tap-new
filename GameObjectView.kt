package com.donttap.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.donttap.game.data.GameObjectData
import com.donttap.game.data.ShapeType
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Renders one interactive object and owns all of its *local* timed
 * behavior: delayed color reveal (Level 5), delayed mutation (Level 6),
 * staggered appearance (Level 8), gentle movement (Level 4), and the
 * flashy-but-fair distraction animation (Level 9).
 */
@Composable
fun GameObjectView(
    data: GameObjectData,
    fieldWidthPx: Float,
    fieldHeightPx: Float,
    onTap: (Int) -> Unit
) {
    // Level 8: object stays invisible until its stagger delay elapses.
    var visible by remember(data.id) { mutableStateOf(data.appearAtMs == 0L) }
    LaunchedEffect(data.id, data.appearAtMs) {
        if (data.appearAtMs > 0L) {
            delay(data.appearAtMs)
            visible = true
        }
    }

    // Level 5: show true color briefly, then hide to neutral.
    var displayColor by remember(data.id) { mutableStateOf(data.trueColor ?: data.color) }
    LaunchedEffect(data.id, data.revealColorThenHideMs) {
        data.revealColorThenHideMs?.let { hideDelay ->
            delay(hideDelay)
            displayColor = data.color
        }
    }

    // Level 6: mutate color/size/shape after a delay.
    var displayShape by remember(data.id) { mutableStateOf(data.shape) }
    var displaySize by remember(data.id) { mutableStateOf(data.sizeDp) }
    LaunchedEffect(data.id, data.changeAtMs) {
        data.changeAtMs?.let { delayMs ->
            delay(delayMs)
            data.changedColor?.let { displayColor = it }
            data.changedSizeDp?.let { displaySize = it }
            data.changedShape?.let { displayShape = it }
        }
    }

    // Level 4: gentle circular movement for the moving object only.
    val infiniteTransition = rememberInfiniteTransition(label = "move")
    val moveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (data.movesWithAmplitude > 0f) (2 * Math.PI).toFloat() else 0.0001f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "moveProgress"
    )
    val moveOffsetXPx = if (data.movesWithAmplitude > 0f) cos(moveProgress) * data.movesWithAmplitude * fieldWidthPx else 0f
    val moveOffsetYPx = if (data.movesWithAmplitude > 0f) sin(moveProgress) * data.movesWithAmplitude * fieldHeightPx else 0f

    // Level 9: aggressive but fair pulse animation on the decoy object.
    val distractionScale by if (data.distractionAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
            label = "distraction"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    if (!visible) return

    val sizePx = displaySize
    val baseX = data.xFraction * fieldWidthPx - (sizePx / 2f)
    val baseY = data.yFraction * fieldHeightPx - (sizePx / 2f)

    Box(
        modifier = Modifier
            .offset { IntOffset((baseX + moveOffsetXPx).toInt(), (baseY + moveOffsetYPx).toInt()) }
            .graphicsLayer {
                scaleX = distractionScale
                scaleY = distractionScale
            }
    ) {
        TappableShape(
            shape = displayShape,
            color = displayColor,
            size = displaySize.dp,
            onTap = { onTap(data.id) }
        )
    }
}

@Composable
private fun TappableShape(
    shape: ShapeType,
    color: Color,
    size: Dp,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            )
    ) {
        ShapeCanvas(shape = shape, color = color, size = size)
    }
}
