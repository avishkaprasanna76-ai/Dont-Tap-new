package com.donttap.game.data

import androidx.compose.ui.graphics.Color

/**
 * All shapes the game can render.
 */
enum class ShapeType { CIRCLE, SQUARE, TRIANGLE, STAR, HEXAGON }

/**
 * The mental mechanic a level (or a single round, in Level 10) is testing.
 * Each rule owns: instruction text, a round generator, and validation logic
 * (validation lives implicitly in how `isForbidden` is assigned per object).
 */
enum class GameRule(val baseInstruction: String) {
    AVOID_COLOR("DO NOT TAP {COLOR}"),
    AVOID_LARGEST("DO NOT TAP\nTHE BIGGEST"),
    AVOID_SHAPE("DO NOT TAP\nTHE {SHAPE}"),
    AVOID_MOVING("DO NOT TAP\nTHE MOVING ONE"),
    AVOID_PREVIOUS_COLOR("DO NOT TAP THE ONE\nTHAT WAS {COLOR}"),
    AVOID_CHANGED_OBJECT("DO NOT TAP THE\nOBJECT THAT CHANGED"),
    AVOID_ARROW_TARGET("DO NOT TAP WHAT\nTHE ARROW POINTS TO"),
    AVOID_FIRST_OBJECT("DO NOT TAP\nTHE FIRST ONE"),
    AVOID_DISTRACTION_COLOR("DO NOT TAP {COLOR}"),
    AVOID_COMBO_LARGEST_COLOR("DO NOT TAP THE\nBIGGEST {COLOR} OBJECT")
}

/** Palette used across the game so color-based rules stay consistent. */
object Palette {
    val RED = Color(0xFFFF4757)
    val BLUE = Color(0xFF3E7BFA)
    val GREEN = Color(0xFF2ED573)
    val YELLOW = Color(0xFFFFD23F)
    val PURPLE = Color(0xFFA55EEA)
    val NEUTRAL = Color(0xFF6B7280)

    val all = listOf(RED, BLUE, GREEN, YELLOW, PURPLE)

    fun name(c: Color): String = when (c) {
        RED -> "RED"
        BLUE -> "BLUE"
        GREEN -> "GREEN"
        YELLOW -> "YELLOW"
        PURPLE -> "PURPLE"
        else -> "GRAY"
    }
}

/**
 * A single interactive object on screen. Mutable fields use Compose state
 * so animations (movement, delayed changes) can drive recomposition.
 */
data class GameObjectData(
    val id: Int,
    val shape: ShapeType,
    val color: Color,
    val sizeDp: Float,
    val xFraction: Float,
    val yFraction: Float,
    val isForbidden: Boolean,
    // Optional mechanics used by specific rules:
    val label: String? = null,
    val appearAtMs: Long = 0L,
    val movesWithAmplitude: Float = 0f,
    val changeAtMs: Long? = null,
    val changedColor: Color? = null,
    val changedSizeDp: Float? = null,
    val changedShape: ShapeType? = null,
    val revealColorThenHideMs: Long? = null,
    val trueColor: Color? = null, // the color to show briefly before hiding (Level 5)
    val isArrowTarget: Boolean = false,
    val distractionAnimate: Boolean = false
)

/**
 * A fully generated round: the objects to display, which rule produced it,
 * and the instruction text to show the player.
 */
data class Round(
    val rule: GameRule,
    val instructionText: String,
    val objects: List<GameObjectData>,
    val arrowAngleDeg: Float? = null
)

/** Static configuration describing one of the 10 levels. */
data class LevelConfig(
    val number: Int,
    val title: String,
    val rules: List<GameRule>,
    val roundCount: Int,
    val lives: Int = 3,
    val mixedRules: Boolean = false // Level 10: draws from all previous rules
)

val ALL_LEVELS = listOf(
    LevelConfig(1, "COLOR", listOf(GameRule.AVOID_COLOR), 5),
    LevelConfig(2, "SIZE", listOf(GameRule.AVOID_LARGEST), 6),
    LevelConfig(3, "SHAPE", listOf(GameRule.AVOID_SHAPE), 7),
    LevelConfig(4, "MOVEMENT", listOf(GameRule.AVOID_MOVING), 7),
    LevelConfig(5, "MEMORY", listOf(GameRule.AVOID_PREVIOUS_COLOR), 8),
    LevelConfig(6, "CHANGE", listOf(GameRule.AVOID_CHANGED_OBJECT), 8),
    LevelConfig(7, "REVERSE THINKING", listOf(GameRule.AVOID_ARROW_TARGET), 8),
    LevelConfig(8, "TIMING", listOf(GameRule.AVOID_FIRST_OBJECT), 8),
    LevelConfig(9, "DISTRACTION", listOf(GameRule.AVOID_DISTRACTION_COLOR), 10),
    LevelConfig(
        10, "THE FINAL TEST",
        listOf(
            GameRule.AVOID_COLOR, GameRule.AVOID_LARGEST, GameRule.AVOID_SHAPE,
            GameRule.AVOID_MOVING, GameRule.AVOID_PREVIOUS_COLOR, GameRule.AVOID_CHANGED_OBJECT,
            GameRule.AVOID_ARROW_TARGET, GameRule.AVOID_FIRST_OBJECT,
            GameRule.AVOID_COMBO_LARGEST_COLOR
        ),
        15, lives = 5, mixedRules = true
    )
)
