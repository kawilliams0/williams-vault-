package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ========================================================
// Theme Palettes Definition & Color Schemes
// ========================================================

enum class AppThemePalette(
    val id: String,
    val displayName: String,
    val description: String,
    val primaryPreview: Color,
    val accentPreview: Color,
    val darkBgPreview: Color,
    val lightBgPreview: Color
) {
    NAVY_SKY(
        id = "NAVY_SKY",
        displayName = "Electric Sky",
        description = "Midnight Navy & Sky Blue",
        primaryPreview = Color(0xFF38BDF8),
        accentPreview = Color(0xFF34D399),
        darkBgPreview = Color(0xFF0A192F),
        lightBgPreview = Color(0xFFF8FAFC)
    ),
    EMERALD_GROWTH(
        id = "EMERALD_GROWTH",
        displayName = "Emerald Mint",
        description = "Rich Forest & Vivid Mint",
        primaryPreview = Color(0xFF10B981),
        accentPreview = Color(0xFF38BDF8),
        darkBgPreview = Color(0xFF061A14),
        lightBgPreview = Color(0xFFF0FDF4)
    ),
    CYBER_PURPLE(
        id = "CYBER_PURPLE",
        displayName = "Neon Violet",
        description = "Royal Obsidian & Radiant Lilac",
        primaryPreview = Color(0xFFA855F7),
        accentPreview = Color(0xFF34D399),
        darkBgPreview = Color(0xFF0F0C20),
        lightBgPreview = Color(0xFFFAF5FF)
    ),
    SUNSET_AMBER(
        id = "SUNSET_AMBER",
        displayName = "Golden Amber",
        description = "Obsidian & Radiant Gold",
        primaryPreview = Color(0xFFF59E0B),
        accentPreview = Color(0xFF38BDF8),
        darkBgPreview = Color(0xFF18120C),
        lightBgPreview = Color(0xFFFFFBEB)
    ),
    ROSE_CRIMSON(
        id = "ROSE_CRIMSON",
        displayName = "Rose Quartz",
        description = "Midnight Berry & Vivid Rose",
        primaryPreview = Color(0xFFF43F5E),
        accentPreview = Color(0xFF38BDF8),
        darkBgPreview = Color(0xFF180C14),
        lightBgPreview = Color(0xFFFFF1F2)
    ),
    MONOCHROME_SLATE(
        id = "MONOCHROME_SLATE",
        displayName = "Pure Slate",
        description = "Pitch Onyx & Platinum Silver",
        primaryPreview = Color(0xFFE4E4E7),
        accentPreview = Color(0xFF38BDF8),
        darkBgPreview = Color(0xFF09090B),
        lightBgPreview = Color(0xFFFFFFFF)
    );

    companion object {
        fun fromId(id: String): AppThemePalette {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NAVY_SKY
        }
    }
}

// --------------------------------------------------------
// Palette 1: Electric Sky (Midnight Navy & Electric Sky Blue)
// --------------------------------------------------------
private val NavySkyDarkScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF0A192F),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0A192F),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFF34D399),
    background = Color(0xFF0A192F),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF112240),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1B3A5B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF233554),
    outlineVariant = Color(0x3338BDF8),
    error = Color(0xFFF87171),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171)
)

private val NavySkyLightScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF0C4A6E),
    secondary = Color(0xFF0F172A),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF0F172A),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0A192F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0A192F),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// --------------------------------------------------------
// Palette 2: Emerald Growth (Obsidian Forest & Mint Green)
// --------------------------------------------------------
private val EmeraldDarkScheme = darkColorScheme(
    primary = Color(0xFF10B981),
    onPrimary = Color(0xFF061A14),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF6EE7B7),
    onSecondary = Color(0xFF061A14),
    secondaryContainer = Color(0xFF0C2D23),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFF38BDF8),
    background = Color(0xFF061A14),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF0C2D23),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF144738),
    onSurfaceVariant = Color(0xFFD1FAE5),
    outline = Color(0xFF1E5E4B),
    outlineVariant = Color(0x3310B981),
    error = Color(0xFFF87171),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171)
)

