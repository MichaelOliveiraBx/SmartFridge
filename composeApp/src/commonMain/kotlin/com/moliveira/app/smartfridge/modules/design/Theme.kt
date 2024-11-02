package com.moliveira.app.smartfridge.modules.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moliveira.app.smartfridge.modules.theme.SFColors

data class Colors(
    val backgroundColor: Color,
    val primaryColor: Color,
    val textOnBackground: Color,
    val textOnPrimary: Color,
    val buttonPrimary: Color,
    val buttonSecondary: Color,
    val buttonTextPrimary: Color,
    val buttonTextSecondary: Color,
)

object ColorsThemeMode {
    fun light() = Colors(
        backgroundColor = Color.White,
        primaryColor = Color(0xFF63C0AE),
        textOnBackground = Color(0xFF63C0AE),
        textOnPrimary = Color.White,
        buttonPrimary = SFColors.secondary._500,
        buttonSecondary = Color.White,
        buttonTextPrimary = Color.White,
        buttonTextSecondary = SFColors.secondary._500,
    )

    fun dark() = Colors(
        backgroundColor = Color.White,
        primaryColor = Color(0xFF63C0AE),
        textOnBackground = Color(0xFF63C0AE),
        textOnPrimary = Color.White,
        buttonPrimary = Color(0xFF63C0AE),
        buttonSecondary = Color.White,
        buttonTextPrimary = Color.White,
        buttonTextSecondary = Color(0xFF63C0AE),
    )
}

object ColorsTheme {
    val colors
        @Composable
        get() = LocalColors.current
}


val LocalColors = staticCompositionLocalOf {
    ColorsThemeMode.light()
}

@Composable
private fun typography() = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = ColorsTheme.colors.textOnBackground,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = ColorsTheme.colors.textOnBackground,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = ColorsTheme.colors.primaryColor,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = ColorsTheme.colors.primaryColor,
    ),
)


@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) ColorsThemeMode.dark() else ColorsThemeMode.light()
    CompositionLocalProvider(
        LocalColors provides colors,
    ) {
        MaterialTheme(
            typography = typography(),
            shapes = Shapes(
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(16.dp),
            ),
            content = content
        )
    }
}