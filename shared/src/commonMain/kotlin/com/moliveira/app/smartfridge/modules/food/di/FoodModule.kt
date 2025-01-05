package com.moliveira.app.smartfridge.modules.food.di

import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.api.FoodApiClient
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val foodModule = module {
    factoryOf(::FoodApiClient)
    singleOf(::FoodDatabase)
    singleOf(::FoodRepository)
}