package com.moliveira.app.smartfridge.modules.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moliveira.app.smartfridge.modules.theme.SFColors

enum class ButtonType {
    PRIMARY,
    SECONDARY
}

@Composable
fun Button(
    modifier: Modifier = Modifier,
    text: String,
    type: ButtonType,
    enable: Boolean = true,
    onClick: () -> Unit,
    isLoading: Boolean = false,
) {
    val alphaAnimated by animateFloatAsState(if (enable) 1f else 0.5f)
    androidx.compose.material3.Button(
        modifier = modifier.alpha(alphaAnimated),
        colors = ButtonDefaults.buttonColors(
            containerColor = type.backgroundColor(),
            contentColor = type.textColor(),
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        enabled = enable,
        border = BorderStroke(1.dp, type.borderColor())
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val contentAlpha by animateFloatAsState(
                if (isLoading) 0f else 1f,
            )
            val isLoadingAlpha by animateFloatAsState(
                if (isLoading) 1f else 0f,
            )

            Text(
                modifier = Modifier.alpha(contentAlpha),
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            )

            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxHeight(0.7f)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .alpha(isLoadingAlpha),
                strokeWidth = 2.dp
            )
        }
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
    ButtonType.SECONDARY -> SFColors.secondary._500
}