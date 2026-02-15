package com.example.educationalapp.features.wowgames

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- 1. AURA MAGICĂ (Lumina din spatele oului) ---
@Composable
fun MagicAura(
    color: Color,
    isActive: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    
    // Aura pulsează (se mărește și se micșorează)
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "aura_scale"
    )

    // Aura se rotește ușor
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "aura_rot"
    )

    if (isActive) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height - 300f) // Ajustat să fie în spatele oului
            
            withTransform({
                scale(scale, scale, center)
                rotate(rotation, center)
            }) {
                // Desenăm raze de lumină
                val rays = 12
                for (i in 0 until rays) {
                    val angle = (360f / rays) * i
                    val rad = angle * (PI / 180f).toFloat()
                    val start = center
                    val end = Offset(
                        center.x + cos(rad) * 400f,
                        center.y + sin(rad) * 400f
                    )
                    
                    drawLine(
                        color = color.copy(alpha = 0.3f),
                        start = start,
                        end = end,
                        strokeWidth = 40f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
                
                // Desenăm cercul central difuz
                drawCircle(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.6f), Color.Transparent),
                        center = center,
                        radius = 350f
                    ),
                    center = center,
                    radius = 350f
                )
            }
        }
    }
}

// --- 2. PARTICULE (Explozia la spargere) ---
data class MagicParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 -> 0.0
    var color: Color,
    var size: Float
)

@Composable
fun ParticleExplosion(
    trigger: Int, // Când se schimbă, declanșează explozia
    color: Color,
    originY: Float // De unde pleacă (înălțimea oului)
) {
    val particles = remember { mutableStateListOf<MagicParticle>() }

    // Logică de update (Game Loop)
    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos { 
                val dt = 0.016f // aprox 60fps
                val iterator = particles.iterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx * dt * 60f
                    p.y += p.vy * dt * 60f
                    p.vy += 0.5f * dt * 60f // Gravitație
                    p.life -= 1.5f * dt
                    
                    if (p.life <= 0) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    // Trigger explozie
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            repeat(40) {
                val angle = Random.nextFloat() * -PI.toFloat() // Doar în sus (semicerc)
                val speed = Random.nextFloat() * 15f + 5f
                particles.add(
                    MagicParticle(
                        x = 0f, // Relativ la centru canvas
                        y = originY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        life = 1f + Random.nextFloat(),
                        color = if (Random.nextBoolean()) color else Color.White,
                        size = Random.nextFloat() * 15f + 10f
                    )
                )
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                radius = p.size * p.life,
                center = Offset(centerX + p.x, p.y)
            )
        }
    }
}