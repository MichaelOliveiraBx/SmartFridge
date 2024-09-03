package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val foodRepository: FoodRepository,
    private val converter: HomeViewStateConverter,
    private val notificationService: NotificationService,
    private val database: FoodDatabase,
) : BaseScreenModel<HomeState, HomeUiEffect>(
    HomeState()
) {

    private val isFirstScanFlow = MutableStateFlow(true)
    private val internalStateFlow = MutableStateFlow<HomeInternalState>(HomeInternalState.Idle)
    override val uiStateProvider: Flow<HomeState>
        get() = combine(
            internalStateFlow,
            isFirstScanFlow,
        ) { internalState, firstScan ->
            converter(internalState, firstScan)
        }

    fun onBarcodeRecognized(text: String) {
        Napier.w("onBarcodeRecognized: $text")
        viewModelScope.launch(Dispatchers.Default) {
            if (internalStateFlow.value !is HomeInternalState.Idle) return@launch

            internalStateFlow.value = HomeInternalState.ProductInSearch
            Napier.w("onBarcodeRecognized: getFoodById $text")
            foodRepository.getFoodById(text)
                .onSuccess {
                    Napier.w("onBarcodeRecognized: getFoodById success $it")
                    internalStateFlow.value = HomeInternalState.ProductFound(it)
                }
                .onFailure {
                    Napier.w("onBarcodeRecognized: getFoodById failure $it")
                    HomeInternalState.Idle
                    sendUiEffect(HomeUiEffect.DisplayMessage("Product not found"))
                }
        }
    }


    private val dateRegex = Regex("""\d{1,2}/\d{1,2}/\d{2,4}""")
    private val pointDateRegex = Regex("""\d{1,2}.\d{1,2}.\d{2,4}""")
    fun onTextRecognized(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.ProductFound) ?: return@launch

            val textMatch = pointDateRegex.find(text)
                ?.value?.replace(".", "/")
                ?.also { Napier.w("onTextRecognized: date found pointDateRegex $it") }
                ?: dateRegex.find(text)?.value
                    ?.also { Napier.w("onTextRecognized: date found slash $it") }
                ?: return@launch

            val yearSize = textMatch.split("/").last().length
            Napier.w("onTextRecognized: date found $textMatch, yearSize: $yearSize")
            runCatching {
                val dateFormat = LocalDate.Format {
                    dayOfMonth()
                    char('/')
                    monthNumber()
                    char('/')
                    if (yearSize == 2) yearTwoDigits(2000) else year()
                }
                LocalDate.parse(text, dateFormat)
            }.onSuccess {
                Napier.i("onTextRecognized: date parsed $it")
                internalStateFlow.value = HomeInternalState.DateSettled(
                    foodModel = productFoundState.foodModel,
                    date = it,
                )
            }
                .onFailure {
                    Napier.w("onTextRecognized: date parse failure $it")
                }
        }
    }

    fun onAddProduct() {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.DateSettled) ?: return@launch
            Napier.w("onAddProduct: $productFoundState")
            notificationService.scheduleNotification(
                title = "Hé oh !!",
                body = "Tes ${productFoundState.foodModel.name} vont bientôt périmer",
                icon = null,
                localDateTime = Clock.System.now()
                    .plus(1, DateTimeUnit.MINUTE)
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
            )
                .onSuccess {
                    database.addUserFood(
                        model = productFoundState.foodModel,
                        notificationId = it,
                        expirationDate = productFoundState.date,
                    )
                }
            isFirstScanFlow.value = false
            internalStateFlow.value = HomeInternalState.Idle
        }
    }

    fun onDeleteProduct() {
        Napier.w("onDeleteProduct")
        internalStateFlow.value = HomeInternalState.Idle
    }
}