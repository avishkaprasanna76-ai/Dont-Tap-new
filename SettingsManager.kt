package com.donttap.game.data

import android.content.Context
import android.content.SharedPreferences

/**
 * All local persistence for the game lives here: SharedPreferences only,
 * no network, no Firebase, no backend. Everything survives app restarts
 * and works fully offline (including airplane mode).
 */
class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("dont_tap_prefs", Context.MODE_PRIVATE)

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION, value).apply()

    var highestUnlockedLevel: Int
        get() = prefs.getInt(KEY_UNLOCKED, 1)
        set(value) = prefs.edit().putInt(KEY_UNLOCKED, value.coerceAtLeast(prefs.getInt(KEY_UNLOCKED, 1))).apply()

    var bestScore: Int
        get() = prefs.getInt(KEY_BEST_SCORE, 0)
        set(value) {
            if (value > bestScore) prefs.edit().putInt(KEY_BEST_SCORE, value).apply()
        }

    var hasSeenTutorial: Boolean
        get() = prefs.getBoolean(KEY_TUTORIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_TUTORIAL, value).apply()

    fun bestScoreForLevel(level: Int): Int = prefs.getInt("$KEY_LEVEL_BEST_PREFIX$level", 0)

    fun setBestScoreForLevel(level: Int, score: Int) {
        if (score > bestScoreForLevel(level)) {
            prefs.edit().putInt("$KEY_LEVEL_BEST_PREFIX$level", score).apply()
        }
    }

    fun resetProgress() {
        prefs.edit()
            .putInt(KEY_UNLOCKED, 1)
            .putInt(KEY_BEST_SCORE, 0)
            .apply()
        for (i in 1..10) prefs.edit().remove("$KEY_LEVEL_BEST_PREFIX$i").apply()
    }

    companion object {
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_UNLOCKED = "highest_unlocked_level"
        private const val KEY_BEST_SCORE = "best_score"
        private const val KEY_TUTORIAL = "has_seen_tutorial"
        private const val KEY_LEVEL_BEST_PREFIX = "level_best_"
    }
}
