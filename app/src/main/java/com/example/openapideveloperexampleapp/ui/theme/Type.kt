// SPDX-FileCopyrightText: 2024 Swarovski-Optik AG & Co KG.
// SPDX-License-Identifier: Apache-2.0

package com.example.openapideveloperexampleapp.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle

// Design Language Typography
val AppTypography = Typography(
    // Headings: 32-48sp, medium weight (500)
    displayLarge = TextStyle(
        fontSize = 48.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    displaySmall = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    // Body: 16-18sp, regular weight (400)
    bodyLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = TextSecondary
    ),
    bodyMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = TextSecondary
    ),
    // Buttons: 14-16sp, medium weight (500)
    labelLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    ),
    // Captions: 12-14sp, regular weight (400)
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = TextMuted
    )
)

