package com.moliveira.app.smartfridge.modules.sdk

import androidx.compose.ui.text.intl.Locale

data class LocalizedString(
    val en: String? = null,
    val fr: String? = null,
)

fun LocalizedString.localizedString(): String {
    return when (Locale.current.language) {
        "fr" -> fr ?: en ?: ""
        else -> en ?: fr ?: ""
    }
}