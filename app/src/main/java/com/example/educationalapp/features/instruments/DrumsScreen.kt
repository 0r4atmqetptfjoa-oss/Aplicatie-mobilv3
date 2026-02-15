package com.example.educationalapp.features.instruments

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.educationalapp.fx.ParticleController

@Composable
fun DrumsScreen(
    keyBitmaps: List<ImageBitmap?>,
    players: List<Player>,
    particles: ParticleController
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        // =====================================================================
        // GHID DE MODIFICARE TOBE
        // =====================================================================
        // x, y: Poziția centrului tobei (0.0 - 1.0).
        // widthScale / heightScale: Cât de MARE e toba.
        //   - 1.0 = Mărime normală.
        //   - 1.5 = Cu 50% mai mare (Gigantică).
        //   - 0.8 = Mai mică.
        // Exemplu: Vrei Toba Mare (Kick) mai imensă? Pune 2.0 la scale.
        // =====================================================================

        val drumConfigs = remember {
            listOf(
                PosConfig(x = 0.20f, y = 0.25f, widthScale = 1.2f, heightScale = 1.2f), // 1. Crash (Cinel Stânga)
                PosConfig(x = 0.80f, y = 0.25f, widthScale = 1.3f, heightScale = 1.3f), // 2. Ride (Cinel Dreapta)
                PosConfig(x = 0.12f, y = 0.50f, widthScale = 1.0f, heightScale = 1.0f), // 3. Hi-Hat (Lateral)
                PosConfig(x = 0.38f, y = 0.35f, widthScale = 0.9f, heightScale = 0.9f), // 4. Tom 1 (Mic)
                PosConfig(x = 0.62f, y = 0.35f, widthScale = 0.9f, heightScale = 0.9f), // 5. Tom 2 (Mic)
                PosConfig(x = 0.30f, y = 0.60f, widthScale = 1.1f, heightScale = 1.1f), // 6. Snare (Premier)
                PosConfig(x = 0.70f, y = 0.60f, widthScale = 1.2f, heightScale = 1.2f), // 7. Floor Tom (Cazan)
                PosConfig(x = 0.50f, y = 0.75f, widthScale = 1.5f, heightScale = 1.5f)  // 8. Kick (Toba Mare)
            )
        }

        val hitAndRenderData = remember(widthPx, heightPx) {
            drumConfigs.mapIndexed { index, config ->
                val baseSize = 120.dp
                val w = with(density) { (baseSize * config.widthScale).toPx() }
                val h = with(density) { (baseSize * config.heightScale).toPx() }
                val x = (config.x * widthPx) - (w / 2)
                val y = (config.y * heightPx) - (h / 2)
                Triple(index, Rect(x, y, x + w, y + h), config)
            }
        }

        GenericMultiTouchController(hitAndRenderData.map { RectHitArea(it.first, it.second) }, { i -> if(i < players.size) playNote(players[i], particles, hitAndRenderData[i].second.center) }) { pressedIds ->
            hitAndRenderData.forEach { (index, rect, _) ->
                if (index < keyBitmaps.size) keyBitmaps[index]?.let { btm -> InstrumentElement(btm, Modifier.offset { IntOffset(rect.left.toInt(), rect.top.toInt()) }.size(with(density){rect.width.toDp()}, with(density){rect.height.toDp()}), pressedIds.contains(index)) }
            }
        }
    }
}