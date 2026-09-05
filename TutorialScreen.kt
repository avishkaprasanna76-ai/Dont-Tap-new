package com.donttap.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donttap.game.ui.theme.AccentRed
import com.donttap.game.ui.theme.AccentYellow
import com.donttap.game.ui.theme.BackgroundDark
import com.donttap.game.ui.theme.TextPrimary
import com.donttap.game.ui.theme.TextSecondary

/** Short, single-screen first-launch tutorial, per spec: no long onboarding. */
@Composable
fun TutorialScreen(onGotIt: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("DON'T", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 40.sp)
            Text("TAP", color = AccentRed, fontWeight = FontWeight.Black, fontSize = 40.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                "Tap anything except what the game tells you to avoid.",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = onGotIt,
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                modifier = Modifier.fillMaxWidth(0.6f).height(52.dp)
            ) {
                Text("GOT IT", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}
