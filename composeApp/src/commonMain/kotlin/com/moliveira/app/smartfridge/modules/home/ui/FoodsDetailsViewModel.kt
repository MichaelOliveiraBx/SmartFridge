package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.food.domain.UserFoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserNotificationModel
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.handleNotificationTime
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import com.moliveira.app.smartfridge.notification_title
import com.moliveira.app.smartfridge.notification_title_description
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.format
import org.jetbrains.compose.resources.getString

class FoodsDetailsViewModel(
    private val database: FoodDatabase,
    private val repository: FoodRepository,
    private val notificationService: NotificationService,
) : BaseScreenModel<FoodsDetailsScreenState, Unit>(
    FoodsDetailsScreenState(
        foodItems = List(6) {
            FoodItemState()
        }
    )
) {

    private val notificationFlow = MutableStateFlow<List<UserNotificationModel>?>(null)

    override val uiStateProvider: Flow<FoodsDetailsScreenState>
        get() = combine(
            repository.userFoodFlow.mapNotNull { it },
            notificationFlow.mapNotNull { it },
        ) { userFood, notifications ->
            FoodsDetailsScreenState(
                foodItems = userFood.map { food ->
                    FoodItemState(
                        id = food.id,
                        name = food.name,
                        thumbnail = food.thumbnail,
                        expirationDate = food.expirationDate.format(SimpleDateFormatter),
                        notificationEnable = notifications.any { it.id == food.id }
                    )
                }
            )
        }
    
    fun onResume() {
        refreshNotif()
    }

    private fun refreshNotif() {
        viewModelScope.launch(Dispatchers.Default) {
            notificationFlow.value = database.allNotificationModel()
        }
    }

    fun onNotificationClick(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val product = repository.userFood?.find { it.id == id } ?: return@launch
            Napier.d("onNotificationClick: $id")
            if (database.hasNotificationById(id)) {
                Napier.d("hasNotification: true")
                notificationFlow.value = notificationFlow.value?.filter { it.id != id }
                notificationService.cancelNotification(id)
                database.removeNotificationId(id)
            } else {
                Napier.d("hasNotification: false")
                product.expirationDate.handleNotificationTime()
                    ?.let {
                        notificationService.scheduleNotification(
                            title = getString(Res.string.notification_title),
                            body = getString(
                                Res.string.notification_title_description,
                                product.name
                            ),
                            icon = null,
                            localDateTime = it,
                        ).onSuccess {
                            Napier.d("scheduleNotification: $it")
                            notificationFlow.value = notificationFlow.value.orEmpty() + listOf(
                                UserNotificationModel(
                                    id = id,
                                    uuid = it,
                                )
                            )
                            database.addNotificationId(id, it)
                        }
                    }
            }
        }
    }

    fun onDeleteClick(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            Napier.d("onDeleteClick: $id")
            repository.userFood?.find { it.id == id } ?: return@launch
            notificationService.cancelNotification(id)
            repository.removeUserFood(id)
        }
    }
}