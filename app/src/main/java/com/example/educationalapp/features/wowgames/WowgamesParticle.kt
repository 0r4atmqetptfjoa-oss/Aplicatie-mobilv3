package com.example.educationalapp.features.wowgames

import androidx.compose.ui.graphics.Color

// Model shared pentru particule (folosit de BuildFarmGame etc.)
data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var color: Color,
    var size: Float = 1f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f
)
