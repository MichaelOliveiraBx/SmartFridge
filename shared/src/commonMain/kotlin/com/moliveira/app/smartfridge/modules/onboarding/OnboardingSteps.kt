package com.moliveira.app.smartfridge.modules.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moliveira.app.smartfridge.modules.theme.SFColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource


interface Step {
    @Composable
    fun content()

    suspend fun onContinueClick(): Boolean = true
}

data class SimpleStep(
    val title: StringResource,
    val subtitle: StringResource,
) : Step {

    @Composable
    override fun content() {
        OnboardingStepString(
            title = stringResource(title),
            subtitle = stringResource(subtitle),
        )
    }
}

@Composable
private fun OnboardingStepString(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(0.7f),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge
                .copy(
                    fontWeight = FontWeight.Bold,
                ),
            color = SFColors.secondary._500,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.headlineSmall,
            color = SFColors.secondary._500,
            textAlign = TextAlign.Center,
        )
    }
}