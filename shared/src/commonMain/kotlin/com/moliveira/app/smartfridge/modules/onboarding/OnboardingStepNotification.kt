package com.moliveira.app.smartfridge.modules.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.cta_authorize
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.theme.SFColors
import com.moliveira.app.smartfridge.onboarding_description_4
import com.moliveira.app.smartfridge.onboarding_description_banner
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class OnboardingStepNotification(
    private val notificationService: NotificationService,
) : Step {
    private var isLoading = mutableStateOf(false)
    private var buttonEnable = mutableStateOf(true)
    private var checkVisible = mutableStateOf(false)
    private var textVisible = mutableStateOf(true)
    private var continueEnable = mutableStateOf(false)

    private suspend fun askPermFct() {
        isLoading.value = true
        buttonEnable.value = false

        if (notificationService.askForPermission()) {
            buttonEnable.value = false
            checkVisible.value = true
            textVisible.value = false
        } else {
            buttonEnable.value = true
            textVisible.value = true
        }
        continueEnable.value = true
        isLoading.value = false
    }

    override suspend fun onContinueClick(): Boolean {
        if (continueEnable.value) {
            return true
        } else {
            askPermFct()
            return false
        }
    }

    @Composable
    override fun content() {
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.onboarding_description_4),
                style = MaterialTheme.typography.headlineSmall,
                color = SFColors.secondary._500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        color = SFColors.secondary._100,
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(
                        top = 28.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 20.dp,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.onboarding_description_banner))
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(" Smart Fridge")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = SFColors.secondary._500,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = SFColors.secondary._500,
                    ),
                    enabled = buttonEnable.value,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 2.dp,
                        color = SFColors.secondary._500,
                    ),
                    onClick = {
                        coroutineScope.launch { askPermFct() }
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        val alphaText by animateFloatAsState(targetValue = if (textVisible.value) 1f else 0f)
                        Text(
                            modifier = Modifier
                                .alpha(alphaText),
                            text = stringResource(Res.string.cta_authorize),
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                        val alphaProgress by animateFloatAsState(targetValue = if (isLoading.value) 1f else 0f)
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .alpha(alphaProgress),
                            strokeWidth = 4.dp
                        )
                        val alphaCheck by animateFloatAsState(targetValue = if (checkVisible.value) 1f else 0f)
                        Icon(
                            modifier = Modifier
                                .size(28.dp)
                                .alpha(alphaCheck),
                            imageVector = Icons.Default.Check,
                            contentDescription = "Check",
                        )
                    }
                }
            }
        }
    }
}