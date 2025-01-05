package com.moliveira.app.smartfridge.modules.home.ui

import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

object DateRecognizer {

    private val dateRegex = Regex("""\d{1,2}[./]\d{1,2}[./]\d{2,4}""")
    private val dateRegexShort = Regex("""\d{1,2}[./]\d{1,2}""")
    fun invoke(text: String): LocalDate? {
        return dateRegex.find(text)?.value?.let { matchText ->
            val textMatch = matchText.replace(".", "/")
            val yearSize = textMatch.split("/").last().length
            Napier.d("TTTT : $matchText - $textMatch - yearSize:$yearSize ")
            runCatching {
                LocalDate.parse(matchText.replace(".", "/"), formatLong(yearSize == 2))
            }.getOrNull()
        } ?: dateRegexShort.find(text)?.value?.let { matchText ->
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            Napier.d("TTTT SHORT: $matchText - date:$date ")
            runCatching {
                LocalDate.parse(
                    matchText
                        .replace(".", "/")
                        .plus("/" + date.year),
                    formatShort()
                )
            }.getOrNull()
        }
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