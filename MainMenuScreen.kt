package com.donttap.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donttap.game.data.SettingsManager
import com.donttap.game.ui.theme.AccentRed
import com.donttap.game.ui.theme.AccentYellow
import com.donttap.game.ui.theme.BackgroundDark
import com.donttap.game.ui.theme.TextPrimary
import com.donttap.game.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    settings: SettingsManager,
    onPlay: () -> Unit,
    onLevels: () -> Unit,
    onSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        IconButton(onClick = onSettings, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("DON'T", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 48.sp, textAlign = TextAlign.Center)
            Text("TAP", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 48.sp, textAlign = TextAlign.Center)

            Spacer(Modifier.height(56.dp))

            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                modifier = Modifier.fillMaxWidth(0.6f).height(56.dp)
            ) {
                Text("PLAY", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onLevels,
                colors = ButtonDefaults.buttonColors(containerColor = com.donttap.game.ui.theme.SurfaceDark),
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
            ) {
                Text("LEVELS", color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(28.dp))

            Text("BEST SCORE: ${settings.bestScore}", color = TextSecondary, fontSize = 14.sp)
        }
    }
}
