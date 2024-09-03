package com.moliveira.app.smartfridge.modules.food.database

import com.moliveira.app.smartfridge.database.cache.CacheApiDatabase
import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserFoodModel
import com.moliveira.app.smartfridge.modules.sdk.LocalizedString
import com.moliveira.app.smartfridge.modules.sdk.localizedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

class FoodDatabase(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = CacheApiDatabase(databaseDriverFactory.createDriver("apiCacheFood.db"))
    private val dbQuery = database.cacheApiDatabaseQueries
    private val dbQueryFoodUser = database.userFoodTableQueries

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

    suspend fun addUserFood(
        model : FoodModel,
        notificationId: String,
        expirationDate: LocalDate,
    ) {
        withContext(Dispatchers.IO) {
            dbQueryFoodUser.insertUserFood(
                id = model.id,
                name = model.name.localizedString(),
                thumbnail = model.thumbnail.orEmpty(),
                expirationDate = expirationDate.dbFormat(),
                notificationId = notificationId,
                addAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    .dbFormat(),
            )
        }
    }

    suspend fun getAllUserFood(): List<UserFoodModel> = withContext(Dispatchers.IO) {
        dbQueryFoodUser.selectUserFoods(::toUserFoodModel).executeAsList()
    }

    private fun toUserFoodModel(
        id: String,
        name: String,
        thumbnail: String,
        addAt: String,
        expirationDate: String,
        notificationId: String?,
    ): UserFoodModel = UserFoodModel(
        id = id,
        name = name,
        thumbnail = thumbnail,
        addAt = addAt.dbParse(),
        expirationDate = expirationDate.localDatedbParse(),
        notificationId = notificationId,
    )

    private fun LocalDateTime.dbFormat(): String = format(LocalDateTime.Formats.ISO)

    private fun String.dbParse(): LocalDateTime =
        LocalDateTime.parse(this, LocalDateTime.Formats.ISO)

    private fun LocalDate.dbFormat(): String = format(LocalDate.Formats.ISO)

    private fun String.localDatedbParse(): LocalDate =
        LocalDate.parse(this, LocalDate.Formats.ISO)

}