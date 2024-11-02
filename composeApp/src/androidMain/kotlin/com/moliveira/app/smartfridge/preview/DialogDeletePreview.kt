package com.moliveira.app.smartfridge.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.moliveira.app.smartfridge.modules.home.ui.DialogDelete

@Composable
@Preview
fun DialogDeletePreview() {
    DialogDelete(
        product = "Arroz",
    )
}