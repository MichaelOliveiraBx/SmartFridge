package com.moliveira.app.smartfridge

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.moliveira.app.smartfridge.modules.design.AppTheme
import com.moliveira.app.smartfridge.modules.home.ui.HomeScreenDestination
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    AppTheme {
        Navigator(
            screen = HomeScreenDestination(),
        )
    }
}