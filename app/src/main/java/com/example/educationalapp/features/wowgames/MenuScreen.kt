package com.example.educationalapp.features.wowgames

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.educationalapp.R
import com.example.educationalapp.fx.AssetImage
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold

@Composable
fun MenuScreen(navController: NavHostController) {
    val particles = ParticleController()

    UltraGameScaffold(
        backgroundRes = R.drawable.bg_magic_forest,
        hud = GameHudState(title = "WOW Games", score = 0, levelLabel = "Alege aventura", starCount = 0),
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
                text = "Selectează un mini-joc WOW",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.10f))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WowGameCard(
                    title = "Alphabet Adventure",
                    subtitle = "Sari pe litere, colectează stele!",
                    icon = "ic_alphabet_launcher",
                    onClick = { navController.navigate("alphabet_adventure") }
                )
                WowGameCard(
                    title = "Numbers Maze",
                    subtitle = "Labirint + numere + viteză",
                    icon = "icon_game_math",
                    onClick = { navController.navigate("numbers_maze") }
                )
                WowGameCard(
                    title = "Build Farm",
                    subtitle = "Construiește ferma, pas cu pas",
                    icon = "icon_game_animals",
                    onClick = { navController.navigate("build_farm") }
                )
                WowGameCard(
                    title = "Colour Rainbow",
                    subtitle = "Culori, reflexe, combo!",
                    icon = "icon_game_colors",
                    onClick = { navController.navigate("colour_rainbow") }
                )
                WowGameCard(
                    title = "Interactive Story",
                    subtitle = "Alege-ți povestea!",
                    icon = "icon_game_story",
                    onClick = { navController.navigate("interactive_story") }
                )
            }

            Spacer(Modifier.weight(1f))
            Text(text = "✨ Tip: toate ecranele au efecte, particule și feedback!", color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WowGameCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.08f))
                )
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssetImage(drawableName = icon, modifier = Modifier.size(56.dp))
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFFF59D).copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "➜", color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}
