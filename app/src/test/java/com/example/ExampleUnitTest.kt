package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

class ExampleUnitTest {

    @Test
    fun testLoveCounterCalculation() {
        val startDate = LocalDate.of(2025, 1, 1)
        val testDate = LocalDate.of(2026, 1, 1)

        val daysBetween = ChronoUnit.DAYS.between(startDate, testDate) + 1
        assertEquals(366L, daysBetween)

        val period = Period.between(startDate, testDate)
        assertEquals(1, period.years)
    }

    @Test
    fun testMilestoneComputation() {
        val startDate = LocalDate.of(2025, 1, 1)
        val day100 = startDate.plusDays(99)
        assertEquals(LocalDate.of(2025, 4, 10), day100)

        val day365 = startDate.plusDays(364)
        assertEquals(LocalDate.of(2025, 12, 31), day365)
    }

    @Test
    fun testFogOfWarRadius() {
        val radiusList = listOf(100, 300, 500, 1000)
        assertTrue(radiusList.contains(300))
    }
}
