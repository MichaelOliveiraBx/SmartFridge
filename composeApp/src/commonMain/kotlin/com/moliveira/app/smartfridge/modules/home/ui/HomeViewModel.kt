package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.handleNotificationTime
import com.moliveira.app.smartfridge.modules.notification.notificationGetTitle
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import com.moliveira.app.smartfridge.modules.sdk.LocalizedString
import com.moliveira.app.smartfridge.modules.sdk.localizedString
import com.moliveira.app.smartfridge.notification_title_description
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.seconds

class HomeViewModel(
    private val foodRepository: FoodRepository,
    private val converter: HomeViewStateConverter,
    private val notificationService: NotificationService,
) : BaseScreenModel<HomeState, HomeUiEffect>(
    HomeState()
) {

    private val isFirstScanFlow = MutableStateFlow(true)
    private val internalStateFlow = MutableStateFlow<HomeInternalState>(HomeInternalState.Idle)
    private val buttonIsLoadingFlow = MutableStateFlow(false)
    override val uiStateProvider: Flow<HomeState>
        get() = combine(
            internalStateFlow,
            isFirstScanFlow,
            buttonIsLoadingFlow,
        ) { internalState, firstScan, buttonIsLoading ->
            converter(internalState, firstScan, buttonIsLoading)
        }

    fun onBarcodeRecognized(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (internalStateFlow.value !is HomeInternalState.Idle) return@launch

            internalStateFlow.value = HomeInternalState.ProductInSearch
            foodRepository.getFoodById(text)
                .onSuccess {
                    Napier.i("HHHH onBarcodeRecognized: getFoodById success $it")
                    internalStateFlow.value = HomeInternalState.ProductFound(it)
                }
                .onFailure {
                    Napier.w("onBarcodeRecognized: getFoodById failure $it")
                    HomeInternalState.Idle
                    sendUiEffect(HomeUiEffect.DisplayMessage("Product not found"))
                }
        }
    }

    fun onTextRecognized(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.ProductFound) ?: return@launch

            DateRecognizer.invoke(text)?.let {
                Napier.i("onTextRecognized: date parsed $it")
                internalStateFlow.value = HomeInternalState.DateSettled(
                    foodModel = productFoundState.foodModel,
                    date = it,
                )
            }
        }
    }

    fun onExpireDateClick() {
        val localDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        sendUiEffectAsync(
            HomeUiEffect.DisplayDatePicker(localDate)
        )
    }

    fun onExpireDateSettled(date: LocalDate) {
        viewModelScope.launch {
            val foodModel = when (val state = internalStateFlow.value) {
                is HomeInternalState.ProductFound -> state.foodModel
                is HomeInternalState.DateSettled -> state.foodModel
                else -> return@launch
            }

            internalStateFlow.value = HomeInternalState.DateSettled(
                foodModel = foodModel,
                date = date,
            )
        }
    }

    fun onAddProduct() {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.DateSettled) ?: return@launch
            Napier.d("onAddProduct: productFoundState:$productFoundState")
            val notificationTime = productFoundState.date.handleNotificationTime()
                ?: run {
                    Napier.w("onAddProduct: notificationTime null")
                    sendUiEffect(
                        HomeUiEffect.DisplayMessage(
                            "Sorry the date is already expired"
                        )
                    )
                    return@launch
                }
//            val notificationTime =
//                Clock.System.now()
//                    .plus(30.seconds)
//                    .toLocalDateTime(TimeZone.currentSystemDefault())

            notificationService.scheduleNotification(
                title = notificationGetTitle(),
                body = getString(
                    Res.string.notification_title_description,
                    productFoundState.foodModel.name.localizedString(),
                ),
                icon = null,
                localDateTime = notificationTime,
            )
                .onSuccess { notificationId ->
                    foodRepository.addUserFood(
                        model = productFoundState.foodModel,
                        notificationId = notificationId,
                        expirationDate = productFoundState.date,
                    ).onFailure {
                        Napier.w("onAddProduct: addUserFood failure $it")
                        sendUiEffect(
                            HomeUiEffect.DisplayMessage(
                                "Sorry this product is already added"
                            )
                        )
                    }.onSuccess {
                        productFoundState.foodModel.thumbnail?.let {
                            sendUiEffect(
                                HomeUiEffect.StartAddAnimation(it)
                            )
                        }
                    }
                }
                .onFailure {
                    Napier.w("onAddProduct: scheduleNotification failure $it")
                    sendUiEffect(
                        HomeUiEffect.DisplayMessage(
                            "Sorry there is an error !"
                        )
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