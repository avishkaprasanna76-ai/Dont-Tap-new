package com.donttap.game.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.donttap.game.audio.GameAudioManager
import com.donttap.game.data.ALL_LEVELS
import com.donttap.game.data.GameRule
import com.donttap.game.data.LevelConfig
import com.donttap.game.data.Round
import com.donttap.game.data.SettingsManager

enum class RoundOutcome { NONE, CORRECT, WRONG }
enum class LevelPhase { COUNTDOWN, PLAYING, ROUND_RESULT, LEVEL_COMPLETE, LEVEL_FAILED }

/**
 * Owns all mutable game state for a single level session. Recreated each
 * time the player enters a level so state never leaks between attempts.
 * Deliberately framework-light (no ViewModel dependency) so it is easy to
 * unit test and to host inside a single Composable via `remember`.
 */
class GameEngine(
    val level: LevelConfig,
    private val settings: SettingsManager,
    private val audio: GameAudioManager
) {
    var score by mutableStateOf(0)
        private set
    var lives by mutableStateOf(level.lives)
        private set
    var combo by mutableStateOf(0)
        private set
    var comboMultiplier by mutableStateOf(1)
        private set
    var roundNumber by mutableStateOf(1)
        private set
    var phase by mutableStateOf(LevelPhase.COUNTDOWN)
        private set
    var outcome by mutableStateOf(RoundOutcome.NONE)
        private set
    var lastPointsAwarded by mutableStateOf(0)
        private set
    var currentRound: Round by mutableStateOf(nextRound(0))
        private set

    private var roundStartTime = 0L
    private var inputLocked = false

    private fun nextRound(index: Int): Round {
        val rule = pickRuleForRound(index)
        return RoundGenerator.generate(rule, index)
    }

    /** Level 10 mixes rules; combo rules only appear in the final 5 rounds, per spec. */
    private fun pickRuleForRound(index: Int): GameRule {
        if (!level.mixedRules) return level.rules.random()
        val comboRules = level.rules.filter { it == GameRule.AVOID_COMBO_LARGEST_COLOR }
        val singleRules = level.rules.filter { it != GameRule.AVOID_COMBO_LARGEST_COLOR }
        val inFinalFive = index >= level.roundCount - 5
        return if (inFinalFive && comboRules.isNotEmpty() && kotlin.random.Random.nextBoolean()) {
            comboRules.random()
        } else {
            singleRules.random()
        }
    }

    fun beginRound() {
        phase = LevelPhase.PLAYING
        outcome = RoundOutcome.NONE
        inputLocked = false
        roundStartTime = System.currentTimeMillis()
    }

    /** Call when the player taps an object. Returns true if the tap was accepted. */
    fun onObjectTapped(objectId: Int): Boolean {
        if (inputLocked || phase != LevelPhase.PLAYING) return false
        inputLocked = true
        val tapped = currentRound.objects.firstOrNull { it.id == objectId } ?: return false
        audio.playTap()

        if (tapped.isForbidden) {
            handleWrong()
        } else {
            handleCorrect()
        }
        return true
    }

    private fun handleCorrect() {
        combo += 1
        comboMultiplier = when {
            combo >= 10 -> 4
            combo >= 5 -> 3
            combo >= 3 -> 2
            else -> 1
        }
        val elapsed = System.currentTimeMillis() - roundStartTime
        val speedBonus = if (elapsed < 1200) 50 else if (elapsed < 2000) 20 else 0
        val points = (100 + speedBonus) * comboMultiplier
        lastPointsAwarded = points
        score += points
        outcome = RoundOutcome.CORRECT
        phase = LevelPhase.ROUND_RESULT
        audio.playCorrect()
    }

    private fun handleWrong() {
        combo = 0
        comboMultiplier = 1
        lives -= 1
        lastPointsAwarded = 0
        outcome = RoundOutcome.WRONG
        phase = LevelPhase.ROUND_RESULT
        audio.playWrong()
    }

    /** Advance after showing round-result feedback. */
    fun advance() {
        if (lives <= 0) {
            phase = LevelPhase.LEVEL_FAILED
            return
        }
        if (roundNumber >= level.roundCount) {
            finishLevel()
            return
        }
        roundNumber += 1
        currentRound = nextRound(roundNumber - 1)
        beginRound()
    }

    private fun finishLevel() {
        phase = LevelPhase.LEVEL_COMPLETE
        settings.bestScore = score
        settings.setBestScoreForLevel(level.number, score)
        if (level.number < ALL_LEVELS.size) {
            settings.highestUnlockedLevel = level.number + 1
        }
        audio.playLevelComplete()
    }

    /** Restart the same level from scratch (used after LEVEL_FAILED). */
    fun restart(): GameEngine = GameEngine(level, settings, audio)
}
