package com.example.educationalapp.features.learning.colors

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.educationalapp.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ColorsGameScreen(
    viewModel: ColorsGameViewModel = hiltViewModel(),
    onHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenW = with(density) { config.screenWidthDp.dp.toPx() }
    val screenH = with(density) { config.screenHeightDp.dp.toPx() }
    
    // Ținta este poziționată pe personaj (stânga centru)
    val targetPos = remember(screenW, screenH) { 
        Offset(screenW * 0.25f, screenH * 0.5f) 
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    val bgOffset by infiniteTransition.animateFloat(
        initialValue = -20f, targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "bg_parallax"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background
        Image(
            painter = painterResource(ColorsUi.Backgrounds.carnival),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.1f)
                .offset(x = bgOffset.dp)
        )

        // 2. Main Layout
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character Area
            Box(
                modifier = Modifier.weight(0.4f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                CharacterDisplay(
                    colorItem = uiState.currentTarget,
                    gameState = uiState.gameState
                )
            }

            // Options Area
            Box(
                modifier = Modifier.weight(0.6f).fillMaxHeight()
            ) {
                OptionsGrid(
                    options = uiState.options,
                    wrongId = uiState.wrongSelectionId,
                    poppedId = uiState.poppedBalloonId, // ID-ul de ascuns
                    onOptionClick = { item, pos -> 
                        viewModel.onOptionSelected(item, pos, targetPos)
                    }
                )
            }
        }

        // 3. FX Layers
        if (uiState.gameState == GameState.PROJECTILE_FLYING) {
            ProjectileAnimation(
                start = uiState.projectileStart,
                end = uiState.projectileEnd,
                color = uiState.projectileColor,
                duration = PROJECTILE_DURATION
            )
        }

        if (uiState.gameState == GameState.IMPACT) {
            SplatEffect(
                center = uiState.projectileEnd,
                color = uiState.projectileColor
            )
        }

        // 4. UI
        ScoreBoard(uiState.score)
        HomeButton(onHome)
    }
}

@Composable
fun CharacterDisplay(
    colorItem: ColorItem,
    gameState: GameState
) {
    val infiniteTransition = rememberInfiniteTransition(label = "char_idle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "char_scale"
    )

    // Este colorat doar dacă am ajuns la IMPACT sau CELEBRATE
    val isColored = gameState == GameState.IMPACT || gameState == GameState.CELEBRATE
    
    val jumpOffset by animateDpAsState(
        targetValue = if (isColored) (-30).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.5f), label = "jump"
    )

    // Imaginea: Dacă e încă în joc -> Sad. Dacă a câștigat -> Happy.
    val imgRes = if (isColored) colorItem.happyImageRes else colorItem.sadImageRes
    
    // Matrice alb-negru
    val grayMatrix = ColorMatrix().apply { setToSaturation(0f) }
    val colorFilter = if (!isColored) ColorFilter.colorMatrix(grayMatrix) else null

    Image(
        painter = painterResource(imgRes),
        contentDescription = null,
        colorFilter = colorFilter,
        modifier = Modifier
            .size(320.dp)
            .offset(y = jumpOffset)
            .scale(scale)
    )
}

