package com.donttap.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.donttap.game.audio.GameAudioManager
import com.donttap.game.data.ALL_LEVELS
import com.donttap.game.data.SettingsManager
import com.donttap.game.ui.screens.GameScreen
import com.donttap.game.ui.screens.LevelSelectScreen
import com.donttap.game.ui.screens.MainMenuScreen
import com.donttap.game.ui.screens.SettingsScreen
import com.donttap.game.ui.screens.TutorialScreen
import com.donttap.game.ui.theme.DontTapTheme

/**
 * Single-Activity app. Screen state is a simple sealed hierarchy switched on
 * in `setContent` — no heavy navigation library needed for a game this
 * small, which keeps startup fast per the performance requirements.
 */
sealed class Screen {
    object Tutorial : Screen()
    object MainMenu : Screen()
    object LevelSelect : Screen()
    data class Game(val levelNumber: Int) : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var settings: SettingsManager
    private lateinit var audio: GameAudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settings = SettingsManager(this)
        audio = GameAudioManager(this, settings)

        setContent {
            DontTapTheme {
                var screen by remember {
                    mutableStateOf<Screen>(
                        if (settings.hasSeenTutorial) Screen.MainMenu else Screen.Tutorial
                    )
                }
                // Settings screen returns to whatever screen requested it.
                var previousScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

                when (val current = screen) {
                    is Screen.Tutorial -> TutorialScreen(onGotIt = {
                        settings.hasSeenTutorial = true
                        screen = Screen.Game(1)
                    })

                    is Screen.MainMenu -> MainMenuScreen(
                        settings = settings,
                        onPlay = { screen = Screen.Game(settings.highestUnlockedLevel) },
                        onLevels = { screen = Screen.LevelSelect },
                        onSettings = {
                            previousScreen = Screen.MainMenu
                            screen = Screen.Settings
                        }
                    )

                    is Screen.LevelSelect -> LevelSelectScreen(
                        settings = settings,
                        onBack = { screen = Screen.MainMenu },
                        onSelectLevel = { levelNum -> screen = Screen.Game(levelNum) }
                    )

                    is Screen.Game -> {
                        val levelConfig = ALL_LEVELS.first { it.number == current.levelNumber }
                        GameScreen(
                            level = levelConfig,
                            settings = settings,
                            audio = audio,
                            onQuit = { screen = Screen.MainMenu },
                            onLevelComplete = {
                                screen = if (current.levelNumber < ALL_LEVELS.size) {
                                    Screen.Game(current.levelNumber + 1)
                                } else {
                                    Screen.MainMenu
                                }
                            }
                        )
                    }

                    is Screen.Settings -> SettingsScreen(
                        settings = settings,
                        onBack = { screen = previousScreen }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audio.release()
    }
}
