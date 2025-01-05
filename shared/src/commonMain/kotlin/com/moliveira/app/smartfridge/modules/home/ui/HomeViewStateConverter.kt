package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.home_bottom_text_scan_barcode
import com.moliveira.app.smartfridge.home_bottom_text_scan_date
import com.moliveira.app.smartfridge.home_bottom_text_scan_ready
import com.moliveira.app.smartfridge.home_title_2
import com.moliveira.app.smartfridge.modules.sdk.localizedString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.getString

val SimpleDateFormatter = LocalDate.Format {
    dayOfMonth()
    char('/')
    monthNumber()
    char('/')
    yearTwoDigits(2000)
}

class HomeViewStateConverter {

    suspend operator fun invoke(
        internalState: HomeInternalState,
        firstScan: Boolean,
        buttonIsLoading: Boolean,
    ): HomeState = when (internalState) {
        is HomeInternalState.Idle -> {
            HomeState(
                bottomBannerText = if (firstScan) getString(Res.string.home_bottom_text_scan_barcode) else null,
                productBanner = null,
            )
        }

        is HomeInternalState.ProductInSearch -> {
            HomeState(
                productBanner = HomeProductBannerState(
                    name = null,
                    thumbnail = null,
                    expirationDate = null,
                ),
            )
        }

        is HomeInternalState.ProductFound -> {
            HomeState(
                bottomBannerText = getString(Res.string.home_bottom_text_scan_date),
                productBanner = HomeProductBannerState(
                    name = internalState.foodModel.name.localizedString(),
                    thumbnail = internalState.foodModel.thumbnail,
                    expirationDate = null,
                    buttonIsLoading = buttonIsLoading,
                ),
            )
        }

        is HomeInternalState.DateSettled -> {
            HomeState(
                bottomBannerText = if (firstScan) getString(Res.string.home_bottom_text_scan_ready) else null,
                productBanner = HomeProductBannerState(
                    name = internalState.foodModel.name.localizedString(),
                    thumbnail = internalState.foodModel.thumbnail,
                    expirationDate = SimpleDateFormatter.format(internalState.date),
                    buttonIsLoading = buttonIsLoading,
                ),
            )
        }
    }
}