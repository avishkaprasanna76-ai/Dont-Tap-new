package com.donttap.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donttap.game.data.SettingsManager
import com.donttap.game.ui.theme.AccentRed
import com.donttap.game.ui.theme.AccentYellow
import com.donttap.game.ui.theme.BackgroundDark
import com.donttap.game.ui.theme.TextPrimary

@Composable
fun SettingsScreen(settings: SettingsManager, onBack: () -> Unit) {
    var soundOn by remember { mutableStateOf(settings.soundEnabled) }
    var vibrationOn by remember { mutableStateOf(settings.vibrationEnabled) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("SETTINGS", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        SettingRow("Sound", soundOn) {
            soundOn = it
            settings.soundEnabled = it
        }
        SettingRow("Vibration", vibrationOn) {
            vibrationOn = it
            settings.vibrationEnabled = it
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
        ) {
            Text("RESET PROGRESS", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Progress?") },
            text = { Text("This will erase your unlocked levels and best scores. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    settings.resetProgress()
                    showResetDialog = false
                }) { Text("RESET", color = AccentRed) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("CANCEL") }
            }
        )
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 18.sp)
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AccentYellow)
        )
    }
}
