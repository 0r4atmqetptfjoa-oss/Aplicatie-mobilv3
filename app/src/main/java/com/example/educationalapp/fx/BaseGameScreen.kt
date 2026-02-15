package com.example.educationalapp.fx

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

/**
 * A base composable that sets up the common scaffolding for game screens. It
 * delegates to [UltraGameScaffold] for consistent HUD and background and
 * exposes a [content] lambda where the actual game UI can be drawn.
 *
 * @param title Title displayed in the HUD bar.
 * @param score Current score to show in the HUD bar.
 * @param levelLabel Additional label (e.g. level number) for the HUD.
 * @param starCount Number of stars collected. Defaults to 0.
 * @param backgroundRes Optional drawable resource for the background. When null
 *        a generic game background is used.
 * @param onBack Callback invoked when the back button is pressed.
 * @param content The composable that draws the specific game content.
 */
@Composable
fun BaseGameScreen(
    title: String,
    score: Int,
    levelLabel: String,
    starCount: Int = 0,
    backgroundRes: Int? = null,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    UltraGameScaffold(
        backgroundRes = backgroundRes ?: com.example.educationalapp.R.drawable.bg_game_instruments,
        hud = GameHudState(title = title, score = score, levelLabel = levelLabel, starCount = starCount),
        onBack = onBack,
        particleController = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}