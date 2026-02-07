package com.example.relatoriomanutencao.utils

import java.time.*
import java.util.*

object ShiftManager {

    data class Shift(val id: Int, val displayName: String, val start: LocalTime, val end: LocalTime, val crossesMidnight: Boolean)

    data class ShiftInfo(val shiftId: Int, val shiftName: String, val workDate: Date)

    private val shifts = listOf(
        Shift(1, "1º Turno (05:00 - 13:40)", LocalTime.of(5,0), LocalTime.of(13,40), false),
        Shift(2, "2º Turno (13:20 - 22:00)", LocalTime.of(13,20), LocalTime.of(22,0), false),
        Shift(3, "3º Turno (21:30 - 05:20)", LocalTime.of(21,30), LocalTime.of(5,20), true)
    )

    fun getCurrentShiftInfo(zone: ZoneId = ZoneId.systemDefault()): ShiftInfo {
        return getShiftInfo(Instant.now(), zone)
    }

    fun getShiftInfo(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): ShiftInfo {
        val zdt = instant.atZone(zone)
        val localTime = zdt.toLocalTime()

        // Find matching shifts
        val matches = shifts.filter { shift ->
            if (!shift.crossesMidnight) {
                (localTime >= shift.start) && (localTime < shift.end)
            } else {
                (localTime >= shift.start) || (localTime < shift.end)
            }
        }

        // Resolve overlap by choosing the shift with latest start time
        val selected = if (matches.isEmpty()) {
            // Fallback: choose 1st shift
            shifts[0]
        } else {
            matches.maxByOrNull { it.start }!!
        }

        // Compute workDate = date of shift start (midnight of that date)
        val startDate = if (selected.crossesMidnight && localTime.isBefore(selected.end)) {
            zdt.toLocalDate().minusDays(1)
        } else {
            zdt.toLocalDate()
        }

        val workDateStart = ZonedDateTime.of(startDate, LocalTime.MIDNIGHT, zone)

        return ShiftInfo(selected.id, selected.displayName, Date.from(workDateStart.toInstant()))
    }

    // Helper to compute shift info from epoch millis
    fun getShiftInfo(millis: Long, zone: ZoneId = ZoneId.systemDefault()): ShiftInfo {
        return getShiftInfo(Instant.ofEpochMilli(millis), zone)
    }

    // Get shift info for a specific shift id at a given instant
    fun getShiftInfoForShiftId(shiftId: Int, instant: Instant = Instant.now(), zone: ZoneId = ZoneId.systemDefault()): ShiftInfo {
        val shift = shifts.find { it.id == shiftId } ?: shifts[0]
        val zdt = instant.atZone(zone)
        val localTime = zdt.toLocalTime()

        val startDate = if (shift.crossesMidnight && localTime.isBefore(shift.end)) {
            zdt.toLocalDate().minusDays(1)
        } else {
            zdt.toLocalDate()
        }

        val workDateStart = ZonedDateTime.of(startDate, LocalTime.MIDNIGHT, zone)
        return ShiftInfo(shift.id, shift.displayName, Date.from(workDateStart.toInstant()))
    }
}
