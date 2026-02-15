package com.example.educationalapp.features.minigames.MemoryGame

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.educationalapp.R
import kotlinx.coroutines.delay

@Composable
fun MemoryGameScreen(
    viewModel: MemoryViewModel = hiltViewModel(),
    onHome: () -> Unit
) {
    val totalCards = viewModel.cards.size
    
    // Configurații Grid Landscape
    val (rows, columns) = when (totalCards) {
        4 -> 2 to 2
        6 -> 2 to 3
        8 -> 2 to 4
        12 -> 3 to 4
        14 -> 2 to 7
        16 -> 4 to 4
        else -> 3 to 4
    }

    // Spațiu MINIM posibil (4dp) ca să fie cardurile imense
    val spacing = 4.dp 

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_memory_pixar_room),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // HUD Minimal - doar butonul back, foarte mic, într-un colț
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp) 
            ) {
                Image(
                    painter = painterResource(R.drawable.ui_btn_back_wood),
                    contentDescription = "Back",
                    modifier = Modifier.size(45.dp).clickable { onHome() }
                )
            }

            // GRID CARE OCUPE TOT RESTUL ECRANULUI
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp), // Aproape zero padding
                contentAlignment = Alignment.Center
            ) {
                // Calculăm dimensiunile exacte disponibile
                val availableWidth = maxWidth - (spacing * (columns - 1))
                val availableHeight = maxHeight - (spacing * (rows - 1))
                
                val cardWidth = availableWidth / columns
                val cardHeight = availableHeight / rows
                
                // Folosim 100% din spațiul minim (fără margine de siguranță)
                val cardSize = minOf(cardWidth, cardHeight)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(spacing),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    modifier = Modifier
                        .width(cardSize * columns + spacing * (columns - 1))
                        .height(cardSize * rows + spacing * (rows - 1))
                ) {
                    itemsIndexed(viewModel.cards) { index, card ->
                        BigPremiumCard(
                            card = card,
                            size = cardSize,
                            onClick = { viewModel.onCardClick(card) }
                        )
                    }
                }
            }
        }

        if (viewModel.isGameWon()) {
            WinOverlay(
                onAutoAdvance = { viewModel.advanceLevel() }, // Trecere automată
                onBack = onHome
            )
        }
    }
}

@Composable
fun BigPremiumCard(card: MemoryCard, size: Dp, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "float"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val scale = remember { Animatable(1f) }
    LaunchedEffect(card.popToken) {
        if (card.popToken > 0) {
            scale.animateTo(1.1f, tween(150))
            scale.animateTo(1f, spring(Spring.DampingRatioHighBouncy))
        }
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(card.shakeToken) {
        if (card.shakeToken > 0) {
            repeat(4) { shake.animateTo(5f, tween(50)); shake.animateTo(-5f, tween(50)) }
            shake.animateTo(0f, tween(50))
        }
    }

    val isFaceUp = rotation > 90f

    Box(
        modifier = Modifier
            .size(size)
            .offset(y = floatAnim.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
                scaleX = scale.value
                scaleY = scale.value
                translationX = shake.value
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !card.isMatched
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!isFaceUp) {
            Image(
                painter = painterResource(R.drawable.memory_card_back_glass),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(1.dp), // Padding minim
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(card.imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.9f), // Imagine mare
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Image(
            painter = painterResource(R.drawable.memory_card_frame_gloss),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun WinOverlay(onAutoAdvance: () -> Unit, onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "robot_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )

    // Lansăm timer-ul pentru trecerea automată
    LaunchedEffect(Unit) {
        delay(3500) // Stă 3.5 secunde să vadă robotul
        onAutoAdvance()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ROBOTUL DE VICTORIE (robot_win)
            Image(
                painter = painterResource(R.drawable.robot_win), 
                contentDescription = "Winner",
                modifier = Modifier.size(300.dp).scale(scale)
            )
            
            Spacer(Modifier.height(16.dp))
            Text("BRAVO!", color = Color(0xFFFFD700), fontSize = 60.sp, fontWeight = FontWeight.Black)
            
            Spacer(Modifier.height(24.dp))
            
            // Doar buton de EXIT, că next e automat
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE53935))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("Exit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }
}