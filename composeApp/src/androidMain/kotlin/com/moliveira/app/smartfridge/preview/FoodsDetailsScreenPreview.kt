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
                foodItems = emptyList(),
            )
        )
    }
}