package com.pedro.octoos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pedro.octoos.R

// Acronyms and icons
val BebasNeueFamily = FontFamily(
    Font(R.font.bebas_neue_regular, FontWeight.Normal)
)

// Wordmark ("OCTO")
val OrbitronFamily = FontFamily(
    Font(R.font.orbitron_variable, FontWeight.Bold),
    Font(R.font.orbitron_variable, FontWeight.Normal)
)

// Body texts, metadata, and labels
val IbmPlexMonoFamily = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium)
)

// Digital clock and numeric displays
val ShareTechMonoFamily = FontFamily(
    Font(R.font.share_tech_mono_regular, FontWeight.Normal)
)

val OctoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = ShareTechMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 48.sp,
        letterSpacing = 1.sp,
        color = OctoText
    ),
    headlineMedium = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 2.sp,
        color = OctoText
    ),
    titleMedium = TextStyle(
        fontFamily = BebasNeueFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = 1.5.sp,
        color = OctoText
    ),
    bodyLarge = TextStyle(
        fontFamily = IbmPlexMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = OctoText
    ),
    bodyMedium = TextStyle(
        fontFamily = IbmPlexMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = OctoText
    ),
    labelSmall = TextStyle(
        fontFamily = IbmPlexMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.8.sp,
        color = OctoGray
    )
)

