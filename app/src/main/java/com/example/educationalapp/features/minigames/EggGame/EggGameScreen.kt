package com.example.educationalapp.features.wowgames

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.educationalapp.R
import kotlinx.coroutines.launch

@Composable
fun EggGameScreen(
    viewModel: EggGameViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val creature = EggAssets.creatures[uiState.currentCreatureIndex]

    val scaleAnim = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // A. BACKGROUND
        Image(
            painter = painterResource(R.drawable.bg_magic_forest),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // B. STRATURI VIZUALE
        
        // 1. AURA MAGICĂ (Doar când oul e crăpat tare sau la final)
        val showAura = uiState.stage == EggStage.CRACK_2 || uiState.stage == EggStage.HATCHED
        MagicAura(color = creature.particleColor, isActive = showAura)

        // 2. SCENA CENTRALĂ (OU / CREATURĂ)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // --- FIX POZIȚIE: Coborât mult mai jos ---
                .padding(bottom = 50.dp) 
        ) {
            val imageRes = when (uiState.stage) {
                EggStage.INTACT -> creature.eggIntactRes
                EggStage.CRACK_1 -> creature.eggCrack1Res
                EggStage.CRACK_2 -> creature.eggCrack2Res
                EggStage.HATCHED -> if (uiState.isCreatureHappy) creature.charHappyRes else creature.charIdleRes
            }

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Character",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(360.dp)
                    .scale(scaleAnim.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.onInteraction()
                        
                        // Bounce Effect
                        scope.launch {
                            scaleAnim.animateTo(1.15f, spring(dampingRatio = 0.4f, stiffness = 600f))
                            scaleAnim.animateTo(1f)
                        }
                    }
            )
        }

        // 3. PARTICULE (Peste ou)
        ParticleExplosion(
            trigger = uiState.hatchTrigger,
            color = creature.particleColor,
            originY = 1000f // Ajustabil în funcție de ecran, aprox mijloc-jos
        )

        // C. BUTON NEXT (URIAȘ ȘI EVIDENT)
        if (uiState.showNextButton) {
            val infiniteTransition = rememberInfiniteTransition(label = "btn")
            val pulse by infiniteTransition.animateFloat(
                initialValue = 1f, targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "pulse"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Dreapta Jos
                    .padding(end = 20.dp, bottom = 40.dp) // Puțină margine
            ) {
                Image(
                    painter = painterResource(R.drawable.ui_btn_home), // Sau un icon "ARROW RIGHT" mare
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(130.dp) // --- URIAȘ ---
                        .scale(pulse)
                        .clickable { viewModel.nextLevel() }
                )
            }
        }

        // D. BUTON BACK
        Image(
            painter = painterResource(R.drawable.ui_btn_back_wood),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(72.dp)
                .clickable { onBack() }
        )
    }
}