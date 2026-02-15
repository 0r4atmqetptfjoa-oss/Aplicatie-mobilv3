package com.example.educationalapp.features.mainmenu

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
// Importăm explicit rotate din DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.example.educationalapp.R
import com.example.educationalapp.di.SoundManagerEntryPoint
import com.example.educationalapp.fx.AmbientMagicParticles
import com.example.educationalapp.navigation.*
import com.example.educationalapp.ui.theme.KidFontFamily
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.PI

data class MainMenuModule(
    val route: Any,
    @DrawableRes val iconRes: Int,
    val title: String,
)

@Composable
fun MainMenuScreen(
    navController: NavController,
    starCount: Int,
    musicEnabled: Boolean,
) {
    val bgBitmap = ImageBitmap.imageResource(R.drawable.bg_main_menu_landscape)
    val flowerBitmap = ImageBitmap.imageResource(R.drawable.ic_settings_flower)

    val birdFrames = listOf(
        ImageBitmap.imageResource(R.drawable.img_bird_blue),
        ImageBitmap.imageResource(R.drawable.img_bird_blue_flapping_1),
        ImageBitmap.imageResource(R.drawable.img_bird_blue_flapping_2)
    )

    val modules = listOf(
        MainMenuModule(GamesMenuRoute, R.drawable.main_menu_icon_jocuri, stringResource(id = R.string.main_menu_button_games)),
        MainMenuModule(InstrumentsMenuRoute, R.drawable.main_menu_icon_instrumente, stringResource(id = R.string.main_menu_button_instruments)),
        MainMenuModule(SongsMenuRoute, R.drawable.main_menu_icone_cantece, stringResource(id = R.string.main_menu_button_songs)),
        MainMenuModule(SoundsMenuRoute, R.drawable.main_menu_icon_sunete, stringResource(id = R.string.main_menu_button_sounds)),
        MainMenuModule(StoriesMenuRoute, R.drawable.main_menu_icon_povesti, stringResource(id = R.string.main_menu_button_stories)),
    )
    // --- Muzica meniului principal: se aude doar cat timp suntem pe acest ecran ---
    val context = LocalContext.current
    val soundManager = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SoundManagerEntryPoint::class.java
        )
        entryPoint.soundManager()
    }

    DisposableEffect(musicEnabled) {
        if (musicEnabled) soundManager.playMenuMusic() else soundManager.pauseMenuMusic()
        onDispose {
            // Cand iesim din ecranul de meniu, oprim muzica.
            soundManager.pauseMenuMusic()
        }
    }


    LivingBackground(
        backgroundImage = bgBitmap,
        birdFrames = birdFrames
    ) {
        // Gradient jos pentru contrast
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                    )
                )
        )

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val (starsRef, titleRef, sunbeamsRef, settingsRef, menuRowRef) = createRefs()

            // A. CAPSULA DE STELE
            Box(
                modifier = Modifier
                    .constrainAs(starsRef) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val infiniteTransition = rememberInfiniteTransition(label = "starPulse")
                    val starScale by infiniteTransition.animateFloat(
                        initialValue = 1f, targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
                    )

                    Image(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Stele",
                        modifier = Modifier
                            .size(32.dp)
                            .scale(starScale),
                        colorFilter = ColorFilter.tint(Color(0xFFFFD700))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    BubblyText(
                        text = "$starCount",
                        fontSize = 24.sp,
                        strokeColor = Color(0xFFB8860B)
                    )
                }
            }

            // B. RAZELE DIVINE
            RotatingSunbeams(
                modifier = Modifier
                    .constrainAs(sunbeamsRef) {
                        centerTo(titleRef)
                    }
                    .size(500.dp)
                    .alpha(0.5f) 
            )

            // C. TITLUL
            val infiniteTransition = rememberInfiniteTransition(label = "titleFloat")
            val titleOffsetY by infiniteTransition.animateFloat(
                initialValue = -8f, targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ), label = "titleFloatY"
            )

            val titleScale = remember { Animatable(1f) }
            val scope = rememberCoroutineScope()

            Image(
                painter = painterResource(id = R.drawable.main_menu_title),
                contentDescription = "Titlu",
                modifier = Modifier
                    .constrainAs(titleRef) {
                        top.linkTo(parent.top, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.percent(0.32f)
                    }
                    .graphicsLayer {
                        translationY = titleOffsetY
                        scaleX = titleScale.value
                        scaleY = titleScale.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        scope.launch {
                            titleScale.animateTo(0.9f, tween(50))
                            titleScale.animateTo(1.1f, spring(dampingRatio = 0.4f, stiffness = 600f))
                            titleScale.animateTo(1f, tween(100))
                        }
                    }
            )

            // D. SETĂRI (DREAPTA SUS)
            SettingsWiggleButton(
                iconBitmap = flowerBitmap,
                onClick = { navController.navigate(SettingsRoute) },
                modifier = Modifier.constrainAs(settingsRef) {
                    top.linkTo(parent.top, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                }
            )

            // E. BUTOANELE
            Row(
                modifier = Modifier
                    .constrainAs(menuRowRef) {
                        bottom.linkTo(parent.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                modules.forEachIndexed { index, module ->
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(index * 100L)
                        isVisible = true
                    }
                    
                    AnimatedVisibilityButton(
                        isVisible = isVisible,
                        modifier = Modifier.weight(1f)
                    ) {
                        ModuleButton(
                            module = module,
                            navController = navController,
                            delayIndex = index
                        )
                    }
                }
            }
        }

        AmbientMagicParticles()
    }
}

