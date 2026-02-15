package com.example.educationalapp

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.fx.rememberTonePlayer

/**
 * Bonus screen: neon piano.
 */
@Composable
fun PianoScreen(navController: NavController) {
    val particles = remember { ParticleController() }
    val tone = rememberTonePlayer()
    val haptics = LocalHapticFeedback.current

    var notesPlayed by remember { mutableIntStateOf(0) }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_music_stage,
        hud = GameHudState(title = "Pian Neon", score = notesPlayed, levelLabel = "Atinge clapele", starCount = 0),
        onBack = { navController.popBackStack() },
        particleController = particles
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Text(
                text = "🎹 Pian Neon",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 8) {
                    PianoKey(
                        label = listOf("DO", "RE", "MI", "FA", "SOL", "LA", "SI", "DO")[i],
                        tint = listOf(
                            Color(0xFF42A5F5),
                            Color(0xFF26C6DA),
                            Color(0xFF66BB6A),
                            Color(0xFFFFEB3B),
                            Color(0xFFFFA726),
                            Color(0xFFFF5252),
                            Color(0xFFEC407A),
                            Color(0xFF7E57C2)
                        )[i],
                        modifier = Modifier.weight(1f)
                    ) { at ->
                        tone.play(i)
                        notesPlayed++
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        particles.burst(at, count = 50)
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = { navController.popBackStack() }) { Text("Înapoi") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PianoKey(
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onPress: (at: androidx.compose.ui.geometry.Offset) -> Unit
) {
    Box(
        modifier = modifier
            .height(190.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.92f), tint.copy(alpha = 0.45f))
                )
            )
            .pointerInput(label) {
                detectTapGestures { pos -> onPress(pos) }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(bottom = 10.dp),
            color = Color.Black,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Black
        )
    }
}
