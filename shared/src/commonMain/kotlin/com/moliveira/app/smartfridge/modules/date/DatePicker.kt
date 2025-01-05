@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.moliveira.app.smartfridge.modules.date

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.common_validate
import com.moliveira.app.smartfridge.cta_cancel
import com.moliveira.app.smartfridge.modules.design.Button
import com.moliveira.app.smartfridge.modules.design.ButtonType
import com.moliveira.app.smartfridge.modules.theme.SFColors
import io.github.aakira.napier.Napier
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

class RangeSelectableDates(
    private val startDate: LocalDate,
) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        val date = utcTimeMillis.convertMillisToDate().getOrNull() ?: return false
        return date >= startDate
    }

    override fun isSelectableYear(year: Int): Boolean {
        return year >= startDate.year
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val datePickerState = rememberDatePickerState(
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = RangeSelectableDates(date),
    )
    val calendarPickerMainColor = SFColors.secondary._500

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        Row {
            Button(modifier = Modifier.height(48.dp),
                type = ButtonType.PRIMARY,
                text = stringResource(Res.string.common_validate),
                paddingValues = PaddingValues(horizontal = 12.dp),
                onClick = {
                    datePickerState.selectedDateMillis?.convertMillisToDate()
                        ?.onSuccess { onDateSelected(it) }?.onFailure {
                            Napier.e("Error converting date", it)
                        }
                })
            Spacer(modifier = Modifier.width(16.dp))
        }
    }, dismissButton = {
        // Dismiss button to close the dialog without selecting a date
        Button(
            modifier = Modifier.height(48.dp),
            type = ButtonType.SECONDARY,
            text = stringResource(Res.string.cta_cancel),
            paddingValues = PaddingValues(horizontal = 12.dp),
            onClick = onDismiss,
        )
    }) {
        // The actual DatePicker component within the dialog
        androidx.compose.material3.DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color.White,
                selectedDayContainerColor = SFColors.primary._300,
                selectedDayContentColor = Color.White,
                selectedYearContainerColor = SFColors.primary._300,
                selectedYearContentColor = Color.White,
                todayContentColor = calendarPickerMainColor,
                todayDateBorderColor = SFColors.primary._300,
            ),
        )
    }
}

@Composable
expect fun DatePickerPlatform(modifier: Modifier)

// Create function which convert millis from epoch to kotlinx LocalDate
fun Long.convertMillisToDate(): Result<LocalDate> = runCatching {
    LocalDate.fromEpochDays((this / 86400000).toInt())
}