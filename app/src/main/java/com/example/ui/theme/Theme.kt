package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun FinanceTheme(
    darkTheme: Boolean = true,
    paletteId: String = "NAVY_SKY",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val palette = AppThemePalette.fromId(paletteId)
    val colorScheme = getAppColorScheme(palette, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    FinanceTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
