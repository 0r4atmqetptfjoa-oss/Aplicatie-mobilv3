package com.example.educationalapp.features.intro

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManagerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * INTRO LEGENDAR 2026: "Nașterea unei Stele"
 * O secvență cinematografică în 5 acte realizată complet în Kotlin Compose.
 */
@Composable
fun IntroScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val soundManager = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, SoundManagerEntryPoint::class.java).soundManager()
    }

    var stage by remember { mutableIntStateOf(1) } // 1: Dust, 2: Vortex, 3: Ignition, 4: Reveal, 5: Done
    val mainProgress = remember { Animatable(0f) }
    
    val particles = remember { List(120) { IntroParticle() } }

    LaunchedEffect(Unit) {
        // ACTUL 1: Vidul Cosmic & Praful Stelat
        soundManager.playSound(R.raw.math_bg_music) // Muzică ambientală misterioasă
        mainProgress.animateTo(0.3f, tween(2000, easing = LinearEasing))
        
        // ACTUL 2: Atragere Gravitațională (Vortex)
        stage = 2
        mainProgress.animateTo(0.6f, tween(2000, easing = FastOutSlowInEasing))
        
        // ACTUL 3: IGNITION (Explozia Supernovei)
        stage = 3
        soundManager.playSound(R.raw.sfx_bubble_pop) // Sunet de explozie/impact
        mainProgress.animateTo(0.8f, tween(800, easing = LinearEasing))
        
        // ACTUL 4: REVELAȚIA (Logo-ul apare)
        stage = 4
        soundManager.playClickIconSound() // Sunet "Wow"
        mainProgress.animateTo(1.0f, tween(3000, easing = EaseOutBack))
        
        // ACTUL 5: TRANZIȚIA FINALĂ
        delay(1000)
        stage = 5
        delay(1000)
        onDone()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. NEBULOASA DE FUNDAL (Dinamica)
        NebulaBackground(mainProgress.value)

        // 2. MOTORUL DE PARTICULE (Legendar)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val p = mainProgress.value

            particles.forEach { part ->
                val (currentPos, alpha) = when(stage) {
                    1 -> { // Dust
                        val dist = part.initialDist * (1f + p * 0.2f)
                        val angle = part.angle + p * 0.5f
                        Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist) to (p * 3).coerceIn(0f, 0.6f)
                    }
                    2 -> { // Vortex
                        val t = (p - 0.3f) / 0.3f
                        val dist = part.initialDist * (1f - t) + 50f * t
                        val angle = part.angle + p * 8f
                        Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist) to 0.8f
                    }
                    3 -> { // Ignition
                        val t = (p - 0.6f) / 0.2f
                        val dist = 50f + part.explodeDist * t
                        val angle = part.angle + p * 12f
                        Offset(center.x + cos(angle) * dist, center.y + sin(angle) * dist) to (1f - t)
                    }
                    else -> Offset.Zero to 0f
                }

                if (stage < 4) {
                    drawCircle(
                        brush = Brush.radialGradient(listOf(part.color.copy(alpha = alpha), Color.Transparent)),
                        radius = part.size * (1f + p),
                        center = currentPos
                    )
                }
            }
            
            // 3. FLASH-UL EXPLOZIEI
            if (stage == 3) {
                val t = (p - 0.6f) / 0.2f
                drawCircle(
                    color = Color.White.copy(alpha = (1f - t).coerceIn(0f, 1f)),
                    radius = size.maxDimension * t * 1.5f,
                    center = center
                )
            }
        }

        // 4. LOGO-UL REVELAT (WOW EFFECT)
        if (stage >= 4) {
            val t = (mainProgress.value - 0.8f) / 0.2f
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    alpha = if (stage == 5) 1f - t else t
                    scaleX = 0.8f + t * 0.2f
                    scaleY = 0.8f + t * 0.2f
                }
            ) {
                // Aura Pulsatorie
                val infiniteTransition = rememberInfiniteTransition(label = "aura")
                val glowScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse)
                )
                
                Box(
                    modifier = Modifier
                        .size(400.dp)
                        .scale(glowScale)
                        .background(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)), CircleShape)
                )

                Image(
                    painter = painterResource(id = R.drawable.main_menu_title),
                    contentDescription = "Logo Legendar",
                    modifier = Modifier.width(450.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun NebulaBackground(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    val rot by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing))
    )

    Box(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rot }) {
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF311B92),
                    0.5f to Color(0xFF006064),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.3f, size.height * 0.4f),
                    radius = size.maxDimension * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFF4A148C),
                    0.6f to Color(0xFF880E4F),
                    1.0f to Color.Transparent,
                    center = Offset(size.width * 0.7f, size.height * 0.6f),
                    radius = size.maxDimension * 0.6f
                )
            )
        }
    }
}

private class IntroParticle {
    val angle = Random.nextFloat() * 2f * PI.toFloat()
    val initialDist = 600f + Random.nextFloat() * 1000f
    val explodeDist = 1500f + Random.nextFloat() * 1000f
    val size = 3f + Random.nextFloat() * 6f
    val color = listOf(
        Color(0xFFFFD700), Color(0xFF00E5FF), Color(0xFFFF4081), 
        Color(0xFF7C4DFF), Color.White
    ).random()
}
