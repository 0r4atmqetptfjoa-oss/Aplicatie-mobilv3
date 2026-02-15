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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.educationalapp.R
import kotlin.math.roundToInt

@Composable
fun FeedGameScreen(
    viewModel: FeedGameViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenW = with(density) { config.screenWidthDp.dp.toPx() }
    val screenH = with(density) { config.screenHeightDp.dp.toPx() }
    
    // Gura Monstrului: Ajustată pentru monstrul mai mare
    val mouthPos = remember(screenW, screenH) { Offset(screenW * 0.5f, screenH * 0.4f) }

    val infiniteTransition = rememberInfiniteTransition(label = "idle")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), label = "breath"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. BACKGROUND
        Image(
            painter = painterResource(FeedAssets.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. MONSTRUL MOMO (URIAȘ)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                // Îl lăsăm puțin mai jos ca să pară că stă la masă, dar să nu intre peste mâncare
                .offset(y = (-20).dp) 
        ) {
            val currentImage = when (uiState.monsterState) {
                MonsterState.IDLE -> FeedAssets.charIdle
                MonsterState.OPEN_MOUTH -> FeedAssets.charOpen
                MonsterState.EATING -> FeedAssets.charEat
                MonsterState.SAD -> FeedAssets.charSad
            }

            val currentScale = if (uiState.monsterState == MonsterState.IDLE) breatheScale else 1f

            Image(
                painter = painterResource(currentImage),
                contentDescription = "Momo",
                modifier = Modifier
                    .size(550.dp) // --- AICI E MODIFICAREA: MULT MAI MARE ---
                    .scale(currentScale)
            )
        }

        // 3. GRID MÂNCARE (Jos)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedAssets.foods.forEach { food ->
                var myPos by remember { mutableStateOf(Offset.Zero) }
                val isBeingThrown = (uiState.isFlying && uiState.flyingFoodRes == food.imageRes)
                
                Image(
                    painter = painterResource(food.imageRes),
                    contentDescription = food.name,
                    alpha = if (isBeingThrown) 0f else 1f,
                    modifier = Modifier
                        .size(110.dp) 
                        .onGloballyPositioned { 
                            val pos = it.positionInRoot()
                            val size = it.size
                            myPos = Offset(pos.x + size.width/2, pos.y + size.height/2)
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.onFoodSelected(food, myPos)
                        }
                )
            }
        }

        // 4. MÂNCARE ZBURĂTOARE
        if (uiState.isFlying && uiState.flyingFoodRes != null) {
            val anim = remember { Animatable(0f) }
            LaunchedEffect(uiState.isFlying) {
                anim.snapTo(0f)
                anim.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
            }
            
            val t = anim.value
            val currentX = uiState.flyStart.x + (mouthPos.x - uiState.flyStart.x) * t
            val currentY = uiState.flyStart.y + (mouthPos.y - uiState.flyStart.y) * t
            val arcY = currentY - (150f * kotlin.math.sin(t * Math.PI).toFloat())

            Image(
                painter = painterResource(uiState.flyingFoodRes!!),
                contentDescription = null,
                modifier = Modifier
                    .offset { IntOffset(currentX.roundToInt() - 150, arcY.roundToInt() - 150) }
                    .size(100.dp)
                    .zIndex(10f)
            )
        }

        // Buton Back
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