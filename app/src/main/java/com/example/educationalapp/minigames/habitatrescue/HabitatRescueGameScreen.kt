package com.example.educationalapp.minigames.habitatrescue

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.animation.animateColorAsState

/**
 * Compose screen for the Animal Habitat Rescue mini‑game.  Presents a set of
 * animals and habitats; the child must match each animal to its correct
 * habitat.  We use a simple two‑step interaction: select an animal, then
 * select the habitat.  Correct matches show positive feedback, incorrect
 * matches prompt to try again.
 */
@Composable
fun HabitatRescueGameScreen(viewModel: HabitatRescueGameViewModel) {
    var selectedAnimal by remember { mutableStateOf<AnimalType?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    // Track the animal that should animate when matched
    var animatingAnimal by remember { mutableStateOf<AnimalType?>(null) }
    // Track which habitat should highlight when a correct match is made
    var highlightedHabitat by remember { mutableStateOf<HabitatType?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Găsește habitatul potrivit pentru fiecare animal!",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(16.dp))
            // Animal buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                viewModel.animals.forEach { animal ->
                    // Determine scale animation for this animal
                    val targetScale = if (animatingAnimal == animal) 1.3f else 1f
                    val scale by animateFloatAsState(targetValue = targetScale)
                    AnimalButton(animal = animal, scale = scale) {
                        selectedAnimal = animal
                        feedbackMessage = null
                    }
                }
            }
            Spacer(modifier = Modifier.size(24.dp))
            // Habitat buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                viewModel.habitats.forEach { habitat ->
                    val isHighlighted = highlightedHabitat == habitat
                    HabitatButton(habitat = habitat, highlighted = isHighlighted) {
                        if (selectedAnimal != null) {
                            val correct = viewModel.submitMatch(selectedAnimal!!, habitat)
                            if (correct) {
                                feedbackMessage = "Excelent!"
                                animatingAnimal = selectedAnimal
                                highlightedHabitat = habitat
                            } else {
                                feedbackMessage = "Încearcă din nou!"
                            }
                            // reset selection after attempt
                            selectedAnimal = null
                        }
                    }
                }
            }
            feedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.size(16.dp))
                Text(text = msg, color = Color.Green)
            }

            // Reset animation states after a short delay
            animatingAnimal?.let { aa ->
                LaunchedEffect(aa) {
                    delay(600)
                    animatingAnimal = null
                }
            }
            highlightedHabitat?.let { hh ->
                LaunchedEffect(hh) {
                    delay(600)
                    highlightedHabitat = null
                }
            }
        }
    }
}

@Composable
fun AnimalButton(animal: AnimalType, scale: Float = 1f, onSelect: () -> Unit) {
    val color = when (animal) {
        AnimalType.FISH -> Color.Cyan
        AnimalType.SHEEP -> Color.LightGray
        AnimalType.BEAR -> Color(0xFFA0522D) // brown
        AnimalType.PENGUIN -> Color.DarkGray
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(72.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(animal.label, color = Color.Black)
    }
}

@Composable
fun HabitatButton(habitat: HabitatType, highlighted: Boolean = false, onClick: () -> Unit) {
    val baseColor = when (habitat) {
        HabitatType.SEA -> Color(0xFF64B5F6) // medium blue
        HabitatType.FARM -> Color(0xFF81C784) // medium green
        HabitatType.FOREST -> Color(0xFF4CAF50) // forest green
        HabitatType.ICE -> Color(0xFFB3E5FC) // light blue for ice
    }
    // Animate scale and border colour when highlighted
    val scale by animateFloatAsState(targetValue = if (highlighted) 1.1f else 1f)
    val borderColor by animateColorAsState(targetValue = if (highlighted) Color.Yellow else Color.Black)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(100.dp, 80.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(baseColor)
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(habitat.label, color = Color.White)
    }
}