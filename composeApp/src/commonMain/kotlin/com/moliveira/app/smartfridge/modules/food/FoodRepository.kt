package com.moliveira.app.smartfridge.modules.food

import com.moliveira.app.smartfridge.modules.food.api.FoodApiClient
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class FoodRepository(
    private val foodDatabase: FoodDatabase,
    private val foodApiClient: FoodApiClient,
) {
    suspend fun getFoodById(id: String): Result<FoodModel> = withContext(Dispatchers.IO) {
        foodDatabase.getFoodById(id)?.let {
            Result.success(it)
        } ?: run {
            foodApiClient.getFoodById(id).onSuccess {
                foodDatabase.addFood(it)
            }
        }
    }
}