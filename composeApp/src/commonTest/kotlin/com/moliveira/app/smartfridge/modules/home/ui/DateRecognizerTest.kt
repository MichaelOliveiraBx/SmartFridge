package com.moliveira.app.smartfridge.modules.home.ui

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DateRecognizerTest {

    @Test
    fun `recognizes date`() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        assertEquals(
            DateRecognizer.invoke("24/08/2023"),
            LocalDate(2023, 8, 24)
        )
        assertEquals(
            DateRecognizer.invoke("24.08.2023"),
            LocalDate(2023, 8, 24)
        )
        assertEquals(
            DateRecognizer.invoke("24/08"),
            LocalDate(now.year, 8, 24)
        )
        assertEquals(
            DateRecognizer.invoke("24.08"),
            LocalDate(now.year, 8, 24)
        )

        assertEquals(
            DateRecognizer.invoke("24.08.2"),
            null,
        )
    }
}