package com.moliveira.app.smartfridge.modules.food

import com.moliveira.app.smartfridge.modules.food.api.FoodApiClient
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserFoodModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

@OptIn(DelicateCoroutinesApi::class)
class FoodRepository(
    private val foodDatabase: FoodDatabase,
    private val foodApiClient: FoodApiClient,
) {
    private val scope = GlobalScope

    private val _userFoodFlow = MutableStateFlow<List<UserFoodModel>?>(null)
    val userFoodFlow: Flow<List<UserFoodModel>?> = _userFoodFlow
        .onSubscription {
            if (userFood == null) {
                delay(500)
                refreshFood()
            }
        }

    val userFood: List<UserFoodModel>? get() = _userFoodFlow.value

    suspend fun getFoodById(id: String): Result<FoodModel> = withContext(Dispatchers.IO) {
        foodDatabase.getFoodById(id)?.let {
            Result.success(it)
        } ?: run {
            foodApiClient.getFoodById(id).onSuccess {
                foodDatabase.addFood(it)
            }
        }
    }

    suspend fun addUserFood(
        model: FoodModel,
        notificationId: String,
        expirationDate: LocalDate,
    ): Result<Unit> = foodDatabase.addUserFood(model, notificationId, expirationDate)
        .mapCatching {
            foodDatabase.addNotificationId(
                id = it,
                notificationId = notificationId,
            )
            refreshFood()
        }

    fun removeUserFood(id: String) {
        scope.launch {
            foodDatabase.deleteUserFood(id)
            foodDatabase.removeNotificationId(id)
            refreshFood()
        }
        _userFoodFlow.value = _userFoodFlow.value?.filter { it.id != id }
    }

    fun refreshFood() {
        scope.launch {
            _userFoodFlow.value = foodDatabase.getAllUserFood()
        }
    }

}