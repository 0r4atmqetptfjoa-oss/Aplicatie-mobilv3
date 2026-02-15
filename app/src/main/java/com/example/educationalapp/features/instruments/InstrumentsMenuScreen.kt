package com.example.educationalapp.features.instruments

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.educationalapp.R
import com.example.educationalapp.common.AppBackButton
import com.example.educationalapp.di.SoundManager
import com.example.educationalapp.ui.components.rememberAssetSheet
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SoundManagerEntryPoint {
    fun soundManager(): SoundManager
}

/**
 * Represents an instrument shown in the instruments menu. The [route] property is the
 * navigation destination associated with the instrument. It is typed as [Any] so that
 * route objects defined in [com.example.educationalapp.navigation.Routes] can be stored
 * directly without converting to strings. When null, tapping the card will do nothing.
 */
// A simple data holder for instrument menu entries. Each entry links an
// [InstrumentDefinition] with a navigation route and an optional icon asset.
private data class MenuInstrument(
    val definition: InstrumentDefinition,
    val route: Any,
    val icon: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentsMenuScreen(navController: NavController) {
    // Build the menu items. We use the instrument definitions defined in
    // [InstrumentSoundScreen.kt] and associate each with its navigation route and
    // a representative icon (using the first key image for that instrument).
    val instruments = remember {
        listOf(
            MenuInstrument(PianoDefinition, com.example.educationalapp.navigation.PianoSoundsRoute, "tecla_p1.png"),
            MenuInstrument(XylofonDefinition, com.example.educationalapp.navigation.XylofonSoundsRoute, "tecla_x1.png"),
            MenuInstrument(GuitarDefinition, com.example.educationalapp.navigation.GuitarSoundsRoute, "cuerda1.png"),
            MenuInstrument(HarpDefinition, com.example.educationalapp.navigation.HarpaSoundsRoute, "arpa_c1.png"),
            MenuInstrument(SaxophoneDefinition, com.example.educationalapp.navigation.SaxofonSoundsRoute, "btn_saxo01.png"),
            // Newly added drums instrument (tobe). Use the first drum plate as the icon.
            MenuInstrument(DrumsDefinition, com.example.educationalapp.navigation.TobeSoundsRoute, "platillo1.png")
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_game_instruments),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(id = R.string.main_menu_button_instruments),
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(start = 64.dp)
                        )
                    },
                    navigationIcon = {}, // Folosim AppBackButton manual
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp)
            ) {
                itemsIndexed(instruments) { _, instrument ->
                    InstrumentMenuCard(
                        instrument = instrument,
                        onClick = {
                            navController.navigate(instrument.route)
                        }
                    )
                }
            }
        }

        AppBackButton(onBack = { navController.popBackStack() })
    }
}

/**
 * A card used in the instruments menu. It displays a large button with an
 * illustration for the instrument and its name. The card bounces slightly
 * when pressed to provide tactile feedback and triggers [onClick].
 */
@Composable
private fun InstrumentMenuCard(
    instrument: MenuInstrument,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            SoundManagerEntryPoint::class.java
        )
        entryPoint.soundManager()
    }

    // Load the icon from the assets folder. We prefix with the instrument folder
    // to locate the correct file. The image is decoded off the UI thread via
    // rememberAssetSheet.
    val bitmap = rememberAssetSheet(path = "instruments/${instrument.definition.folder}/${instrument.icon}")
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 300f),
        label = "scale"
    )
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        // Wait for release to reset the pressed state
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    },
                    onTap = {
                        soundManager.playClickIconSound()
                        onClick()
                    }
                )
            },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .padding(bottom = 12.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Text(
                text = instrument.definition.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun InstrumentsMenuScreenPreview() {
    InstrumentsMenuScreen(rememberNavController())
}
