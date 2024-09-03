package com.moliveira.app.smartfridge.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.moliveira.app.smartfridge.modules.design.AppTheme
import com.moliveira.app.smartfridge.modules.home.ui.FoodItemState
import com.moliveira.app.smartfridge.modules.home.ui.FoodsDetailsScreen
import com.moliveira.app.smartfridge.modules.home.ui.FoodsDetailsScreenState

@Preview
@Composable
fun FoodsDetailsScreenPreview() {
    AppTheme {
        FoodsDetailsScreen(
            state = FoodsDetailsScreenState(
                foodItems = listOf(
                    FoodItemState(
                        name = "Banana",
                        thumbnail = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                        expirationDate = "12/31/2022",
                    ),
                    FoodItemState(
                        name = "Apple",
                        thumbnail = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                        expirationDate = "12/31/2022",
                    ),
                    FoodItemState(
                        name = "Orange",
                        thumbnail = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png",
                        expirationDate = "12/31/2022",
                    ),
                )
            )
        )
    }
}