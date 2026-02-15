package com.example.educationalapp.minigames.shapetrain

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
// Button is unused in this file; remove to avoid unused import
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
import androidx.compose.animation.core.animateDpAsState
// Additional animations
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.animation.animateColorAsState

/**
 * A simple Compose screen for the Shape Train mini‑game.  Children first
 * select a shape, then tap a wagon numbered 1‑4 to deliver it.  If the
 * selection is correct for the current assignment, a congratulatory
 * message is shown.  This skeleton omits drag‑and‑drop for simplicity and
 * focuses on a two‑step interaction suitable for preschoolers.
 */
@Composable
fun ShapeTrainGameScreen(viewModel: ShapeTrainGameViewModel) {
    // Current assignment index
    var assignmentIndex by remember { mutableStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    // Track which wagon should be highlighted after a correct match
    var highlightedWagon by remember { mutableStateOf<Int?>(null) }
    val assignment = viewModel.assignments.getOrNull(assignmentIndex)

    // Animate the horizontal offset of the train row.  Each wagon plus spacing
    // is approximated at 120.dp wide; adjust multiplier to suit your design.
    val trainOffset by animateDpAsState(targetValue = (assignmentIndex * 120).dp)

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ajută formele să urce în vagoane!",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(16.dp))
            // Display shapes row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShapeButton(shape = ShapeType.CIRCLE) { selectedShape ->
                    // Save the selected shape in viewModel state
                    viewModel.selectShape(selectedShape)
                    feedbackMessage = null
                }
                ShapeButton(shape = ShapeType.SQUARE) { selectedShape ->
                    viewModel.selectShape(selectedShape)
                    feedbackMessage = null
                }
                ShapeButton(shape = ShapeType.TRIANGLE) { selectedShape ->
                    viewModel.selectShape(selectedShape)
                    feedbackMessage = null
                }
                ShapeButton(shape = ShapeType.RECTANGLE) { selectedShape ->
                    viewModel.selectShape(selectedShape)
                    feedbackMessage = null
                }
            }
            Spacer(modifier = Modifier.size(32.dp))
            // Show wagons with numbers and animate their movement when a correct
            // match is made.  The row is offset to create a sliding train effect.
            Row(
                modifier = Modifier.offset(x = -trainOffset),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                (1..4).forEach { wagonNumber ->
                    val isHighlighted = highlightedWagon == wagonNumber
                    WagonButton(number = wagonNumber, highlighted = isHighlighted) {
                        if (assignment != null) {
                            val correct = viewModel.submitToWagon(wagonNumber)
                            feedbackMessage = if (correct) {
                                "Bravo!"
                            } else {
                                "Încearcă din nou!"
                            }
                            if (correct) {
                                // highlight the correct wagon and reset after delay
                                highlightedWagon = wagonNumber
                                assignmentIndex = (assignmentIndex + 1) % viewModel.assignments.size
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(24.dp))
            if (assignment != null) {
                Text("Selectează ${assignment.shape.label} pentru vagonul ${assignment.number}")
            }
            feedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = msg, color = Color.Green)
            }

            // Clear highlight after a brief period
            highlightedWagon?.let { hw ->
                LaunchedEffect(hw) {
                    delay(600)
                    highlightedWagon = null
                }
            }
        }
    }
}

/**
 * Composable representing a coloured shape button.  Each shape is given a
 * distinct colour for visual identification.
 */
@Composable
fun ShapeButton(shape: ShapeType, onSelect: (ShapeType) -> Unit) {
    val size = 64.dp
    val color = when (shape) {
        ShapeType.CIRCLE -> Color.Red
        ShapeType.SQUARE -> Color.Yellow
        ShapeType.TRIANGLE -> Color.Blue
        ShapeType.RECTANGLE -> Color.Green
    }
    val shapeModifier = when (shape) {
        ShapeType.CIRCLE -> Modifier.clip(CircleShape)
        ShapeType.SQUARE -> Modifier.clip(RoundedCornerShape(4.dp))
        ShapeType.TRIANGLE -> Modifier // triangle drawn via custom drawing would be more complex
        ShapeType.RECTANGLE -> Modifier.clip(RoundedCornerShape(4.dp))
    }
    // Local state for press animation
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 1.2f else 1f)
    // Reset pressed state after a short delay
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(200)
            pressed = false
        }
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .then(shapeModifier)
            .background(color)
            .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
            .clickable {
                pressed = true
                onSelect(shape)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(shape.label, color = Color.Black)
    }
}

/**
 * Composable representing a wagon with a number.  Uses a rounded rectangle to
 * suggest a carriage.
 */
@Composable
fun WagonButton(number: Int, highlighted: Boolean = false, onClick: () -> Unit) {
    // Animate background colour and scale when highlighted
    val bgColor by animateColorAsState(targetValue = if (highlighted) Color(0xFFFFF59D) else Color.LightGray)
    val scale by animateFloatAsState(targetValue = if (highlighted) 1.15f else 1f)
    val borderColor by animateColorAsState(targetValue = if (highlighted) Color(0xFFFFC107) else Color.DarkGray)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(80.dp, 60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(number.toString(), style = MaterialTheme.typography.titleLarge)
    }
}
