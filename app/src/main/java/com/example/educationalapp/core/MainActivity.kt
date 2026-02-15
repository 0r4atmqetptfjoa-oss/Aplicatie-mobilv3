package com.example.educationalapp.core

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.educationalapp.common.AppNavigation
import com.example.educationalapp.common.LocalSoundManager
import com.example.educationalapp.MainViewModel
import com.example.educationalapp.di.BgMusicManager
import com.example.educationalapp.di.SoundManager
import com.example.educationalapp.ui.theme.EducationalAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var bgMusicManager: BgMusicManager
    @Inject lateinit var soundManager: SoundManager
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Permitem conținutului să se întindă sub barele de sistem
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 2. Folosim și spațiul de lângă Notch (Display Cutout) pentru Full Screen total
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // 3. Ascundem barele imediat
        hideSystemBars()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        setContent {
            CompositionLocalProvider(LocalSoundManager provides soundManager) {
                EducationalAppTheme {
                    AppNavigation(viewModel = mainViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    /**
     * Configurează modul Imersiv total (Sticky Immersive).
     * Barele de sistem sunt ascunse complet. Dacă se face swipe, apar temporar
     * ca niște umbre și dispar singure după câteva secunde fără să modifice layout-ul.
     */
    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        // Comportamentul "Transient": barele apar doar la swipe și dispar singure.
        // Este cea mai bună opțiune pentru copii deoarece nu mută butoanele jocului.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        // Ascunde atât bara de status (sus) cât și cea de navigare (jos)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Singletons are OK to release here because the app is shutting down.
        soundManager.release()
        bgMusicManager.release()
    }
}