@Composable
fun OptionsGrid(
    options: List<ColorItem>,
    wrongId: String?,
    poppedId: String?, // ID-ul balonului spart
    onOptionClick: (ColorItem, Offset) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val rows = 2
        val cols = 2
        
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (c in 0 until cols) {
                    val index = r * cols + c
                    if (index < options.size) {
                        val item = options[index]
                        
                        key(item.id) {
                            // Dacă acest balon este cel spart (poppedId), îl ascundem (nu îl randa)
                            if (item.id != poppedId) {
                                FloatingBalloon(
                                    item = item,
                                    isWrong = wrongId == item.id,
                                    onClick = onOptionClick
                                )
                            } else {
                                // Spacer gol ca să nu se strice layout-ul gridului
                                Spacer(modifier = Modifier.size(130.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FloatingBalloon(
    item: ColorItem,
    isWrong: Boolean,
    onClick: (ColorItem, Offset) -> Unit
) {
    val phase = remember { Random.nextFloat() * 2 * PI }
    val infiniteTransition = rememberInfiniteTransition(label = "balloon_float")
    val dy by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000 + Random.nextInt(500), easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset((phase * 1000).toInt())
        ), label = "dy"
    )

    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(isWrong) {
        if (isWrong) {
            repeat(4) {
                shakeAnim.animateTo(10f, tween(50))
                shakeAnim.animateTo(-10f, tween(50))
            }
            shakeAnim.animateTo(0f)
        }
    }

    var myPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .size(130.dp)
            .offset(x = shakeAnim.value.dp, y = dy.dp)
            .onGloballyPositioned { 
                val pos = it.positionInRoot()
                val size = it.size
                myPosition = Offset(pos.x + size.width / 2, pos.y + size.height / 2)
            }
            .pointerInput(item) {
                detectTapGestures {
                    onClick(item, myPosition)
                }
            }
    ) {
        Image(
            painter = painterResource(item.balloonImageRes),
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize()
        )
        // Iconița din balon - arătăm varianta Happy colorată ca indiciu
        Image(
            painter = painterResource(item.happyImageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .offset(y = (-10).dp)
        )
    }
}

@Composable
fun ProjectileAnimation(
    start: Offset,
    end: Offset,
    color: Color,
    duration: Long
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(duration.toInt(), easing = LinearEasing))
    }

    val t = anim.value
    // Poziția liniară
    val currentX = start.x + (end.x - start.x) * t
    val currentY = start.y + (end.y - start.y) * t
    
    // Arcare (Parabolă)
    val arcHeight = 200f
    val arcY = (4 * arcHeight * t * (1 - t))
    
    val finalX = currentX
    val finalY = currentY - arcY

    Canvas(modifier = Modifier.fillMaxSize().zIndex(99f)) {
        // Bila de vopsea
        drawCircle(color = color, radius = 30f, center = Offset(finalX, finalY))
        
        // Coadă (Trail) pentru efect de viteză
        val trailCount = 3
        for (i in 1..trailCount) {
            val lag = i * 0.05f
            if (t > lag) {
                val trailT = t - lag
                val tx = start.x + (end.x - start.x) * trailT
                val ty = start.y + (end.y - start.y) * trailT - (4 * arcHeight * trailT * (1 - trailT))
                drawCircle(
                    color = color.copy(alpha = 0.5f / i),
                    radius = 20f - (i * 4),
                    center = Offset(tx, ty)
                )
            }
        }
    }
}

@Composable
fun SplatEffect(center: Offset, color: Color) {
    data class Particle(val angle: Float, val speed: Float, val size: Float)
    val particles = remember {
        List(25) {
            Particle(
                angle = Random.nextFloat() * 360f,
                speed = Random.nextFloat() * 10f + 5f,
                size = Random.nextFloat() * 20f + 5f
            )
        }
    }
    
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = Modifier.fillMaxSize().zIndex(100f)) {
        val t = anim.value
        // Pata principală
        drawCircle(
            color = color.copy(alpha = 1f - t),
            radius = 80f * t + 20f,
            center = center
        )

        // Stropi
        particles.forEach { p ->
            val rad = Math.toRadians(p.angle.toDouble())
            val dist = p.speed * t * 50f
            val px = center.x + (cos(rad) * dist).toFloat()
            val py = center.y + (sin(rad) * dist).toFloat()
            
            drawCircle(
                color = color.copy(alpha = 1f - t),
                radius = p.size * (1f - t),
                center = Offset(px, py)
            )
        }
    }
}

@Composable
fun ScoreBoard(score: Int) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Image(
                painter = painterResource(ColorsUi.Icons.scoreBg),
                contentDescription = null,
                modifier = Modifier.size(140.dp, 60.dp)
            )
            Text(
                text = "$score",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun HomeButton(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Image(
            painter = painterResource(ColorsUi.Icons.home),
            contentDescription = "Home",
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(64.dp)
                .clickable { onClick() }
        )
    }
}