private val EmeraldLightScheme = lightColorScheme(
    primary = Color(0xFF059669),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF064E3B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F4EA),
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF075985),
    background = Color(0xFFF0FDF4),
    onBackground = Color(0xFF061A14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF061A14),
    surfaceVariant = Color(0xFFDCFCE7),
    onSurfaceVariant = Color(0xFF166534),
    outline = Color(0xFFBBF7D0),
    outlineVariant = Color(0xFFDCFCE7),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// --------------------------------------------------------
// Palette 3: Neon Violet (Midnight Royal & Radiant Lilac)
// --------------------------------------------------------
private val VioletDarkScheme = darkColorScheme(
    primary = Color(0xFFA855F7),
    onPrimary = Color(0xFF0F0C20),
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFC084FC),
    onSecondary = Color(0xFF0F0C20),
    secondaryContainer = Color(0xFF1E173D),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFF34D399),
    background = Color(0xFF0F0C20),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF181335),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF261E52),
    onSurfaceVariant = Color(0xFFE9D5FF),
    outline = Color(0xFF3B2F70),
    outlineVariant = Color(0x33A855F7),
    error = Color(0xFFF87171),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171)
)

private val VioletLightScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = Color(0xFF3B0764),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFFAF5FF),
    onBackground = Color(0xFF0F0C20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F0C20),
    surfaceVariant = Color(0xFFF5F3FF),
    onSurfaceVariant = Color(0xFF581C87),
    outline = Color(0xFFDDD6FE),
    outlineVariant = Color(0xFFEDE9FE),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// --------------------------------------------------------
// Palette 4: Golden Amber (Obsidian & Radiant Gold)
// --------------------------------------------------------
private val AmberDarkScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF18120C),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFCD34D),
    onSecondary = Color(0xFF18120C),
    secondaryContainer = Color(0xFF2E2015),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFF34D399),
    background = Color(0xFF18120C),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF261D14),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3A2D1F),
    onSurfaceVariant = Color(0xFFFEF3C7),
    outline = Color(0xFF57422E),
    outlineVariant = Color(0x33F59E0B),
    error = Color(0xFFF87171),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171)
)

private val AmberLightScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF78350F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFFBEB),
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFFFFBEB),
    onBackground = Color(0xFF18120C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18120C),
    surfaceVariant = Color(0xFFFEF9C3),
    onSurfaceVariant = Color(0xFF854D0E),
    outline = Color(0xFFFDE68A),
    outlineVariant = Color(0xFFFEF3C7),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// --------------------------------------------------------
// Palette 5: Rose Quartz (Midnight Berry & Vivid Rose)
// --------------------------------------------------------
private val RoseDarkScheme = darkColorScheme(
    primary = Color(0xFFF43F5E),
    onPrimary = Color(0xFF180C14),
    primaryContainer = Color(0xFF881337),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFFDA4AF),
    onSecondary = Color(0xFF180C14),
    secondaryContainer = Color(0xFF2C1322),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF075985),
    onTertiaryContainer = Color(0xFF38BDF8),
    background = Color(0xFF180C14),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF271321),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF3B1E33),
    onSurfaceVariant = Color(0xFFFFE4E6),
    outline = Color(0xFF5A2A4C),
    outlineVariant = Color(0x33F43F5E),
    error = Color(0xFFF87171),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171)
)

private val RoseLightScheme = lightColorScheme(
    primary = Color(0xFFE11D48),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF881337),
    secondary = Color(0xFF881337),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFF1F2),
    onSecondaryContainer = Color(0xFF881337),
    tertiary = Color(0xFF0284C7),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F2FE),
    onTertiaryContainer = Color(0xFF075985),
    background = Color(0xFFFFF1F2),
    onBackground = Color(0xFF180C14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF180C14),
    surfaceVariant = Color(0xFFFFE4E6),
    onSurfaceVariant = Color(0xFF9F1239),
    outline = Color(0xFFFECDD3),
    outlineVariant = Color(0xFFFFE4E6),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

