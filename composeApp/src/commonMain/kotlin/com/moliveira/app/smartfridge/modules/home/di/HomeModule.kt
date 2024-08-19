package com.moliveira.app.smartfridge.modules.home.di

import com.moliveira.app.smartfridge.modules.home.ui.HomeViewModel
import com.moliveira.app.smartfridge.modules.home.ui.HomeViewStateConverter
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val homeModule = module {
    factoryOf(::HomeViewModel)
    factoryOf(::HomeViewStateConverter)
}