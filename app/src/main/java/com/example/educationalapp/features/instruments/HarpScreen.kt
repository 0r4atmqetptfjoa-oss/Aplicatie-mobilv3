package com.example.educationalapp.features.instruments

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

// =====================================================================
// 🎛️ CLASA PENTRU REGLAJE FINE (AM REDENUMIT-O CA SĂ NU SE LOVEASCĂ DE CHITARĂ)
// =====================================================================
data class HarpStringAdjustment(
    val moveX: Float = 0f,      // Mută Stânga (-) / Dreapta (+)
    val moveY: Float = 0f,      // Mută Sus (-) / Jos (+)
    val rotation: Float = 0f,   // Rotește în grade
    val scale: Float = 2.0f     // Scalare lungime (1.0 = Normal)
)

@Composable
fun HarpScreen(
    keyBitmaps: List<ImageBitmap?>, 
    players: List<Player>,          
    particles: ParticleController,
    maskBitmap: ImageBitmap? = null,
    modifier: Modifier = Modifier
) {
    // Folosim modifier-ul primit, dar ne asigurăm că umplem ecranul
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // =====================================================================
        // 🛠️ ZONA DE CONFIGURARE "CHIRURGICALĂ" - HARPĂ (VERTICALĂ)
        // =====================================================================

        // 1. VREI SĂ VEZI PUNCTELE ROȘII? (DEBUG)
        val showDebugPoints = false // Dezactivat implicit pentru build

        // 2. POZIȚIA GENERALĂ
        val globalMoveX = 0.0f
        val globalMoveY = 0.0f 

        // Lățimea vizuală a corzilor (tuburilor colorate).
        // Am redus grosimea pentru a face corzile mai subțiri, la cererea utilizatorului.
        // Utilizatorul a cerut în mod repetat corzi din ce în ce mai subțiri –
        // aici am setat grosimea la 8dp pentru un aspect fin.
        val harpStringThickness = 8.dp 

        // =====================================================================
        // 3. CONFIGURARE CELE 8 CORZI (De la Stânga la Dreapta)
        // =====================================================================
        
        val baseConfigs = remember {
            listOf(
                // Pozițiile corzilor preluate din bulinele verzi ale harpei (conform bkg_arpa.png).
                // Fiecare coardă pornește dintr-o bulină verde (startX, startY) și merge vertical
                // în jos până sub marginea inferioară a ecranului (endY = 1.1f), astfel încât
                // capătul de jos să nu fie vizibil.
                // 1. Roșu – bulină la (0.2854, 0.4907)
                StringConfig(
                    startX = 0.2854f + globalMoveX, startY = 0.4907f + globalMoveY,
                    endX = 0.2854f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 2. Portocaliu – bulină la (0.3323, 0.5)
                StringConfig(
                    startX = 0.3323f + globalMoveX, startY = 0.5f + globalMoveY,
                    endX = 0.3323f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 3. Galben – bulină la (0.3760, 0.5)
                StringConfig(
                    startX = 0.3760f + globalMoveX, startY = 0.5f + globalMoveY,
                    endX = 0.3760f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 4. Verde – bulină la (0.4292, 0.5)
                StringConfig(
                    startX = 0.4292f + globalMoveX, startY = 0.5f + globalMoveY,
                    endX = 0.4292f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 5. Cyan – bulină la (0.4760, 0.4795)
                StringConfig(
                    startX = 0.4760f + globalMoveX, startY = 0.4795f + globalMoveY,
                    endX = 0.4760f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 6. Albastru închis – bulină la (0.5281, 0.4515)
                StringConfig(
                    startX = 0.5281f + globalMoveX, startY = 0.4515f + globalMoveY,
                    endX = 0.5281f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 7. Mov – bulină la (0.5875, 0.3974)
                StringConfig(
                    startX = 0.5875f + globalMoveX, startY = 0.3974f + globalMoveY,
                    endX = 0.5875f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                ),
                // 8. Roz – bulină la (0.65, 0.3582)
                StringConfig(
                    startX = 0.65f + globalMoveX, startY = 0.3582f + globalMoveY,
                    endX = 0.65f + globalMoveX, endY = 1.1f + globalMoveY,
                    thickness = harpStringThickness
                )
            )
        }

        // ---------------------------------------------------------------------
        // 4. REGLAJE FINE 
        // ---------------------------------------------------------------------
        val adjustments = remember {
            List(8) { HarpStringAdjustment() } // Folosim noua clasă redenumită
        }

        // =====================================================================
        // MOTORUL MATEMATIC
        // =====================================================================
        
        val finalStrings = remember(widthPx, heightPx, baseConfigs, adjustments) {
            baseConfigs.mapIndexed { index, config ->
                val adj = adjustments.getOrElse(index) { HarpStringAdjustment() }

                // 1. Calcul puncte pixeli
                var start = Offset(config.startX * widthPx, config.startY * heightPx)
                var end = Offset(config.endX * widthPx, config.endY * heightPx)

                // 2. Centru
                val center = (start + end) / 2f

                // 3. Scalare
                val s = if (adj.scale == 0f) 1.0f else adj.scale
                if (s != 1.0f) {
                    start = center + (start - center) * s
                    end = center + (end - center) * s
                }

                // 4. Rotire
                if (adj.rotation != 0f) {
                    val rad = (adj.rotation * PI / 180.0).toFloat()
                    val c = cos(rad)
                    val sVal = sin(rad)
                    fun rot(p: Offset): Offset {
                        val dx = p.x - center.x
                        val dy = p.y - center.y
                        return Offset(center.x + (dx * c - dy * sVal), center.y + (dx * sVal + dy * c))
                    }
                    start = rot(start)
                    end = rot(end)
                }

                // 5. Mutare
                val move = Offset(adj.moveX * widthPx, adj.moveY * heightPx)
                start += move
                end += move

                Triple(start, end, config)
            }
        }

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
                    HarpDebugPoint(pStart.x, pStart.y) 
                    HarpDebugPoint(pEnd.x, pEnd.y)     
                }
            }

            // Rendăm masca peste corzi
            maskBitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

// Funcție privată și redenumită pentru siguranță maximă
@Composable
private fun HarpDebugPoint(x: Float, y: Float) {
    Box(
        modifier = Modifier
            .offset { IntOffset((x - 20).toInt(), (y - 20).toInt()) }
            .size(40.dp) 
            .background(Color.Red.copy(alpha = 0.5f), CircleShape)
    )
}