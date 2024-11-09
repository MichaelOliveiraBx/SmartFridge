@file:OptIn(ExperimentalForeignApi::class, ExperimentalForeignApi::class)

package com.moliveira.app.smartfridge.modules.date

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIDatePicker
import platform.UIKit.UIDatePickerMode
import platform.UIKit.UIDatePickerStyle
import platform.UIKit.UITextField
import platform.darwin.NSObject

@Composable
fun IOSDatePicker(
    modifier: Modifier,
    selectedDate: NSDate,
    onDateChanged: (NSDate) -> Unit
) {
    val handler = remember { DatePickerHandler(onDateChanged) }

    UIKitView(
        modifier = modifier.height(400.dp).fillMaxWidth(),
        factory = {
            val datePicker = UIDatePicker().apply {
                datePickerMode = UIDatePickerMode.UIDatePickerModeDate
                setPreferredDatePickerStyle(UIDatePickerStyle.UIDatePickerStyleInline)
            }

            datePicker.addTarget(
                target = handler,
                action = NSSelectorFromString("dateChanged:"),
                forControlEvents = UIControlEventValueChanged
            )

            datePicker
        },
        update = { view ->
            view.date = selectedDate
        }
    )
//
//    var datePickerHeight by remember { mutableStateOf(0.dp) }
//
//    // Cible pour gérer les événements de changement de date
//    val handler = remember { DatePickerHandler(onDateChanged) }
//
//    val density = LocalDensity.current
//
//    UIKitView(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(datePickerHeight) // Applique la hauteur de `UIDatePicker`
//            .onGloballyPositioned { coordinates ->
//                // Obtenez la hauteur réelle du `UIDatePicker`
//                with(density) {
//                    val heightInDp = coordinates.size.height.toDp()
//                    if (datePickerHeight != heightInDp) {
//                        datePickerHeight = heightInDp
//                    }
//                }
//            },
//        factory = {
//            val datePicker = UIDatePicker().apply {
//                datePickerMode = UIDatePickerMode.UIDatePickerModeDate
//                setPreferredDatePickerStyle(UIDatePickerStyle.UIDatePickerStyleInline)
//                date = selectedDate
//                addTarget(
//                    target = handler,
//                    action = NSSelectorFromString("onDateChanged:"),
//                    forControlEvents = UIControlEventValueChanged
//                )
//            }
//            datePicker
//        },
//        update = { view ->
//            view.date = selectedDate
//        }
//    )
}

// Cible pour le gestionnaire de l'événement
@OptIn(BetaInteropApi::class)
class DatePickerHandler(
    private val onDateChanged: (NSDate) -> Unit
) : NSObject() {

    @ObjCAction
    fun onDateChanged(sender: UIDatePicker) {
        onDateChanged(sender.date)
    }
}