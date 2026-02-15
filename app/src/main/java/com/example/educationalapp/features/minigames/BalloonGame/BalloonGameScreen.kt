package com.example.educationalapp.BalloonGame

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.educationalapp.R
import kotlinx.coroutines.isActive
import kotlin.math.roundToInt

@Composable
fun BalloonGameScreen(
    viewModel: BalloonViewModel = hiltViewModel(),
    onHome: () -> Unit
) {
    val config = LocalConfiguration.current
    val screenH = config.screenHeightDp.toFloat()
    val screenW = config.screenWidthDp.toFloat()

    LaunchedEffect(Unit) {
        var lastTime = withFrameNanos { it }
        while (isActive) {
            val currentTime = withFrameNanos { it }
            val dt = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime
            viewModel.updateGame(dt, screenH * 2.5f, screenW * 2.5f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // A. FUNDAL
        Image(
            painter = painterResource(id = R.drawable.bg_alphabet_sky),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // B. BALOANE
        viewModel.balloons.forEach { balloon ->
            if (!balloon.isPopped) {
                
                // --- MODIFICARE CRITICĂ PENTRU DIMENSIUNI ---
                // Conform index.htm:
                // Normal = 512x512 (Pătrat) -> Se vede bine la 130dp
                // Speciale (Inima, Stea, Luna, Cub) = 1400x764 (Late) -> Trebuie mult mai mari pe lățime
                
                val balloonSize = when (balloon.type) {
                    BalloonType.NORMAL -> 130.dp 
                    BalloonType.CLOUD -> 150.dp // Norul e 1024x1024, puțin mai mare
                    else -> 230.dp // Inima, Stea, Luna, Cub (le facem FOARTE late ca să aibă înălțime ok)
                }

                Image(
                    painter = painterResource(id = balloon.imageRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit, 
                    modifier = Modifier
                        .offset { IntOffset(balloon.currentX.roundToInt(), balloon.y.roundToInt()) }
                        .size(balloonSize) // Aplicăm dimensiunea calculată mai sus
                        .rotate(balloon.rotation)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.onBalloonTap(balloon) }
                )
            }
        }

        // C. PARTICULE
        viewModel.particles.forEach { p ->
            val particleSize = 35.dp 
            
            if (p.imageRes != null) {
                Image(
                    painter = painterResource(id = p.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .offset { IntOffset(p.currentX.roundToInt(), p.currentY.roundToInt()) }
                        .size(particleSize)
                        .rotate(p.rotation)
                        .alpha(p.alpha)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.vfx_star),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(p.color),
                    modifier = Modifier
                        .offset { IntOffset(p.currentX.roundToInt(), p.currentY.roundToInt()) }
                        .size(particleSize)
                        .rotate(p.rotation)
                        .alpha(p.alpha)
                )
            }
        }

        // D. HUD (SCOR)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ui_score_container),
                contentDescription = null,
                modifier = Modifier.size(140.dp, 70.dp)
            )
            Text(
                text = "${viewModel.score.value}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // E. HOME
        Image(
            painter = painterResource(id = R.drawable.ui_button_home),
            contentDescription = "Home",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(64.dp)
                .clickable { onHome() }
        )
    }
}