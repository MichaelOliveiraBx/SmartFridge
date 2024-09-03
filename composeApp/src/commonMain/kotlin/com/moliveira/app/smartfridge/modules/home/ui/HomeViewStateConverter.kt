package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.modules.sdk.localizedString
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.char

val SimpleDateFormatter = LocalDate.Format {
    dayOfMonth()
    char('/')
    monthNumber()
    char('/')
    yearTwoDigits(2000)
}

class HomeViewStateConverter {

    operator fun invoke(
        internalState: HomeInternalState,
        firstScan: Boolean,
    ): HomeState = when (internalState) {
        is HomeInternalState.Idle -> {
            HomeState(
                bottomBannerText = if (firstScan) "On scan le code-barres" else null,
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
                bottomBannerText = "Et maintenant la date de péremption",
                productBanner = HomeProductBannerState(
                    name = internalState.foodModel.name.localizedString(),
                    thumbnail = internalState.foodModel.thumbnail,
                    expirationDate = null,
                ),
            )
        }

        is HomeInternalState.DateSettled -> {
            HomeState(
                bottomBannerText = if (firstScan) "C'est prêt a etre enregistré !" else null,
                productBanner = HomeProductBannerState(
                    name = internalState.foodModel.name.localizedString(),
                    thumbnail = internalState.foodModel.thumbnail,
                    expirationDate = SimpleDateFormatter.format(internalState.date),
                ),
            )
        }
    }
}