package com.donttap.game.engine

import com.donttap.game.data.*
import kotlin.random.Random

/**
 * Generates a single Round for a given rule. Every generator guarantees:
 *  - positions are spread out (no overlap) on a grid with jitter
 *  - exactly one forbidden object (unless the rule is explicitly a combo rule)
 *  - the forbidden object is always one of the objects actually shown
 *
 * `roundIndexInLevel` lets each rule ramp difficulty (speed, subtlety) as the
 * player progresses through a level, per the difficulty-progression spec.
 */
object RoundGenerator {

    private val shapes = ShapeType.values().toList()

    /** Evenly spaced, slightly jittered positions so objects never overlap. */
    private fun gridPositions(count: Int): List<Pair<Float, Float>> {
        val cols = when {
            count <= 4 -> 2
            count <= 6 -> 3
            else -> 3
        }
        val rows = (count + cols - 1) / cols
        val positions = mutableListOf<Pair<Float, Float>>()
        for (i in 0 until count) {
            val row = i / cols
            val col = i % cols
            val cellW = 1f / cols
            val cellH = 1f / rows
            val jitterX = (Random.nextFloat() - 0.5f) * cellW * 0.25f
            val jitterY = (Random.nextFloat() - 0.5f) * cellH * 0.25f
            val x = cellW * col + cellW / 2f + jitterX
            val y = cellH * row + cellH / 2f + jitterY
            positions.add(x.coerceIn(0.12f, 0.88f) to y.coerceIn(0.12f, 0.88f))
        }
        positions.shuffle()
        return positions
    }

    fun generate(rule: GameRule, roundIndexInLevel: Int): Round = when (rule) {
        GameRule.AVOID_COLOR -> avoidColor()
        GameRule.AVOID_LARGEST -> avoidLargest()
        GameRule.AVOID_SHAPE -> avoidShape()
        GameRule.AVOID_MOVING -> avoidMoving(roundIndexInLevel)
        GameRule.AVOID_PREVIOUS_COLOR -> avoidPreviousColor()
        GameRule.AVOID_CHANGED_OBJECT -> avoidChangedObject(roundIndexInLevel)
        GameRule.AVOID_ARROW_TARGET -> avoidArrowTarget()
        GameRule.AVOID_FIRST_OBJECT -> avoidFirstObject(roundIndexInLevel)
        GameRule.AVOID_DISTRACTION_COLOR -> avoidDistractionColor()
        GameRule.AVOID_COMBO_LARGEST_COLOR -> avoidComboLargestColor()
    }

    private fun avoidColor(): Round {
        val forbiddenColor = Palette.all.random()
        val colors = Palette.all.shuffled().take(4).toMutableList()
        if (!colors.contains(forbiddenColor)) colors[0] = forbiddenColor
        val positions = gridPositions(colors.size)
        val objects = colors.mapIndexed { i, c ->
            GameObjectData(
                id = i, shape = ShapeType.CIRCLE, color = c, sizeDp = 90f,
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = c == forbiddenColor
            )
        }
        return Round(GameRule.AVOID_COLOR, "DO NOT TAP\n${Palette.name(forbiddenColor)}", objects)
    }

    private fun avoidLargest(): Round {
        val count = 5
        val sizes = generateSequence { Random.nextInt(50, 130) }.distinct().take(count).toMutableList()
        while (sizes.size < count) sizes.add(Random.nextInt(50, 130))
        val maxSize = sizes.max()
        val positions = gridPositions(count)
        val colors = Palette.all.shuffled()
        val objects = sizes.mapIndexed { i, s ->
            GameObjectData(
                id = i, shape = ShapeType.CIRCLE, color = colors[i % colors.size], sizeDp = s.toFloat(),
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = s == maxSize
            )
        }
        return Round(GameRule.AVOID_LARGEST, "DO NOT TAP\nTHE BIGGEST", objects)
    }

    private fun avoidShape(): Round {
        val chosen = shapes.shuffled().take(5)
        val positions = gridPositions(chosen.size)
        val colors = Palette.all.shuffled()
        val objects = chosen.mapIndexed { i, s ->
            GameObjectData(
                id = i, shape = s, color = colors[i % colors.size], sizeDp = 90f,
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = s == ShapeType.TRIANGLE
            )
        }
        // Guarantee the triangle is present.
        val finalObjects = if (objects.none { it.isForbidden }) {
            objects.toMutableList().also {
                it[0] = it[0].copy(shape = ShapeType.TRIANGLE, isForbidden = true)
            }
        } else objects
        return Round(GameRule.AVOID_SHAPE, "DO NOT TAP\nTHE TRIANGLE", finalObjects)
    }

    private fun avoidMoving(roundIndex: Int): Round {
        val count = 4 + Random.nextInt(0, 2)
        val positions = gridPositions(count)
        val colors = Palette.all.shuffled()
        val movingIndex = Random.nextInt(count)
        // amplitude ramps slightly with round index, capped for fairness
        val amplitude = (0.06f + roundIndex * 0.01f).coerceAtMost(0.12f)
        val objects = (0 until count).map { i ->
            GameObjectData(
                id = i, shape = ShapeType.CIRCLE, color = colors[i % colors.size], sizeDp = 90f,
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = i == movingIndex,
                movesWithAmplitude = if (i == movingIndex) amplitude else 0f
            )
        }
        return Round(GameRule.AVOID_MOVING, "DO NOT TAP\nTHE MOVING ONE", objects)
    }

