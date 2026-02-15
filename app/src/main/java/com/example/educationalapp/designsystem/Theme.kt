package com.example.educationalapp.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.educationalapp.designsystem.PastelPink
import com.example.educationalapp.designsystem.PastelLavender
import com.example.educationalapp.designsystem.PastelMint
import com.example.educationalapp.designsystem.PastelBlue
import com.example.educationalapp.designsystem.PastelPeach

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// A light colour scheme tuned for children: we swap the default Material
// palette for the pastel colours defined in our design system.  These colours
// follow research on kids’ attention and accessibility by using soft hues
// instead of harsh primaries【412315585523685†L285-L294】.  We pair each
// background colour with a dark on‑colour for sufficient contrast.【751512960875964†L282-L295】
private val LightColorScheme = lightColorScheme(
    primary = PastelPink,
    secondary = PastelLavender,
    tertiary = PastelMint,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    background = PastelBlue,
    surface = PastelPeach,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun EducationalAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
