package com.example.educationalapp.minigames.musicalpattern

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

// Additional imports for animations
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale

/**
 * Compose screen for the Musical Pattern Parade mini‑game.  A random
 * instrument pattern is shown to the child (as text for now); the child
 * repeats it by tapping the instrument buttons.  On a correct sequence,
 * the pattern length increases to add challenge.  This skeleton omits
 * actual audio playback; in the final version, each instrument tap should
 * play the appropriate sound effect.
 */
@Composable
fun MusicalPatternGameScreen(viewModel: MusicalPatternGameViewModel) {
    // State to hold the user's progress and feedback
    var userIndex by remember { mutableStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    val currentPattern = viewModel.currentPattern

    // Handles instrument taps; checks sequence and updates pattern when
    // completed correctly.  Defined as a lambda so it is visible to the
    // composable scopes below.
    val handleTap: (InstrumentType) -> Unit = { tapped ->
        val expected = currentPattern[userIndex]
        if (tapped == expected) {
            userIndex++
            if (userIndex >= currentPattern.size) {
                // Completed pattern successfully
                feedbackMessage = "Bravo!"
                viewModel.advancePattern()
                userIndex = 0
            }
        } else {
            // Wrong instrument tapped
            feedbackMessage = "Greșit, încearcă din nou!"
            userIndex = 0
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Repetă secvența de instrumente",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(8.dp))
            // Display the pattern as names for now
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                currentPattern.forEach { instrument ->
                    Text(instrument.label, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.size(24.dp))
            // Instrument buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InstrumentButton(type = InstrumentType.DRUMS) { tapped ->
                    handleTap(tapped)
                }
                InstrumentButton(type = InstrumentType.XYLOPHONE) { tapped ->
                    handleTap(tapped)
                }
                InstrumentButton(type = InstrumentType.GUITAR) { tapped ->
                    handleTap(tapped)
                }
                InstrumentButton(type = InstrumentType.PIANO) { tapped ->
                    handleTap(tapped)
                }
            }
            feedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.size(16.dp))
                Text(msg, color = if (msg.contains("Bravo")) Color.Green else Color.Red)
            }
        }
    }
}

@Composable
fun InstrumentButton(type: InstrumentType, onTap: (InstrumentType) -> Unit) {
    val color = when (type) {
        InstrumentType.DRUMS -> Color(0xFFFFD700) // gold
        InstrumentType.XYLOPHONE -> Color(0xFF8A2BE2) // purple
        InstrumentType.GUITAR -> Color(0xFF00BFFF) // blue
        InstrumentType.PIANO -> Color(0xFF00FA9A) // green
    }
    // Local state to trigger a bounce animation on tap
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 1.2f else 1f, animationSpec = tween(durationMillis = 200))
    // When pressed becomes true, reset it after a delay to return to original size
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(200)
            pressed = false
        }
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable {
                pressed = true
                onTap(type)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(type.label, color = Color.Black)
    }
}