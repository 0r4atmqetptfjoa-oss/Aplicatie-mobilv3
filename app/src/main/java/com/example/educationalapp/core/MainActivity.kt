package com.example.educationalapp.core

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider // IMPORTANT: Importul care lipsea!
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Importurile noastre
import com.example.educationalapp.common.AppNavigation
import com.example.educationalapp.common.MainViewModel
import com.example.educationalapp.common.LocalSoundManager // Trebuie să existe în common/Locals.kt
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

        // Setări Fullscreen
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        hideSystemBars()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        // Apelul corect setContent
        setContent {
            // Aici oferim SoundManager întregii aplicații
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

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
        bgMusicManager.release()
    }
}