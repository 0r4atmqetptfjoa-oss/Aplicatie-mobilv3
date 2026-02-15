package com.example.educationalapp.features.instruments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// Clasa pentru reglaje individuale
data class StringAdjustment(
    val moveX: Float = 0f,      // Mută orizontal (+ Dreapta, - Stânga)
    val moveY: Float = 0f,      // Mută vertical (+ Jos, - Sus)
    val rotation: Float = 0f,   // Grade rotire (+ Dreapta, - Stânga)
    val scale: Float = 1.0f     // Lungime (1.0 = NORMAL). ATENȚIE: 0.0 = DISPARE!
)

@Composable
fun GuitarScreen(
    keyBitmaps: List<ImageBitmap?>,
    players: List<Player>,
    particles: ParticleController
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // =====================================================================
        // 🎛️ ZONA DE CONTROL TOTAL (AICI FACI MAGIA)
        // =====================================================================

        // 1. DEBUG (PUNCTE ROȘII - Pune 'true' să vezi capetele, 'false' la final)
        val showDebugPoints = false

        // 2. REGLAJE GLOBALE (Afectează TOATE corzile deodată)
        val globalMoveX = 0.0f  
        val globalOffsetY = 0.0f

        // Pozițiile de bază (Nu le schimba dacă vrei să rămână pe capul chitarei)
        val bridgeX = 0.10f + globalMoveX
        val headX = 0.33f + globalMoveX

        // ---------------------------------------------------------------------
        // 3. REGLAJE FINE PER COARDĂ (INDIVIDUAL)
        // ---------------------------------------------------------------------
        // ATENȚIE MARE LA VALORI:
        //
        // moveX (Mutare Stânga/Dreapta): 
        //    0.0f  = Nu muta.
        //    0.02f = Puțin la DREAPTA.
        //   -0.02f = Puțin la STÂNGA.
        //    (NU pune numere mari gen 50, că zboară de pe ecran!)
        //
        // moveY (Mutare Sus/Jos):
        //    0.0f  = Nu muta.
        //    0.02f = Puțin mai JOS.
        //   -0.02f = Puțin mai SUS.
        //
        // rotation (Rotire):
        //    0.0f  = Drept.
        //    5.0f  = Rotește 5 grade (sens ceas).
        //   -5.0f  = Rotește 5 grade (invers ceas).
        //
        // scale (Lungime/Scalare):
        //    1.0f  = LUNGIME NORMALĂ (NU PUNE 0.0 AICI!)
        //    1.1f  = Mai lungă cu 10%.
        //    0.9f  = Mai scurtă cu 10%.

        val adjustments = remember {
            listOf(
                // --- COARDA 1 (SUS - Verde) ---
                StringAdjustment(
                    moveX = 0.0f,     
                    moveY = 0.0f,     
                    rotation = 0.0f,  
                    scale = 1.0f      // <--- Lasă 1.0f pentru normal!
                ),

                // --- COARDA 2 ---
                StringAdjustment(
                    moveX = 0.0f, 
                    moveY = 0.0f, 
                    rotation = 0.0f, 
                    scale = 1.0f
                ),

                // --- COARDA 3 ---
                StringAdjustment(
                    moveX = 0.0f, 
                    moveY = 0.0f, 
                    rotation = 0.0f, 
                    scale = 1.0f
                ),

                // --- COARDA 4 ---
                StringAdjustment(
                    moveX = 0.0f, 
                    moveY = 0.0f, 
                    rotation = 0.0f, 
                    scale = 1.0f
                ),

                // --- COARDA 5 ---
                StringAdjustment(
                    moveX = 0.0f, 
                    moveY = 0.0f, 
                    rotation = 0.0f, 
                    scale = 1.0f
                ),

                // Coarda 6 – reglaje implicite (fără mutare/rotire/scalare)
                StringAdjustment(
                    moveX = 0.0f,
                    moveY = 0.0f,
                    rotation = 0.0f,
                    scale = 1.0f
                )
            )
        }

        // =====================================================================
        // CONFIGURARE DE BAZĂ (POZIȚII ORIGINALE)
        // =====================================================================
        val baseConfigs = remember {
            listOf(
                // Poziționările pentru corzi sunt definite explicit pe baza bulinelor verzi din imagine.
                // Fiecare pereche (startX, startY) corespunde unei buline verzi din stânga,
                // iar (endX, endY) corespunde bulinei verzi de pe aceeași linie din dreapta.
                // Valorile sunt relative la dimensiunea containerului (0f = marginea de sus, 1f = marginea de jos).
                // Dacă dorești să ajustezi manual poziția unei corzi, modifică valorile de mai jos sau
                // folosește StringAdjustment pentru deplasări mici.

                // Coarda 1 (sus) – bulină stângă: (0.1073, 0.4611), bulină dreaptă: (0.3021, 0.4111)
                StringConfig(startX = 0.1073f + globalMoveX, startY = 0.4611f + globalOffsetY, endX = 0.3021f + globalMoveX, endY = 0.4111f + globalOffsetY, thickness = 16.dp),
                // Coarda 2 – bulină stângă: (0.1125, 0.5222), bulină dreaptă: (0.3063, 0.4685)
                StringConfig(startX = 0.1125f + globalMoveX, startY = 0.5222f + globalOffsetY, endX = 0.3063f + globalMoveX, endY = 0.4685f + globalOffsetY, thickness = 15.dp),
                // Coarda 3 – bulină stângă: (0.1177, 0.5833), bulină dreaptă: (0.3125, 0.5259)
                StringConfig(startX = 0.1177f + globalMoveX, startY = 0.5833f + globalOffsetY, endX = 0.3125f + globalMoveX, endY = 0.5259f + globalOffsetY, thickness = 14.dp),
                // Coarda 4 – bulină stângă: (0.1229, 0.6463), bulină dreaptă: (0.3177, 0.5852)
                StringConfig(startX = 0.1229f + globalMoveX, startY = 0.6463f + globalOffsetY, endX = 0.3177f + globalMoveX, endY = 0.5852f + globalOffsetY, thickness = 14.dp),
                // Coarda 5 – bulină stângă: (0.1281, 0.7074), bulină dreaptă: (0.3229, 0.6481)
                StringConfig(startX = 0.1281f + globalMoveX, startY = 0.7074f + globalOffsetY, endX = 0.3229f + globalMoveX, endY = 0.6481f + globalOffsetY, thickness = 13.dp),
                // Coarda 6 (jos) – bulină stângă: (0.1333, 0.7704), bulină dreaptă: (0.3281, 0.7111)
                StringConfig(startX = 0.1333f + globalMoveX, startY = 0.7704f + globalOffsetY, endX = 0.3281f + globalMoveX, endY = 0.7111f + globalOffsetY, thickness = 12.dp)
            )
        }

        // =====================================================================
        // CALCUL MATEMATIC (APLICAREA AJUSTĂRILOR)
        // =====================================================================
        val finalStrings = remember(widthPx, heightPx, baseConfigs, adjustments) {
            baseConfigs.mapIndexed { index, config ->
                val adj = adjustments.getOrElse(index) { StringAdjustment() }

                // 1. Coordonate inițiale în pixeli
                var start = Offset(config.startX * widthPx, config.startY * heightPx)
                var end = Offset(config.endX * widthPx, config.endY * heightPx)

                // 2. Calcul Centru Coardă (pentru rotire și scalare)
                val center = (start + end) / 2f

                // 3. Aplicare SCALARE (Lungime)
                // PROTECȚIE: Dacă ai pus 0.0f din greșeală, folosim 1.0f ca să nu dispară!
                val safeScale = if (adj.scale == 0.0f) 1.0f else adj.scale
                
                if (safeScale != 1.0f) {
                    start = center + (start - center) * safeScale
                    end = center + (end - center) * safeScale
                }

                // 4. Aplicare ROTIRE
                if (adj.rotation != 0f) {
                    val rad = (adj.rotation * PI / 180.0).toFloat()
                    val c = cos(rad)
                    val s = sin(rad)
                    
                    fun rot(p: Offset): Offset {
                        val dx = p.x - center.x
                        val dy = p.y - center.y
                        return Offset(center.x + (dx * c - dy * s), center.y + (dx * s + dy * c))
                    }
                    start = rot(start)
                    end = rot(end)
                }

                // 5. Aplicare TRANSLATIE (Mutare Sus/Jos/Stânga/Dreapta)
                val moveOffset = Offset(adj.moveX * widthPx, adj.moveY * heightPx)
                start += moveOffset
                end += moveOffset

                Triple(start, end, config)
            }
        }

        // =====================================================================
        // LOGICA DE DESENARE ȘI ATINGERE
        // =====================================================================
        
        val hitAreas = remember(finalStrings, density) {
            finalStrings.mapIndexed { index, (pStart, pEnd, config) ->
                LineHitArea(
                    id = index,
                    start = pStart,
                    end = pEnd,
                    thicknessPx = with(density) { config.thickness.toPx() }
                )
            }
        }

        GenericMultiTouchController(
            hitAreas = hitAreas,
            onHit = { index ->
                if (index < players.size) {
                    val (pStart, pEnd, _) = finalStrings[index]
                    playNote(players[index], particles, (pStart + pEnd) / 2f)
                }
            }
        ) { pressedIds ->
            finalStrings.forEachIndexed { index, (pStart, pEnd, config) ->
                if (index < keyBitmaps.size) {
                    InstrumentString(
                        bitmap = keyBitmaps[index],
                        start = pStart,
                        end = pEnd,
                        thickness = config.thickness,
                        isPressed = pressedIds.contains(index)
                    )
                }
            }

            if (showDebugPoints) {
                finalStrings.forEach { (pStart, pEnd, _) ->
                    DebugPoint(pStart.x, pStart.y)
                    DebugPoint(pEnd.x, pEnd.y)
                }
            }
        }
    }
}

@Composable
fun DebugPoint(x: Float, y: Float) {
    Box(
        modifier = Modifier
            .offset { IntOffset((x - 15).toInt(), (y - 15).toInt()) }
            .size(30.dp)
            .background(Color.Red.copy(alpha = 0.8f), CircleShape)
    )
}