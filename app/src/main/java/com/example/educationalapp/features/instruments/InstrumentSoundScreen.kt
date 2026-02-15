package com.example.educationalapp.features.instruments

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import com.example.educationalapp.fx.GameHudState
import com.example.educationalapp.fx.ParticleController
import com.example.educationalapp.fx.UltraGameScaffold
import com.example.educationalapp.ui.components.rememberAssetSheet
import com.example.educationalapp.ui.media.rememberSoundPlayer

@Composable
fun InstrumentSoundScreen(
    definition: InstrumentDefinition,
    navController: NavController
) {
    // Încărcăm resursele
    val backgroundBitmap: ImageBitmap? = definition.background?.let { bgName ->
        rememberAssetSheet(path = "instruments/${definition.folder}/$bgName")
    }
    val players = definition.soundFiles.map { fileName ->
        val uri = remember(fileName) { Uri.parse("asset:///instruments/${definition.folder}/$fileName") }
        rememberSoundPlayer(soundUri = uri)
    }
    val keyBitmaps = definition.keyImages.map { imageName ->
        rememberAssetSheet(path = "instruments/${definition.folder}/$imageName")
    }
    // Resurse extra (capac pian, mască harpă)
    val overlayBitmap: ImageBitmap? = if (definition.folder == "pian") rememberAssetSheet(path = "instruments/pian/tapa_piano.png") else null
    val harpMaskBitmap: ImageBitmap? = if (definition.folder == "harpa") rememberAssetSheet(path = "instruments/harpa/mascara_arpa.png") else null

    val particles = remember { ParticleController() }
    LaunchedEffect(players) { players.forEach { it.volume = 1.0f } }

    UltraGameScaffold(
        backgroundRes = com.example.educationalapp.R.drawable.bg_game_instruments,
        hud = GameHudState(title = definition.name, score = 0, levelLabel = "Atinge notele", starCount = 0),
        onBack = { navController.popBackStack() },
        particleController = particles
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundBitmap != null) {
                Image(bitmap = backgroundBitmap, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }

            // Aici decidem ce ecran afișăm în funcție de folder
            when (definition.folder) {
                "chitara" -> GuitarScreen(keyBitmaps, players, particles)
                "saxofon" -> SaxophoneScreen(keyBitmaps, players, particles)
                "tobe" -> DrumsScreen(keyBitmaps, players, particles)
                "harpa" -> HarpScreen(keyBitmaps, players, particles, harpMaskBitmap)
                "pian" -> PianoScreen(keyBitmaps, players, overlayBitmap, particles)
                "xilofon" -> XylophoneScreen(keyBitmaps, players, particles)
                else -> { /* Fallback */ }
            }
        }
    }
}