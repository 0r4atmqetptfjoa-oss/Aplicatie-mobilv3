package com.example.educationalapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.educationalapp.fx.AssetImage
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold

/**
 * Simple gallery screen for instrument-themed assets.
 */
@Composable
fun InstrumentScreen(navController: NavController) {
    val particles = ParticleController() // no effects needed, but keeps style consistent

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_game_instruments,
        hud = GameHudState(title = "Instrumente", score = 0, levelLabel = "Galerie", starCount = 0),
        onBack = { navController.popBackStack() },
        particleController = particles
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Text(
                text = "Descoperă instrumentele!",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GalleryCard(name = "alphabet_x_xilofon", label = "Xilofon", modifier = Modifier.weight(1f))
                GalleryCard(name = "icon_game_instruments", label = "Arcadă", modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GalleryCard(name = "shape_circle_donut", label = "Tobă", modifier = Modifier.weight(1f))
                GalleryCard(name = "shape_rect_book", label = "Clape", modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = { navController.popBackStack() }) { Text("Înapoi") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GalleryCard(name: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AssetImage(drawableName = name, modifier = Modifier.size(110.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White, fontWeight = FontWeight.Black)
    }
}
