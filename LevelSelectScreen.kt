package com.donttap.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donttap.game.data.ALL_LEVELS
import com.donttap.game.data.SettingsManager
import com.donttap.game.ui.theme.AccentYellow
import com.donttap.game.ui.theme.BackgroundDark
import com.donttap.game.ui.theme.SurfaceDark
import com.donttap.game.ui.theme.TextPrimary
import com.donttap.game.ui.theme.TextSecondary

@Composable
fun LevelSelectScreen(
    settings: SettingsManager,
    onBack: () -> Unit,
    onSelectLevel: (Int) -> Unit
) {
    val unlocked = settings.highestUnlockedLevel

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
            Text("LEVELS", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 24.sp)
        }

        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ALL_LEVELS) { level ->
                val isUnlocked = level.number <= unlocked
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            if (isUnlocked) SurfaceDark else SurfaceDark.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(enabled = isUnlocked) { onSelectLevel(level.number) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(level.number.toString(), color = AccentYellow, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    } else {
                        Text("🔒", fontSize = 18.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("BEST SCORE: ${settings.bestScore}", color = TextSecondary, fontSize = 14.sp)
    }
}
