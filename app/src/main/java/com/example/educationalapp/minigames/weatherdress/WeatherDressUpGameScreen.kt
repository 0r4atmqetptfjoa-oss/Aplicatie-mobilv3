package com.example.educationalapp.minigames.weatherdress

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

// Additional imports for premium animations
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.tween

/**
 * Compose screen for the Weather Dress‑Up mini‑game.  Children select a
 * weather condition and then choose the appropriate clothing items.  A
 * simple matching mechanism provides positive feedback when the correct
 * selections are made.
 */
@Composable
fun WeatherDressUpGameScreen(viewModel: WeatherDressUpGameViewModel) {
    var selectedWeather by remember { mutableStateOf<WeatherType?>(null) }
    var clothingMatches by remember { mutableStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    // Animate the background colour based on selected weather
    val bgColor by animateColorAsState(
        targetValue = when (selectedWeather) {
            WeatherType.SUNNY -> Color(0xFFFFF9C4) // light yellow
            WeatherType.RAINY -> Color(0xFFE3F2FD) // very light blue
            WeatherType.SNOWY -> Color(0xFFF0F0F0) // light grey/white
            WeatherType.WINDY -> Color(0xFFE0F7FA) // very light teal
            null -> MaterialTheme.colorScheme.background
        }
    )

    Surface(modifier = Modifier.fillMaxSize(), color = bgColor) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Alege vremea și îmbracă personajul!",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(16.dp))
            // Weather selection row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                viewModel.weathers.forEach { weather ->
                    val isSelected = selectedWeather == weather
                    WeatherButton(weather = weather, selected = isSelected) {
                        selectedWeather = weather
                        clothingMatches = 0
                        feedbackMessage = null
                    }
                }
            }
            Spacer(modifier = Modifier.size(24.dp))
            // Clothing selection row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                viewModel.clothes.forEach { clothing ->
                    ClothingButton(clothing = clothing) {
                        if (selectedWeather != null) {
                            val correct = viewModel.matchClothing(selectedWeather!!, clothing)
                            if (correct) {
                                clothingMatches++
                                if (clothingMatches >= viewModel.requiredMatches) {
                                    feedbackMessage = "Perfect!"
                                }
                            } else {
                                feedbackMessage = "Nu este potrivit!"
                            }
                        }
                    }
                }
            }
            feedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.size(16.dp))
                Text(msg, color = if (msg.contains("Perfect")) Color.Green else Color.Red)
            }
        }
    }
}

@Composable
fun WeatherButton(weather: WeatherType, selected: Boolean, onSelect: () -> Unit) {
    val baseColor = when (weather) {
        WeatherType.SUNNY -> Color.Yellow
        WeatherType.RAINY -> Color(0xFF87CEFA) // light blue
        WeatherType.SNOWY -> Color.White
        WeatherType.WINDY -> Color.LightGray
    }
    // Animate scale and border colour when selected
    val scale by animateFloatAsState(targetValue = if (selected) 1.2f else 1f)
    val borderColor by animateColorAsState(targetValue = if (selected) Color(0xFFFFC107) else Color.Black)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(baseColor)
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        Text(weather.label, color = Color.Black)
    }
}

@Composable
fun ClothingButton(clothing: ClothingType, onClick: () -> Unit) {
    val color = when (clothing) {
        ClothingType.SUNGLASSES -> Color(0xFFFFC107) // amber
        ClothingType.UMBRELLA -> Color(0xFF00BFFF) // deep sky blue
        ClothingType.COAT -> Color(0xFF8B4513) // brown
        ClothingType.HAT -> Color(0xFFCD5C5C) // salmon
    }
    // Bounce animation on tap
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(targetValue = if (pressed) 1.15f else 1f, animationSpec = tween(durationMillis = 200))
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
            .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
            .clickable {
                pressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(clothing.label, color = Color.Black)
    }
}