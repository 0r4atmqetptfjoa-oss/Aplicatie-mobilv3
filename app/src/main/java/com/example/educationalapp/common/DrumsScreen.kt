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
import androidx.compose.foundation.shape.CircleShape
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
 * Bonus screen: drum pads (no external audio assets).
 * If this screen isn't wired in your nav, it's still safe to keep.
 */
@Composable
fun DrumsScreen(navController: NavController) {
    val particles = remember { ParticleController() }
    val tone = rememberTonePlayer()
    val haptics = LocalHapticFeedback.current

    var hits by remember { mutableIntStateOf(0) }

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_music_stage,
        hud = GameHudState(title = "Tobe", score = hits, levelLabel = "Lovește pad-urile", starCount = 0),
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
                text = "🎧 Feel the beat!",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrumPad(label = "Kick", tint = Color(0xFF26C6DA), modifier = Modifier.weight(1f)) {
                    tone.play(0)
                    hits++
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    particles.burst(it, count = 60)
                }
                DrumPad(label = "Snare", tint = Color(0xFFFFA726), modifier = Modifier.weight(1f)) {
                    tone.play(3)
                    hits++
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    particles.burst(it, count = 60)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrumPad(label = "Hat", tint = Color(0xFF66BB6A), modifier = Modifier.weight(1f)) {
                    tone.play(6)
                    hits++
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    particles.burst(it, count = 60)
                }
                DrumPad(label = "Tom", tint = Color(0xFF7E57C2), modifier = Modifier.weight(1f)) {
                    tone.play(2)
                    hits++
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    particles.burst(it, count = 60)
                }
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = { navController.popBackStack() }) { Text("Înapoi") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DrumPad(
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onHit: (at: androidx.compose.ui.geometry.Offset) -> Unit
) {
    Box(
        modifier = modifier
            .height(170.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.55f))
                )
            )
            .pointerInput(label) {
                detectTapGestures { pos -> onHit(pos) }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
    }
}
