package com.example.educationalapp.features.instruments

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController

@Composable
fun SaxophoneScreen(
    keyBitmaps: List<ImageBitmap?>,
    players: List<Player>,
    particles: ParticleController
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // =====================================================================
        // GHID DE MODIFICARE BUTOANE SAXOFON
        // =====================================================================
        // x: 0.0 (Stânga) -> 1.0 (Dreapta). Ex: 0.5 este fix în mijloc.
        // y: 0.0 (Sus)    -> 1.0 (Jos). Ex: 0.9 este foarte jos.
        // Dacă un buton nu pică pe gaură, modifică ușor X sau Y.
        // =====================================================================

        val btnConfigs = remember {
            listOf(
                PosConfig(x = 0.36f, y = 0.28f), // Buton 1 (Cel mai de sus)
                PosConfig(x = 0.35f, y = 0.36f),
                PosConfig(x = 0.35f, y = 0.44f),
                PosConfig(x = 0.36f, y = 0.52f),
                PosConfig(x = 0.38f, y = 0.60f),
                PosConfig(x = 0.42f, y = 0.67f), // Aici începe curbura
                PosConfig(x = 0.48f, y = 0.73f),
                PosConfig(x = 0.55f, y = 0.76f)  // Ultimul buton (jos la pâlnie)
            )
        }

        val hitAndRenderData = remember(widthPx, heightPx) {
            btnConfigs.mapIndexed { index, config ->
                val sizePx = with(density) { 50.dp.toPx() } // Mărimea butonului (fixă)
                val x = (config.x * widthPx) - (sizePx / 2)
                val y = (config.y * heightPx) - (sizePx / 2)
                Triple(index, Rect(x, y, x + sizePx, y + sizePx), config)
            }
        }

        GenericMultiTouchController(hitAndRenderData.map { RectHitArea(it.first, it.second) }, { i -> if(i < players.size) playNote(players[i], particles, hitAndRenderData[i].second.center) }) { pressedIds ->
            hitAndRenderData.forEach { (index, rect, _) ->
                if (index < keyBitmaps.size) keyBitmaps[index]?.let { btm -> InstrumentElement(btm, Modifier.offset { IntOffset(rect.left.toInt(), rect.top.toInt()) }.size(50.dp), pressedIds.contains(index)) }
            }
            
            // Desenăm Ochii (Parametri: width, height, X_Ratio, Y_Ratio)
            // Modifică 0.31f și 0.13f ca să muți ochii
            AnimatedSaxEyes(pressedIds.isNotEmpty(), widthPx, heightPx, 0.31f, 0.13f)
        }
    }
}

@Composable
private fun AnimatedSaxEyes(isPlaying: Boolean, parentWidth: Float, parentHeight: Float, centerXRatio: Float, centerYRatio: Float) {
    val eyeCenterX = parentWidth * centerXRatio
    val eyeCenterY = parentHeight * centerYRatio
    val bounceAnim = rememberInfiniteTransition(label = "eyes")
    val bounceOffset by bounceAnim.animateFloat(
        initialValue = 0f, targetValue = if (isPlaying) -10f else 0f,
        animationSpec = infiniteRepeatable(tween(150, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "bounce"
    )
    Row(
        modifier = Modifier.offset { IntOffset((eyeCenterX - 40f).toInt(), (eyeCenterY + bounceOffset).toInt()) },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(2) { Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.White).padding(4.dp), contentAlignment = Alignment.BottomCenter) { Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.Black)) } }
    }
}