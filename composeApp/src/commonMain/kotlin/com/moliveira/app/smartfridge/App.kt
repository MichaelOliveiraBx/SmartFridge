package com.moliveira.app.smartfridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import com.moliveira.app.smartfridge.modules.design.AppTheme
import com.moliveira.app.smartfridge.modules.home.ui.HomeScreenDestination
import com.moliveira.app.smartfridge.modules.notification.LocalNotificationService
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.onboarding.OnboardingScreenDestination
import com.moliveira.app.smartfridge.modules.sdk.LocalSharedPrefs
import com.moliveira.app.smartfridge.modules.sdk.SharedPrefs
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val notificationService = AppModule.koinApplication.koin.get<NotificationService>()
    val sharedPrefs = AppModule.koinApplication.koin.get<SharedPrefs>()

    val onboardingDisplayed by sharedPrefs.isOnboardingDisplayedFlow.collectAsState()
    val startingDestination = if (onboardingDisplayed) {
        HomeScreenDestination()
    } else {
        OnboardingScreenDestination()
    }

    CompositionLocalProvider(
        LocalNotificationService provides notificationService,
        LocalSharedPrefs provides sharedPrefs,
    ) {
        AppTheme {
            Navigator(
                screen = startingDestination,
            )
        }
    }
}