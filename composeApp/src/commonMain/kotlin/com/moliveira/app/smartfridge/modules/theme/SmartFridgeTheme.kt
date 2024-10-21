package com.moliveira.app.smartfridge.modules.theme

import androidx.compose.ui.graphics.Color

object SFColors {
    val primary = SFTypeColors(
        _100 = Color(0xFFE0F4FE),
        _200 = Color(0xFF72D6FB),
        _300 = Color(0xFF35B0D4),
        _400 = Color(0xFF2788A4),
        _500 = Color(0xFF1A6277),
    )
    val secondary = SFTypeColors(
        _100 = Color(0xFFD3D9F9),
        _200 = Color(0xFFA2B0F3),
        _300 = Color(0xFF6C87EC),
        _400 = Color(0xFF3561D4),
        _500 = Color(0xFF224193),
    )
}

data class SFTypeColors(
    val _100: Color,
    val _200: Color,
    val _300: Color,
    val _400: Color,
    val _500: Color,
)