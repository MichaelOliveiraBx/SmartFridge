package com.moliveira.app.smartfridge.modules.food.api

import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.sdk.LocalizedString
import com.moliveira.app.smartfridge.modules.sdk.handle
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

//openfoodfacts
private const val OPEN_FOOD_FACTS_BASE_URL = "https://world.openfoodfacts.org/api/v2/product/"

@Serializable
data class FoodProductDto(
    val name_en: String?,
    val name_fr: String?,
    val image_front_small_url: String?,
)

@Serializable
data class FoodModelDto(
    val code: String,
    val product: FoodProductDto,
)

class FoodApiClient(
    private val httpClient: HttpClient,
) {
    suspend fun getFoodById(id: String): Result<FoodModel> =
        httpClient.get("$OPEN_FOOD_FACTS_BASE_URL/$id")
            .handle<FoodModelDto>()
            .map {
                FoodModel(
                    id = it.code,
                    name = LocalizedString(
                        en = it.product.name_en,
                        fr = it.product.name_fr,
                    ),
                    thumbnail = it.product.image_front_small_url,
                )
            }
}