// =============================================================================
// COMPONENTE UI
// =============================================================================

/**
 * Desenează o umbră rotundă și difuză sub element.
 * Înlocuiește shadowElevation care făcea pătrate.
 */
@Composable
fun SoftShadow(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent),
                center = center,
                radius = size.minDimension / 2
            ),
            center = center,
            radius = size.minDimension / 2
        )
    }
}

@Composable
private fun ModuleButton(
    module: MainMenuModule,
    navController: NavController,
    delayIndex: Int
) {
    val context = LocalContext.current
    val soundManager = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SoundManagerEntryPoint::class.java
        )
        entryPoint.soundManager()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "pressScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "idleBreathing")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = delayIndex * 300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val totalScale = pressScale * breatheScale

    Column(
        modifier = Modifier
            // NU folosim clip() aici.
            // Clickable este pus pe întreaga coloană, dar cu indication = null nu se vede nimic.
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = { 
                    soundManager.playClickIconSound()
                    navController.navigate(module.route) 
                }
            )
            .graphicsLayer {
                scaleX = totalScale
                scaleY = totalScale
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Container pentru Icon + Umbra Custom
        Box(contentAlignment = Alignment.Center) {
            
            // 1. Umbra noastră difuză (nu e pătrată!)
            SoftShadow(
                modifier = Modifier
                    .size(160.dp)
                    .offset(y = 10.dp) // O lăsăm puțin mai jos
                    .scale(0.8f, 0.4f) // O turtim să arate a umbră pe pământ
            )

            // 2. Iconița
            Image(
                painter = painterResource(id = module.iconRes),
                contentDescription = module.title,
                modifier = Modifier.size(160.dp)
                // FĂRĂ shadowElevation AICI!
            )
        }
        
        Spacer(Modifier.height(4.dp))
        
        BubblyText(
            text = module.title,
            fontSize = 24.sp,
            strokeColor = Color(0xFF1B5E20)
        )
    }
}

@Composable
fun SettingsWiggleButton(
    iconBitmap: ImageBitmap,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SoundManagerEntryPoint::class.java
        )
        entryPoint.soundManager()
    }

    var isSpinning by remember { mutableStateOf(false) }
    
    val clickRotation by animateFloatAsState(
        targetValue = if (isSpinning) 360f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        finishedListener = { isSpinning = false },
        label = "clickRotation"
    )

    val idleRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(4000, 7000))
            if (!isSpinning) {
                repeat(3) {
                    idleRotation.animateTo(10f, animationSpec = tween(80))
                    idleRotation.animateTo(-10f, animationSpec = tween(80))
                }
                idleRotation.animateTo(0f, animationSpec = tween(100))
            }
        }
    }

    // Buton Setări fără niciun fundal
    Box(
        modifier = modifier
            .size(90.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Strict invizibil la click
            ) {
                soundManager.playClickIconSound()
                isSpinning = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Punem o umbră fină doar sub floare, dacă vrei
        SoftShadow(
             modifier = Modifier
                .fillMaxSize()
                .scale(0.8f)
                .alpha(0.3f)
        )

        Image(
            bitmap = iconBitmap,
            contentDescription = "Setări",
            modifier = Modifier
                .fillMaxSize()
                .rotate(clickRotation + idleRotation.value)
        )
    }
}

