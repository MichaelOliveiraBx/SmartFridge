package com.moliveira.app.smartfridge.modules.home.ui

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

object DateRecognizer {

    private val dateRegex = Regex("""\d{1,2}[./]\d{1,2}[./]\d{2,4}""")
    private val dateRegexShort = Regex("""\d{1,2}[./]\d{1,2}""")
    fun invoke(text: String): LocalDate? = when {
        dateRegex.matches(text) -> {
            val textMatch = text.replace(".", "/")
            val yearSize = textMatch.split("/").last().length
            LocalDate.parse(text.replace(".", "/"), formatLong(yearSize == 2))
        }

        dateRegexShort.matches(text) -> {
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            LocalDate.parse(
                text
                    .replace(".", "/")
                    .plus("/" + date.year),
                formatShort()
            )
        }

        else -> null
    }

    private fun formatShort() = LocalDate.Format {
        dayOfMonth()
        char('/')
        monthNumber()
        char('/')
        year()
    }

    private fun formatLong(
        yearTwoDigits: Boolean
    ) = LocalDate.Format {
        dayOfMonth()
        char('/')
        monthNumber()
        char('/')
        if (yearTwoDigits) yearTwoDigits(2000) else year()
    }
}