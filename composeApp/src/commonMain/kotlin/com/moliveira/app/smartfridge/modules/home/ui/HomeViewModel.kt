package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.handleNotificationTime
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import com.moliveira.app.smartfridge.modules.sdk.LocalizedString
import com.moliveira.app.smartfridge.notification_title
import com.moliveira.app.smartfridge.notification_title_description
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

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
        viewModelScope.launch(Dispatchers.Default) {
            if (internalStateFlow.value !is HomeInternalState.Idle) return@launch

            internalStateFlow.value = HomeInternalState.ProductInSearch
            foodRepository.getFoodById(text)
                .onSuccess {
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

    fun onAddProduct() {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.DateSettled) ?: return@launch
            Napier.w("onAddProduct: $productFoundState")
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
            notificationService.scheduleNotification(
                title = getString(Res.string.notification_title),
                body = getString(
                    Res.string.notification_title_description,
                    productFoundState.foodModel.name
                ),
                icon = null,
                localDateTime = notificationTime,
            )
                .onSuccess { notificationId ->
                    productFoundState.foodModel.thumbnail?.let {
                        sendUiEffect(
                            HomeUiEffect.StartAddAnimation(
                                it
                            )
                        )
                    }
                    database.addUserFood(
                        model = productFoundState.foodModel,
                        notificationId = notificationId,
                        expirationDate = productFoundState.date,
                    ).onSuccess {
                        Napier.w("onAddProduct: addUserFood success")
                        database.addNotificationId(
                            id = productFoundState.foodModel.id + "_" + productFoundState.date,
                            notificationId = notificationId,
                        )
                    }
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