package com.example.educationalapp.fx

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Resolve drawables by name at runtime.
 *
 * This lets us freely "select the best art" from the drawable pack without risking
 * compile-time crashes if a resource name is missing in a particular build variant.
 */
object Assets {
    @DrawableRes
    fun idByName(context: android.content.Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }
}

@Composable
fun rememberDrawableId(name: String, fallback: Int = 0): Int {
    val ctx = LocalContext.current
    return remember(name) {
        val id = Assets.idByName(ctx, name)
        if (id == 0) fallback else id
    }
}
