package com.example.educationalapp.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp // <--- ACESTA LIPSEA
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    var color: Color,
    var size: Float,
    var rotation: Float,
    var rotationSpeed: Float,
    var shape: Int // 0 = Circle, 1 = Rect
)

@Composable
fun PremiumConfetti(
    modifier: Modifier = Modifier,
    durationMillis: Long = 2500
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    
    // Convertim DP în PX corect
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    val colors = listOf(
        Color(0xFFFF1744), Color(0xFF00E676), Color(0xFF2979FF),
        Color(0xFFFFEA00), Color(0xFFAA00FF), Color(0xFFFF9100)
    )

    val particles = remember {
        List(150) {
            Particle(
                x = Random.nextFloat() * screenWidth,
                y = -Random.nextFloat() * screenHeight * 0.5f,
                velocityX = (Random.nextFloat() - 0.5f) * 15f,
                velocityY = Random.nextFloat() * 10f + 5f,
                color = colors.random(),
                size = Random.nextFloat() * 20f + 10f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                shape = Random.nextInt(2)
            )
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        val startTime = System.nanoTime()
        while (isActive) {
            val nanos = System.nanoTime() - startTime
            val millis = nanos / 1_000_000
            if (millis > durationMillis) break
            time = millis
            particles.forEach { p ->
                p.x += p.velocityX
                p.y += p.velocityY
                p.velocityY += 0.5f
                p.rotation += p.rotationSpeed
                p.velocityX *= 0.98f 
            }
            delay(16)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            if (p.y < size.height + 50 && p.y > -500) {
                withTransform({
                    rotate(p.rotation, pivot = Offset(p.x, p.y))
                    translate(p.x, p.y)
                }) {
                    if (p.shape == 0) {
                        drawCircle(color = p.color, radius = p.size / 2)
                    } else {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(-p.size / 2, -p.size / 2),
                            size = Size(p.size, p.size)
                        )
                    }
                }
            }
        }
    }
}