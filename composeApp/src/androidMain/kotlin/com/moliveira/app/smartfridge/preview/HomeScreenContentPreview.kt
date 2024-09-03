package com.moliveira.app.smartfridge.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.moliveira.app.smartfridge.modules.design.AppTheme
import com.moliveira.app.smartfridge.modules.home.ui.HomeProductBannerState
import com.moliveira.app.smartfridge.modules.home.ui.HomeScreenContent
import com.moliveira.app.smartfridge.modules.home.ui.HomeState

@Preview
@Composable
fun HomeScreenContentPreview() {
    AppTheme {
        HomeScreenContent(
            state = HomeState(
                productBanner = HomeProductBannerState(
                    name = "Banana",
                    thumbnail = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                    expirationDate = "12/31/2022",
                ),
            )
        )
    }
}

private val backgroundColor = Color(0xFF7FCABB)