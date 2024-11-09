package com.moliveira.app.smartfridge.modules.date

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import platform.Foundation.NSDate

@Composable
actual fun DatePickerPlatform(
    modifier: Modifier,
) {
    val now = NSDate()
    IOSDatePicker(
        modifier = modifier,
        selectedDate = now,
        onDateChanged = { date -> println("Date changed: $date") }
    )
}