@Composable
fun BubblyText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    strokeColor: Color = Color.Black,
    fillColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 1. Conturul (Outline)
        Text(
            text = text,
            style = TextStyle(
                fontFamily = KidFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                color = strokeColor,
                textAlign = TextAlign.Center,
                drawStyle = Stroke(width = 12f, join = StrokeJoin.Round)
            )
        )
        // 2. Umplutura (Fill)
        Text(
            text = text,
            style = TextStyle(
                fontFamily = KidFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                color = fillColor,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
fun RotatingSunbeams(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunbeamSpin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.rotate(rotation)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width / 2
        val rayCount = 12
        val angleStep = 360f / rayCount

        for (i in 0 until rayCount) {
            val angle = i * angleStep
            
            rotate(degrees = angle, pivot = center) {
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(centerX, centerY)
                        lineTo(centerX + 40f, centerY - radius)
                        lineTo(centerX - 40f, centerY - radius)
                        close()
                    },
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.4f), Color.White.copy(alpha = 0.0f)),
                        radius = radius
                    )
                )
            }
        }
    }
}

@Composable
fun AnimatedVisibilityButton(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "popIn"
    )

    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

@Composable
fun LivingBackground(
    backgroundImage: ImageBitmap,
    birdFrames: List<ImageBitmap>,
    content: @Composable BoxScope.() -> Unit 
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // (scos) nu mai animam floarea/sunflower pe fundal
        // (scos) nu mai animam floarea/sunflower pe fundal

        AnimatedMovingElement(bitmaps = birdFrames, startX = -0.2f, endX = 1.2f, yPercent = 0.25f, duration = 20000, size = 55.dp, frameDuration = 120)
        AnimatedMovingElement(bitmaps = birdFrames, startX = 1.2f, endX = -0.2f, yPercent = 0.35f, duration = 25000, size = 50.dp, flipHorizontal = true, frameDuration = 100)

        content()
    }
}

@Composable
fun MovingElement(
    bitmap: ImageBitmap,
    startX: Float, endX: Float, yPercent: Float, duration: Int, size: Dp, alpha: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "movingElement")
    val xPercent by infiniteTransition.animateFloat(
        initialValue = startX, targetValue = endX,
        animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing)), label = "xPos"
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap, contentDescription = null, alpha = alpha,
            modifier = Modifier.size(size).offset { IntOffset((xPercent * constraints.maxWidth).toInt(), (yPercent * constraints.maxHeight).toInt()) }
        )
    }
}

@Composable
fun AnimatedMovingElement(
    bitmaps: List<ImageBitmap>,
    startX: Float, endX: Float, yPercent: Float, duration: Int, size: Dp,
    flipHorizontal: Boolean = false, frameDuration: Long = 150
) {
    val infiniteTransition = rememberInfiniteTransition(label = "movingAnim")
    val xPercent by infiniteTransition.animateFloat(
        initialValue = startX, targetValue = endX,
        animationSpec = infiniteRepeatable(tween(duration, easing = LinearEasing)), label = "xPos"
    )
    var currentFrame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(frameDuration)
            currentFrame = (currentFrame + 1) % bitmaps.size
        }
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (bitmaps.isNotEmpty()) {
            Image(
                bitmap = bitmaps[currentFrame], contentDescription = null,
                modifier = Modifier
                    .size(size)
                    .offset { IntOffset((xPercent * constraints.maxWidth).toInt(), (yPercent * constraints.maxHeight).toInt()) }
                    .graphicsLayer { scaleX = if (flipHorizontal) -1f else 1f }
            )
        }
    }
}