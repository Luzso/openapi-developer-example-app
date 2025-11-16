// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Design Language Theme
// Minimalist, futuristic interface with deep neutral grays and cool blue accents
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = TextPrimary,

    secondary = PrimaryBlue,
    onSecondary = TextPrimary,
    secondaryContainer = SurfaceMedium,
    onSecondaryContainer = TextSecondary,

    tertiary = SuccessCyan,
    onTertiary = BackgroundDark,

    error = ErrorRed,
    onError = TextPrimary,

    background = BackgroundDark,
    onBackground = TextSecondary,

    surface = SurfaceDark,
    onSurface = TextSecondary,
    surfaceVariant = SurfaceMedium,
    onSurfaceVariant = TextMuted,

    outline = BorderSubtle,
    outlineVariant = TextMutedDark
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Always use dark theme as per design language
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

