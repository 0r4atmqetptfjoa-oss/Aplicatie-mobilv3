package com.example.educationalapp.features.sounds

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

/**
 * Text that can be either a raw String (quick to iterate) or a String resource (for localization).
 * Tip: migrate progressively from UiText.Raw(...) -> UiText.Res(R.string....).
 */
sealed interface UiText {
    data class Raw(val value: String) : UiText
    data class Res(@StringRes val id: Int) : UiText
}

data class SoundItem(
    val id: String,
    val name: UiText,
    @DrawableRes val imageRes: Int,
    @RawRes val soundRes: Int
)

data class SoundCategory(
    val id: String,
    val title: UiText,
    @DrawableRes val coverRes: Int,
    /**
     * Vertical bias for cover crop focus (-1 = top, 0 = center, +1 = bottom).
     * Useful to keep faces fully visible (ex: delfin, panda).
     */
    val coverYBias: Float = 0f,
    @DrawableRes val backgroundRes: Int,
    @RawRes val ambientMusicRes: Int? = null,
    @RawRes val menuSfxRes: Int? = null,
    val themeColor: Color,
    val items: List<SoundItem>
)
