package com.example.educationalapp.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.educationalapp.ui.theme.KidFontFamily

// Set of Material typography styles to start with
// A refreshed typography set with larger sizes and a kid‑friendly font.
// Research on early childhood literacy recommends typefaces with simple,
// generous letterforms and larger x‑heights【412315585523685†L265-L276】.  We use the
// existing KidFontFamily to achieve this and bump the base sizes to improve
// legibility on tablets held at arm’s length.  Additional styles are defined
// for titles and labels so that the UI remains harmonious across screens.
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = KidFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = KidFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = KidFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = KidFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = KidFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = KidFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
