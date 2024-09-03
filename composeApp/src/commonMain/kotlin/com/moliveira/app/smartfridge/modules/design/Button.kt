package com.moliveira.app.smartfridge.modules.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonType {
    PRIMARY,
    SECONDARY
}

@Composable
fun Button(
    modifier: Modifier = Modifier,
    text: String,
    type: ButtonType,
    enable : Boolean = true,
    onClick: () -> Unit,
) {
    val alphaAnimated by animateFloatAsState(if (enable) 1f else 0.5f)
    Box(
        modifier = modifier
            .alpha(alphaAnimated)
            .background(type.backgroundColor(), shape = RoundedCornerShape(12.dp))
            .border(1.dp, type.borderColor(), shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enable, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = type.textColor(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )
    }
}

@Composable
private fun ButtonType.backgroundColor() = when (this) {
    ButtonType.PRIMARY -> ColorsTheme.colors.buttonPrimary
    ButtonType.SECONDARY -> ColorsTheme.colors.buttonSecondary
}

@Composable
private fun ButtonType.textColor() = when (this) {
    ButtonType.PRIMARY -> ColorsTheme.colors.buttonTextPrimary
    ButtonType.SECONDARY -> ColorsTheme.colors.buttonTextSecondary
}

@Composable
private fun ButtonType.borderColor() = when (this) {
    ButtonType.PRIMARY -> Color.Transparent
    ButtonType.SECONDARY -> Color(0xFF63C0AE)
}