package com.moliveira.app.smartfridge.modules.sdk

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(DelicateCoroutinesApi::class)
class SharedPrefs(
    private val dataStoreBuilder: DataStoreBuilder
) {
    companion object {
        private val ONBOARDING_DISPLAYED_KEY = booleanPreferencesKey("onboarding_displayed")
    }

    private val dataStore = dataStoreBuilder.build("shared_prefs.preferences_pb")

    suspend fun setOnboardingDisplayed() {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_DISPLAYED_KEY] = true
        }
    }

    val isOnboardingDisplayedFlow = dataStore.data.map { prefs ->
        prefs[ONBOARDING_DISPLAYED_KEY] ?: false
    }.stateIn(
        scope = GlobalScope,
        started = SharingStarted.Eagerly,
        initialValue = false,
    )
}

val LocalSharedPrefs = staticCompositionLocalOf<SharedPrefs> {
    error("No SharedPrefs provided")
}