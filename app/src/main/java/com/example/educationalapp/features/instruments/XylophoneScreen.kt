package com.example.educationalapp.features.instruments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController

@Composable
fun XylophoneScreen(
    keyBitmaps: List<ImageBitmap?>,
    players: List<Player>,
    particles: ParticleController
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // =====================================================================
        // GHID DE MODIFICARE XILOFON
        // =====================================================================
        val maxBarHeight = 180.dp // Cât de înaltă e cea mai mare bară
        val paddingSide = 32.dp   // Spațiu lateral
        val spacing = 8.dp        // Spațiu între bare
        val growthFactor = 0.12f  // Cât de mult "scade" bara următoare. 
                                  // 0.12f înseamnă că fiecare bară e puțin diferită.
        // =====================================================================

        val hitAndRenderData = remember(widthPx, heightPx) {
            val totalSpacing = with(density) { (spacing * (keyBitmaps.size - 1)).toPx() }
            
            // REPARATIE MATEMATICĂ: Folosim map().sum() pentru a nu avea erori Float vs Double
            val totalWeights = keyBitmaps.indices.map { 1f + it * growthFactor }.sum()
            
            val availableWidth = widthPx - with(density) { (paddingSide * 2).toPx() } - totalSpacing
            val unitWidth = availableWidth / totalWeights
            var currentX = with(density) { paddingSide.toPx() }
            val centerY = heightPx / 2
            val maxH = with(density) { maxBarHeight.toPx() }

            keyBitmaps.mapIndexed { index, _ ->
                val weight = 1f + index * growthFactor
                val w = unitWidth * weight
                val x = currentX
                currentX += w + with(density) { spacing.toPx() }
                val rect = Rect(x, centerY - maxH/2, x + w, centerY + maxH/2)
                Triple(index, rect, Unit)
            }
        }
        
        Box(modifier = Modifier.fillMaxWidth().height(16.dp).align(androidx.compose.ui.Alignment.Center).background(Color(0xFF795548), RoundedCornerShape(4.dp)))

        GenericMultiTouchController(hitAndRenderData.map { RectHitArea(it.first, it.second) }, { i -> if (i < players.size) playNote(players[i], particles, hitAndRenderData[i].second.center) }) { pressedIds ->
            hitAndRenderData.forEach { (index, rect, _) ->
                keyBitmaps.getOrNull(index)?.let { btm -> InstrumentElement(btm, Modifier.offset { IntOffset(rect.left.toInt(), rect.top.toInt()) }.size(with(density){rect.width.toDp()}, with(density){rect.height.toDp()}), pressedIds.contains(index)) }
            }
        }
    }
}