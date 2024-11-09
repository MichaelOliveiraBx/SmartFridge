package com.moliveira.app.smartfridge.modules.home.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
//import network.chaintech.kmp_date_time_picker.ui.datepicker.WheelDatePickerView
//import network.chaintech.kmp_date_time_picker.utils.DateTimePickerView
//import network.chaintech.kmp_date_time_picker.utils.WheelPickerDefaults

@Composable
fun DatePicker(
    showDatePicker: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
//    WheelDatePickerView(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 22.dp, bottom = 26.dp),
//        showDatePicker = showDatePicker,
//        title = "DUE DATE",
//        doneLabel = "Done",
//        titleStyle = TextStyle(
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFF333333),
//        ),
//        doneLabelStyle = TextStyle(
//            fontSize = 16.sp,
//            fontWeight = FontWeight(600),
//            color = Color(0xFF007AFF),
//        ),
//        dateTextColor = Color(0xff007AFF),
//        selectorProperties = WheelPickerDefaults.selectorProperties(
//            borderColor = Color.LightGray,
//        ),
//        rowCount = 5,
//        height = 180.dp,
//        dateTextStyle = TextStyle(
//            fontWeight = FontWeight(600),
//        ),
//        dragHandle = {
//            HorizontalDivider(
//                modifier = Modifier.padding(top = 8.dp).width(50.dp).clip(CircleShape),
//                thickness = 4.dp,
//                color = Color(0xFFE8E4E4)
//            )
//        },
//        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
//        dateTimePickerView = DateTimePickerView.DIALOG_VIEW,
//        onDoneClick = {
//            onDateSelected(it)
//        },
//        onDismiss = onDismiss
//    )
}