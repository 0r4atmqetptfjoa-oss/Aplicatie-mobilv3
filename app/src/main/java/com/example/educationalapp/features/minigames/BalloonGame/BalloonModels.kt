package com.example.educationalapp.BalloonGame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class BalloonType {
    NORMAL, HEART, STAR, MOON, CLOUD, CUBE
}

data class BalloonState(
    val id: Long,
    val imageRes: Int,
    val type: BalloonType,
    val speed: Float,
    val startX: Float,
    val amplitude: Float,
    val frequency: Float
) {
    var y by mutableFloatStateOf(0f)
    var currentX by mutableFloatStateOf(startX)
    
    var isPopped by mutableStateOf(false)
    var scale by mutableFloatStateOf(1f)
    var rotation by mutableFloatStateOf(0f)
}

data class PopParticle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    var velocityX: Float,
    var velocityY: Float,
    val color: Color,
    val imageRes: Int? = null
) {
    var alpha by mutableFloatStateOf(1f)
    var currentX by mutableFloatStateOf(startX)
    var currentY by mutableFloatStateOf(startY)
    var rotation by mutableFloatStateOf(0f)
}