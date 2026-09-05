package com.donttap.game.audio

import android.content.Context
import android.media.AudioManager as AndroidAudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.donttap.game.data.SettingsManager

/**
 * Generates simple beeps programmatically (ToneGenerator) instead of shipping
 * audio asset files — keeps the app tiny and fully offline. Also owns
 * haptic feedback so both live behind the same settings check.
 */
class GameAudioManager(private val context: Context, private val settings: SettingsManager) {

    private val toneGen: ToneGenerator by lazy {
        ToneGenerator(AndroidAudioManager.STREAM_MUSIC, 70)
    }

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playTap() {
        if (settings.soundEnabled) toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    fun playCorrect() {
        if (settings.soundEnabled) toneGen.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        if (settings.vibrationEnabled) vibrate(30)
    }

    fun playWrong() {
        if (settings.soundEnabled) toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        if (settings.vibrationEnabled) vibrate(80)
    }

    fun playLevelComplete() {
        if (settings.soundEnabled) toneGen.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        if (settings.vibrationEnabled) vibratePattern(longArrayOf(0, 40, 60, 40, 60, 80))
    }

    fun playButton() {
        if (settings.soundEnabled) toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 40)
    }

    private fun vibrate(ms: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }

    private fun vibratePattern(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun release() {
        toneGen.release()
    }
}
