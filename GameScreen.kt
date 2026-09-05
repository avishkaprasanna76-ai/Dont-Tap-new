package com.donttap.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donttap.game.audio.GameAudioManager
import com.donttap.game.data.LevelConfig
import com.donttap.game.data.SettingsManager
import com.donttap.game.engine.GameEngine
import com.donttap.game.engine.LevelPhase
import com.donttap.game.engine.RoundOutcome
import com.donttap.game.ui.theme.AccentRed
import com.donttap.game.ui.theme.AccentYellow
import com.donttap.game.ui.theme.SurfaceDark
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    level: LevelConfig,
    settings: SettingsManager,
    audio: GameAudioManager,
    onQuit: () -> Unit,
    onLevelComplete: () -> Unit
) {
    var engine by remember(level.number) { mutableStateOf(GameEngine(level, settings, audio)) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var shakeTrigger by remember { mutableStateOf(0) }

    // Android back button: confirm before quitting mid-level.
    androidx.activity.compose.BackHandler(enabled = true) {
        if (engine.phase == LevelPhase.PLAYING || engine.phase == LevelPhase.COUNTDOWN) {
            showQuitDialog = true
        } else {
            onQuit()
        }
    }

    LaunchedEffect(engine.outcome) {
        if (engine.outcome == RoundOutcome.WRONG) shakeTrigger++
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.donttap.game.ui.theme.BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameHud(engine = engine, level = level)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (engine.phase) {
                    LevelPhase.COUNTDOWN -> Countdown(onDone = { engine.beginRound() })
                    LevelPhase.PLAYING, LevelPhase.ROUND_RESULT -> {
                        PlayField(
                            engine = engine,
                            shakeTrigger = shakeTrigger,
                            onAdvance = { engine.advance() }
                        )
                    }
                    LevelPhase.LEVEL_COMPLETE -> LevelCompleteView(
                        score = engine.score,
                        onContinue = onLevelComplete
                    )
                    LevelPhase.LEVEL_FAILED -> LevelFailedView(
                        onRetry = { engine = engine.restart() },
                        onQuit = onQuit
                    )
                }
            }
        }

        if (showQuitDialog) {
            QuitDialog(
                onContinue = { showQuitDialog = false },
                onQuit = onQuit
            )
        }
    }
}

@Composable
private fun GameHud(engine: GameEngine, level: LevelConfig) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("LEVEL ${level.number}", color = com.donttap.game.ui.theme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("❤".repeat(engine.lives.coerceAtLeast(0)), color = AccentRed, fontSize = 16.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("SCORE ${engine.score}", color = AccentYellow, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (engine.comboMultiplier > 1) {
                Text("COMBO x${engine.comboMultiplier}", color = com.donttap.game.ui.theme.TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun Countdown(onDone: () -> Unit) {
    var count by remember { mutableStateOf(3) }
    LaunchedEffect(Unit) {
        while (count > 0) {
            delay(500)
            count--
        }
        onDone()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (count > 0) count.toString() else "GO!",
            color = AccentYellow,
            fontWeight = FontWeight.Black,
            fontSize = 72.sp
        )
    }
}

@Composable
private fun PlayField(engine: GameEngine, shakeTrigger: Int, onAdvance: () -> Unit) {
    var fieldWidth by remember { mutableStateOf(0f) }
    var fieldHeight by remember { mutableStateOf(0f) }
    val shakeX = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            for (i in 0 until 3) {
                shakeX.animateTo(16f, tween(40))
                shakeX.animateTo(-16f, tween(40))
            }
            shakeX.animateTo(0f, tween(40))
        }
    }

    // Auto-advance shortly after showing round result, per "don't make the
    // player wait unnecessarily between rounds".
    LaunchedEffect(engine.phase, engine.roundNumber) {
        if (engine.phase == LevelPhase.ROUND_RESULT) {
            delay(if (engine.outcome == RoundOutcome.WRONG) 900L else 500L)
            onAdvance()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = shakeX.value }
            .onGloballyPositioned {
                fieldWidth = it.size.width.toFloat()
                fieldHeight = it.size.height.toFloat()
            }
    ) {
        // Instruction banner
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = engine.currentRound.instructionText,
                color = com.donttap.game.ui.theme.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }

        if (fieldWidth > 0f && fieldHeight > 0f) {
            engine.currentRound.objects.forEach { obj ->
                key(engine.roundNumber, obj.id) {
                    GameObjectView(
                        data = obj,
                        fieldWidthPx = fieldWidth,
                        fieldHeightPx = fieldHeight * 0.8f,
                        onTap = { id -> engine.onObjectTapped(id) }
                    )
                }
            }
            engine.currentRound.arrowAngleDeg?.let { angle ->
                ArrowIndicator(angleDeg = angle)
            }
        }

        AnimatedVisibility(
            visible = engine.phase == LevelPhase.ROUND_RESULT,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            ResultBanner(engine = engine)
        }
    }
}

@Composable
private fun ResultBanner(engine: GameEngine) {
    val isCorrect = engine.outcome == RoundOutcome.CORRECT
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isCorrect) "+${engine.lastPointsAwarded}" else "WRONG!",
            color = if (isCorrect) AccentYellow else AccentRed,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp
        )
        if (!isCorrect) {
            Text(
                text = "You tapped the forbidden object.",
                color = com.donttap.game.ui.theme.TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ArrowIndicator(angleDeg: Float) {
    Box(modifier = Modifier.fillMaxSize().padding(top = 90.dp), contentAlignment = Alignment.TopCenter) {
        Text(
            text = "➤",
            color = AccentYellow,
            fontSize = 40.sp,
            modifier = Modifier.rotate(angleDeg + 90f)
        )
    }
}

@Composable
private fun LevelCompleteView(score: Int, onContinue: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("LEVEL COMPLETE", color = AccentYellow, fontWeight = FontWeight.Black, fontSize = 30.sp)
            Spacer(Modifier.height(12.dp))
            Text("Score: $score", color = com.donttap.game.ui.theme.TextPrimary, fontSize = 20.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)) {
                Text("CONTINUE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LevelFailedView(onRetry: () -> Unit, onQuit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TRY AGAIN", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 30.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)) {
                Text("RETRY LEVEL", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onQuit, colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark)) {
                Text("QUIT", color = com.donttap.game.ui.theme.TextPrimary)
            }
        }
    }
}

@Composable
private fun QuitDialog(onContinue: () -> Unit, onQuit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(SurfaceDark, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("QUIT LEVEL?", color = com.donttap.game.ui.theme.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(16.dp))
            Row {
                Button(onClick = onContinue, colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)) {
                    Text("CONTINUE", color = Color.Black)
                }
                Spacer(Modifier.width(12.dp))
                Button(onClick = onQuit, colors = ButtonDefaults.buttonColors(containerColor = AccentRed)) {
                    Text("QUIT", color = Color.White)
                }
            }
        }
    }
}
