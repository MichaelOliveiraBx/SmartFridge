package com.moliveira.app.smartfridge.modules.food.database

import com.moliveira.app.smartfridge.database.cache.CacheApiDatabase
import com.moliveira.app.smartfridge.database.cache.DatabaseDriverFactory
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserFoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserNotificationModel
import com.moliveira.app.smartfridge.modules.sdk.LocalizedString
import com.moliveira.app.smartfridge.modules.sdk.localizedString
import io.github.aakira.napier.Napier
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
    private val dbQueryNotification = database.userNotificationTableQueries

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
        model: FoodModel,
        notificationId: String,
        expirationDate: LocalDate,
    ): Result<String> = runCatching {
        val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .dbFormat()
        withContext(Dispatchers.IO) {
            val id = model.id + "_" + expirationDate
            dbQueryFoodUser.insertUserFood(
                
                id = id,
                productId = model.id,
                name = model.name.localizedString(),
                thumbnail = model.thumbnail.orEmpty(),
                expirationDate = expirationDate.dbFormat(),
                notificationId = notificationId,
                addAt = date,
            )
            id
        }
    }

    suspend fun getAllUserFood(): List<UserFoodModel> = withContext(Dispatchers.IO) {
        dbQueryFoodUser.selectUserFoods(::toUserFoodModel).executeAsList()
    }

    suspend fun deleteUserFood(id: String) = withContext(Dispatchers.IO) {
        dbQueryFoodUser.deleteUserFood(id)
    }

    private fun toUserFoodModel(
        id: String,
        productId: String,
        name: String,
        thumbnail: String,
        addAt: String,
        expirationDate: String,
        notificationId: String?,
    ): UserFoodModel = UserFoodModel(
        id = id,
        productId = productId,
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

    suspend fun addNotificationId(id: String, notificationId: String) =
        withContext(Dispatchers.IO) {
            dbQueryNotification.insertUserNotification(
                id = id,
                notificationUuid = notificationId,
            )
        }

    suspend fun removeNotificationId(id: String) = withContext(Dispatchers.IO) {
        dbQueryNotification.removeUserNotificationById(id)
    }

    suspend fun hasNotification(notificationUuid: String): Boolean = withContext(Dispatchers.IO) {
        dbQueryNotification.hasNotification(notificationUuid).executeAsOneOrNull() != null
    }

    suspend fun hasNotificationById(id: String): Boolean = withContext(Dispatchers.IO) {
        dbQueryNotification.hasNotificationById(id).executeAsOneOrNull() != null
    }

    suspend fun allNotificationModel(): List<UserNotificationModel> = withContext(Dispatchers.IO) {
        dbQueryNotification.selectUserNotifications { id, notificationUuid ->
            UserNotificationModel(
                id = id,
                uuid = notificationUuid,
            )
        }.executeAsList()
    }
}