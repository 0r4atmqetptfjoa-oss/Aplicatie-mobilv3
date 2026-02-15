package com.example.educationalapp.minigames.colormixing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.animateColorAsState

// Additional animation imports for premium effects
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A simple Compose screen for the Color Mixing mini‑game.  This screen displays
 * primary color droplets that children can tap to select.  Two selected colors
 * are then mixed to reveal a new colour.  This skeleton focuses on UI
 * structure; the actual audio prompts and resource loading should be added
 * later using the app's AudioController.
 */
@Composable
fun ColorMixingGameScreen(viewModel: ColorMixingGameViewModel) {
    var firstColor by remember { mutableStateOf<Color?>(null) }
    var secondColor by remember { mutableStateOf<Color?>(null) }
    var resultColor by remember { mutableStateOf<Color?>(null) }

    // Helper to handle colour selection
    fun onColorTapped(color: Color) {
        if (firstColor == null) {
            firstColor = color
        } else if (secondColor == null) {
            secondColor = color
            // Once two colours are selected, compute the result via view model
            resultColor = viewModel.mixColors(firstColor!!, secondColor!!)
        } else {
            // Reset for next try
            firstColor = color
            secondColor = null
            resultColor = null
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Selectează două culori pentru a le amesteca",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Display colour droplet buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ColorDroplet(color = Color.Red) { onColorTapped(Color.Red) }
                ColorDroplet(color = Color.Yellow) { onColorTapped(Color.Yellow) }
                ColorDroplet(color = Color.Blue) { onColorTapped(Color.Blue) }
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Show result with swirling animation when available
            if (resultColor != null) {
                Text(
                    text = "Rezultatul este:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Animate the result colour for a smoother transition
                val animatedResultColor by animateColorAsState(targetValue = resultColor!!)
                // Infinite rotation for a playful swirl effect
                val infiniteTransition = rememberInfiniteTransition()
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                ColorResultBox(color = animatedResultColor, rotation = rotation)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                firstColor = null
                secondColor = null
                resultColor = null
            }) {
                Text("Resetează")
            }
        }
    }
}

/**
 * Simple composable representing a coloured droplet.  Uses a circular shape and
 * calls the supplied callback when tapped.  The size and padding are kept
 * generous to support uncoordinated fingers, following UX guidelines for
 * preschool children.
 */
@Composable
fun ColorDroplet(color: Color, onTap: () -> Unit) {
    // Local pressed state to trigger a bounce animation on tap
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 1.2f else 1f)
    // When the droplet is tapped, set pressed to true and invoke onTap; reset with a delay
    LaunchedEffect(pressed) {
        if (pressed) {
            // Keep the droplet enlarged briefly
            delay(200)
            pressed = false
        }
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color)
            .padding(8.dp)
            .clip(CircleShape)
            .background(color)
            .clickableNoRipple {
                pressed = true
                onTap()
            },
    )
}

/**
 * Displays the mixed colour result as a large circle.
 */
@Composable
fun ColorResultBox(color: Color, rotation: Float) {
    // Apply rotation to create a swirling effect while blending colours
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(120.dp)
            .graphicsLayer { rotationZ = rotation }
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        // Optionally display nothing inside; the swirling colour itself is eye‑catching
    }
}

/**
 * Custom modifier to make clickable areas without default ripple to avoid
 * distractions for very young users.  Compose’s default ripple may be too
 * subtle or confusing for toddlers; a simple callback suffices.
 */
@Composable
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) { onClick() }