// --------------------------------------------------------
// Palette 6: Pure Slate (Pitch Onyx & Platinum Silver)
// --------------------------------------------------------
private val SlateDarkScheme = darkColorScheme(
    primary = Color(0xFFE4E4E7),
    onPrimary = Color(0xFF09090B),
    primaryContainer = Color(0xFF27272A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF09090B),
    secondaryContainer = Color(0xFF18181B),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFF34D399),
    background = Color(0xFF09090B),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF141416),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF202024),
    onSurfaceVariant = Color(0xFFD4D4D8),
    outline = Color(0xFF3F3F46),
    outlineVariant = Color(0x33E4E4E7),
    error = Color(0xFFF87171),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171)
)

private val SlateLightScheme = lightColorScheme(
    primary = Color(0xFF18181B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4E4E7),
    onPrimaryContainer = Color(0xFF18181B),
    secondary = Color(0xFF3F3F46),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4F4F5),
    onSecondaryContainer = Color(0xFF18181B),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF09090B),
    surface = Color(0xFFF4F4F5),
    onSurface = Color(0xFF09090B),
    surfaceVariant = Color(0xFFE4E4E7),
    onSurfaceVariant = Color(0xFF27272A),
    outline = Color(0xFFD4D4D8),
    outlineVariant = Color(0xFFE4E4E7),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

fun getAppColorScheme(palette: AppThemePalette, isDark: Boolean): ColorScheme {
    return when (palette) {
        AppThemePalette.NAVY_SKY -> if (isDark) NavySkyDarkScheme else NavySkyLightScheme
        AppThemePalette.EMERALD_GROWTH -> if (isDark) EmeraldDarkScheme else EmeraldLightScheme
        AppThemePalette.CYBER_PURPLE -> if (isDark) VioletDarkScheme else VioletLightScheme
        AppThemePalette.SUNSET_AMBER -> if (isDark) AmberDarkScheme else AmberLightScheme
        AppThemePalette.ROSE_CRIMSON -> if (isDark) RoseDarkScheme else RoseLightScheme
        AppThemePalette.MONOCHROME_SLATE -> if (isDark) SlateDarkScheme else SlateLightScheme
    }
}

// ==========================================
// Dynamic Theme Color Tokens (Universal Access)
// ==========================================

val PureWhite = Color(0xFFFFFFFF)
val PureBlack = Color(0xFF000000)

val DarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val DarkSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val DarkSurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val LightBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background

val LightSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val LightSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surfaceVariant

val LightSurfaceCard: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.surface

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onBackground

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val LilacPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val LilacOnPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val LilacContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primaryContainer

val LilacOnContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimaryContainer

val LilacActivePill: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary

val LilacActivePillContent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onPrimary

val SecondaryDarkContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.secondaryContainer

val SecondaryOnDarkContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.onSecondaryContainer

val BorderDark: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outline

val BorderDarkSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val BorderDivider: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.outlineVariant

val MintCyan: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val MintCyanContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiaryContainer

val MintCyanText: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val CoralRed: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val CoralRedContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.errorContainer

val ExpenseRed: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.error

val ExpenseRedLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.errorContainer

val IncomeGreen: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiary

val IncomeGreenLight: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.tertiaryContainer

val AmberGold: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFFF59E0B)

val AmberGoldContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = Color(0xFF78350F)

// Constant color references used by charts and palettes
val NavySkyPrimary = Color(0xFF38BDF8)
val NavyRoyalPrimary = Color(0xFF1E3A8A)
val NavyMidnight = Color(0xFF0A192F)
val DarkMintCyan = Color(0xFF34D399)
val SecondaryLavender = Color(0xFF93C5FD)
val DarkAmberGold = Color(0xFFF59E0B)
val DarkCoralRed = Color(0xFFF87171)
