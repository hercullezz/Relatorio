package com.example.relatoriomanutencao.utils

import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import org.junit.Assert.assertEquals

class ShiftManagerTest {

    private val zone = ZoneId.systemDefault()

    private fun toMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val ldt = LocalDateTime.of(year, month, day, hour, minute)
        return ldt.atZone(zone).toInstant().toEpochMilli()
    }

    @Test
    fun testShift1_atStart() {
        val millis = toMillis(2026, 2, 4, 5, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals(1, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift2_afterBoundary() {
        val millis = toMillis(2026, 2, 4, 13, 40)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals(2, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift3_overlapPrefers3rd() {
        val millis = toMillis(2026, 2, 4, 21, 45)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals(3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift3_afterMidnight() {
        val millis = toMillis(2026, 2, 5, 1, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals(3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals(4, cal.get(Calendar.DAY_OF_MONTH)) // workDate is start date (previous day)
    }

    @Test
    fun testShift3_beforeStartOfDay() {
        val millis = toMillis(2026, 2, 4, 4, 59)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals(3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        // started previous day (3rd turno de 03/02 21:30 -> workDate should be 03/02)
        // For this test date (2026-02-04 04:59) workDate should be 2026-02-03
        assertEquals(3, cal.get(Calendar.DAY_OF_MONTH))
    }
}
