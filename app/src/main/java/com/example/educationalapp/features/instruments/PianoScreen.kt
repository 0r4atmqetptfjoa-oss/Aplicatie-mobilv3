package com.example.educationalapp.features.instruments

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController

@Composable
fun PianoScreen(
    keyBitmaps: List<ImageBitmap?>,
    players: List<Player>,
    overlay: ImageBitmap?,
    particles: ParticleController
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // =====================================================================
        // GHID DE MODIFICARE PIAN
        // =====================================================================
        val keyHeight = 150.dp     // Cât de înalte sunt clapele
        val paddingStart = 30.dp   // Cât spațiu lași în stânga
        val paddingBottom = 50.dp  // Cât spațiu lași jos
        val spacing = 4.dp         // Spațiul dintre clape
        // =====================================================================
        
        val hitAndRenderData = remember(widthPx, heightPx) {
            val totalSpacing = with(density) { (spacing * (keyBitmaps.size - 1)).toPx() }
            val availableWidth = widthPx - with(density) { (paddingStart * 2).toPx() } - totalSpacing
            val keyWidth = availableWidth / keyBitmaps.size
            val keyHeightPx = with(density) { keyHeight.toPx() }
            val startX = with(density) { paddingStart.toPx() }
            val startY = heightPx - with(density) { paddingBottom.toPx() } - keyHeightPx

            keyBitmaps.mapIndexed { index, _ ->
                val x = startX + index * (keyWidth + with(density) { spacing.toPx() })
                Triple(index, Rect(x, startY, x + keyWidth, startY + keyHeightPx), Unit)
            }
        }

        GenericMultiTouchController(hitAndRenderData.map { RectHitArea(it.first, it.second) }, { i -> if (i < players.size) playNote(players[i], particles, hitAndRenderData[i].second.bottomCenter) }) { pressedIds ->
            hitAndRenderData.forEach { (index, rect, _) ->
                keyBitmaps.getOrNull(index)?.let { btm -> InstrumentElement(btm, Modifier.offset { IntOffset(rect.left.toInt(), rect.top.toInt()) }.size(with(density){rect.width.toDp()}, with(density){rect.height.toDp()}), pressedIds.contains(index)) }
            }
            overlay?.let {
                val overlayH = 30.dp
                val overlayY = hitAndRenderData[0].second.top - with(density){ overlayH.toPx() }
                Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxWidth().height(overlayH).offset { IntOffset(0, overlayY.toInt()) }, contentScale = ContentScale.FillWidth)
            }
        }
    }
}