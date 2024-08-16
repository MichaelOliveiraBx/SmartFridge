package com.moliveira.app.smartfridge.modules.food.database

import com.moliveira.app.smartfridge.database.cache.CacheApiDatabase
import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.sdk.LocalizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class FoodDatabase(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = CacheApiDatabase(databaseDriverFactory.createDriver("apiCacheFood.db"))
    private val dbQuery = database.cacheApiDatabaseQueries

    suspend fun getFoodById(id: String): FoodModel? = withContext(Dispatchers.IO) {
        dbQuery.selectById(id).executeAsOneOrNull()?.toFoodModel()
    }

    suspend fun addFood(foodModel: FoodModel) = withContext(Dispatchers.IO) {
        dbQuery.insertFood(
            id = foodModel.id,
            name_en = foodModel.name.en,
            name_fr = foodModel.name.fr,
            thumbnail = foodModel.thumbnail,
        )
    }

    private fun com.moliveira.app.smartfridge.database.cache.FoodModelEntity.toFoodModel(): FoodModel =
        FoodModel(
            id = id,
            name = LocalizedString(
                en = name_en,
                fr = name_fr,
            ),
            thumbnail = thumbnail,
        )
}