    private fun avoidPreviousColor(): Round {
        val count = 4
        val positions = gridPositions(count)
        val blueIndex = Random.nextInt(count)
        val otherColors = Palette.all.filter { it != Palette.BLUE }.shuffled()
        val objects = (0 until count).map { i ->
            val trueColor = if (i == blueIndex) Palette.BLUE else otherColors[i % otherColors.size]
            GameObjectData(
                id = i, shape = ShapeType.CIRCLE, color = Palette.NEUTRAL, sizeDp = 90f,
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = i == blueIndex,
                revealColorThenHideMs = 1000L,
                trueColor = trueColor
            )
        }
        return Round(GameRule.AVOID_PREVIOUS_COLOR, "DO NOT TAP THE ONE\nTHAT WAS BLUE", objects)
    }

    private fun avoidChangedObject(roundIndex: Int): Round {
        val count = 5
        val positions = gridPositions(count)
        val colors = Palette.all.shuffled()
        val changeIndex = Random.nextInt(count)
        val changeDelay = (1200L - roundIndex * 40L).coerceAtLeast(800L)
        val changeType = Random.nextInt(3) // 0 color, 1 size, 2 shape
        val baseShape = ShapeType.CIRCLE
        val objects = (0 until count).map { i ->
            val isChanged = i == changeIndex
            GameObjectData(
                id = i, shape = baseShape, color = colors[i % colors.size], sizeDp = 90f,
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = isChanged,
                changeAtMs = if (isChanged) changeDelay else null,
                changedColor = if (isChanged && changeType == 0) colors[(i + 1) % colors.size] else null,
                changedSizeDp = if (isChanged && changeType == 1) 115f else null,
                changedShape = if (isChanged && changeType == 2) ShapeType.STAR else null
            )
        }
        return Round(GameRule.AVOID_CHANGED_OBJECT, "DO NOT TAP THE\nOBJECT THAT CHANGED", objects)
    }

    private fun avoidArrowTarget(): Round {
        val count = 5
        val positions = gridPositions(count)
        val colors = Palette.all.shuffled()
        val shapesForRound = shapes.shuffled()
        val targetIndex = Random.nextInt(count)
        val objects = (0 until count).map { i ->
            GameObjectData(
                id = i, shape = shapesForRound[i % shapesForRound.size], color = colors[i % colors.size],
                sizeDp = 90f, xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = i == targetIndex, isArrowTarget = i == targetIndex
            )
        }
        val target = objects[targetIndex]
        val dx = target.xFraction - 0.5f
        val dy = target.yFraction - 0.35f
        val angle = Math.toDegrees(kotlin.math.atan2(dy, dx).toDouble()).toFloat()
        return Round(GameRule.AVOID_ARROW_TARGET, "DO NOT TAP WHAT\nTHE ARROW POINTS TO", objects, arrowAngleDeg = angle)
    }

    private fun avoidFirstObject(roundIndex: Int): Round {
        val count = 4
        val positions = gridPositions(count)
        val colors = Palette.all.shuffled()
        val shapesForRound = shapes.shuffled()
        val order = (0 until count).shuffled()
        val stagger = (650L - roundIndex * 30L).coerceAtLeast(350L)
        val objects = (0 until count).map { i ->
            val appearOrder = order.indexOf(i)
            GameObjectData(
                id = i, shape = shapesForRound[i % shapesForRound.size], color = colors[i % colors.size],
                sizeDp = 90f, xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = appearOrder == 0,
                appearAtMs = appearOrder * stagger
            )
        }
        return Round(GameRule.AVOID_FIRST_OBJECT, "DO NOT TAP\nTHE FIRST ONE", objects)
    }

    private fun avoidDistractionColor(): Round {
        val forbiddenColor = Palette.all.random()
        val colors = Palette.all.shuffled().take(4).toMutableList()
        if (!colors.contains(forbiddenColor)) colors[0] = forbiddenColor
        val positions = gridPositions(colors.size)
        // Pick a different object to carry the flashy distraction animation.
        val forbiddenPos = colors.indexOf(forbiddenColor)
        var distractIndex = Random.nextInt(colors.size)
        if (distractIndex == forbiddenPos) distractIndex = (distractIndex + 1) % colors.size
        val objects = colors.mapIndexed { i, c ->
            GameObjectData(
                id = i, shape = ShapeType.CIRCLE, color = c, sizeDp = 90f,
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = c == forbiddenColor,
                distractionAnimate = i == distractIndex
            )
        }
        return Round(GameRule.AVOID_DISTRACTION_COLOR, "DO NOT TAP\n${Palette.name(forbiddenColor)}", objects)
    }

    private fun avoidComboLargestColor(): Round {
        val count = 5
        val positions = gridPositions(count)
        val targetColor = Palette.all.random()
        val sizes = generateSequence { Random.nextInt(50, 130) }.distinct().take(count).toMutableList()
        while (sizes.size < count) sizes.add(Random.nextInt(50, 130))
        val colors = (0 until count).map { if (Random.nextFloat() < 0.5f) targetColor else Palette.all.filter { c -> c != targetColor }.random() }
        // Forbidden = largest object that is also targetColor. Guarantee at least one exists.
        val hasTarget = colors.contains(targetColor)
        val finalColors = if (!hasTarget) colors.toMutableList().also { it[0] = targetColor } else colors
        val candidateIndices = finalColors.indices.filter { finalColors[it] == targetColor }
        val forbiddenIndex = candidateIndices.maxByOrNull { sizes[it] } ?: 0
        val objects = (0 until count).map { i ->
            GameObjectData(
                id = i, shape = ShapeType.CIRCLE, color = finalColors[i], sizeDp = sizes[i].toFloat(),
                xFraction = positions[i].first, yFraction = positions[i].second,
                isForbidden = i == forbiddenIndex
            )
        }
        return Round(
            GameRule.AVOID_COMBO_LARGEST_COLOR,
            "DO NOT TAP THE\nBIGGEST ${Palette.name(targetColor)} OBJECT",
            objects
        )
    }
}
