@file:OptIn(ExperimentalAnimationApi::class)

package com.moliveira.app.smartfridge.modules.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.InternalVoyagerApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.internal.BackHandler
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.cta_continue
import com.moliveira.app.smartfridge.ic_onboarding
import com.moliveira.app.smartfridge.modules.design.Button
import com.moliveira.app.smartfridge.modules.design.ButtonType
import com.moliveira.app.smartfridge.modules.home.ui.HomeScreenDestination
import com.moliveira.app.smartfridge.modules.notification.LocalNotificationService
import com.moliveira.app.smartfridge.modules.sdk.LocalSharedPrefs
import com.moliveira.app.smartfridge.modules.theme.SFColors
import com.moliveira.app.smartfridge.onboarding_description_1
import com.moliveira.app.smartfridge.onboarding_description_2
import com.moliveira.app.smartfridge.onboarding_description_3
import com.moliveira.app.smartfridge.onboarding_title_1
import com.moliveira.app.smartfridge.onboarding_title_2
import com.moliveira.app.smartfridge.onboarding_title_3
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


private val steps = listOf(
    SimpleStep(
        title = Res.string.onboarding_title_1,
        subtitle = Res.string.onboarding_description_1,
    ),
    SimpleStep(
        title = Res.string.onboarding_title_2,
        subtitle = Res.string.onboarding_description_2,
    ),
    SimpleStep(
        title = Res.string.onboarding_title_3,
        subtitle = Res.string.onboarding_description_3,
    ),
)

class OnboardingScreenDestination : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val sharedPrefs = LocalSharedPrefs.current
        val coroutineScope = rememberCoroutineScope()

        OnboardingScreen(
            close = {
                coroutineScope.launch {
                    sharedPrefs.setOnboardingDisplayed()
                    navigator.push(HomeScreenDestination())
                }
            }
        )
    }
}

@OptIn(InternalVoyagerApi::class)
@Composable
fun OnboardingScreen(
    close: () -> Unit,
) {
    val notificationService = LocalNotificationService.current
    val localSteps = remember {
        steps + listOf(
            OnboardingStepNotification(notificationService),
        )
    }
    var stepIndex by remember { mutableStateOf(0) }
    val step = remember(stepIndex) { localSteps[stepIndex] }

    val nextRouteFct = {
        if (stepIndex < localSteps.size - 1) {
            stepIndex++
        } else {
            close()
        }
    }

    val previousRouteFct = {
        if (stepIndex > 0) {
            stepIndex--
        }
    }

    val coroutineScope = rememberCoroutineScope()

    BackHandler(
        enabled = true,
    ) {
        previousRouteFct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = SFColors.primary._300,
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(0.7f),
            painter = painterResource(Res.drawable.ic_onboarding),
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.weight(1f))
        AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            targetState = step,
            transitionSpec = {
                slideInHorizontally { fullWidth -> fullWidth } togetherWith
                        slideOutHorizontally { fullWidth -> -fullWidth }
            }
        ) {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                it.content()
            }
        }
        Spacer(modifier = Modifier.weight(1.4f))

        Button(
            modifier = Modifier.fillMaxWidth(0.6f).height(48.dp),
            text = stringResource(Res.string.cta_continue),
            type = ButtonType.PRIMARY,
            onClick = {
                coroutineScope.launch {
                    if (step.onContinueClick()) {
                        nextRouteFct()
                    }
                }
            },
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
