package com.example.relatoriomanutencao.utils

import android.util.Log
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

object ShiftManager {

    data class Shift(val id: Int, val displayName: String, val start: LocalTime, val end: LocalTime, val crossesMidnight: Boolean)
    data class ShiftInfo(val shiftId: Int, val shiftName: String, val workDate: Date)

    private val shifts = listOf(
        Shift(1, "1º Turno (05:00 - 13:40)", LocalTime.of(5,0), LocalTime.of(13,40), false),
        Shift(2, "2º Turno (13:20 - 22:00)", LocalTime.of(13,20), LocalTime.of(22,0), false),
        Shift(3, "3º Turno (21:30 - 05:20)", LocalTime.of(21,30), LocalTime.of(5,20), true)
    )

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatWorkDate(millis: Long): String {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.of("America/Porto_Velho")).toLocalDate().format(dateFormatter)
    }

    fun getCurrentShiftInfo(): ShiftInfo = getShiftInfo(Instant.now())

    /**
     * Retorna o turno ANTERIOR ao turno atual pela sequência 1→3, 2→1, 3→2.
     *
     * Regras de workDate:
     *  - Atual=1º → Anterior=3º do DIA ANTERIOR (workDate -1 dia)
     *  - Atual=2º → Anterior=1º do MESMO dia
     *  - Atual=3º → Anterior=2º do MESMO dia
     */
    fun getPreviousShiftInfo(): ShiftInfo {
        val current = getCurrentShiftInfo()
        val zone = ZoneId.of("America/Porto_Velho")

        val prevShiftId = when (current.shiftId) {
            1 -> 3
            2 -> 1
            3 -> 2
            else -> 1
        }

        // Calcula a workDate do turno anterior
        val currentWorkLocalDate = current.workDate.toInstant().atZone(zone).toLocalDate()
        val prevWorkLocalDate = if (current.shiftId == 1) {
            // 3º turno anterior pertence ao dia de trabalho anterior
            currentWorkLocalDate.minusDays(1)
        } else {
            currentWorkLocalDate
        }

        val prevWorkDate = Date.from(
            ZonedDateTime.of(prevWorkLocalDate, LocalTime.MIDNIGHT, zone).toInstant()
        )

        val prevShift = shifts.find { it.id == prevShiftId } ?: shifts[0]
        return ShiftInfo(prevShift.id, prevShift.displayName, prevWorkDate)
    }

    fun getVisibleShiftInfos(instant: Instant = Instant.now()): List<ShiftInfo> {
        val current = getShiftInfo(instant)
        val oneHourAgo = instant.minus(1, ChronoUnit.HOURS)
        val previous = getShiftInfo(oneHourAgo)

        return if (current.shiftId == previous.shiftId && current.workDate.time == previous.workDate.time) {
            listOf(current)
        } else {
            listOf(previous, current)
        }
    }

    fun getShiftInfo(instant: Instant, zone: ZoneId = ZoneId.of("America/Porto_Velho")): ShiftInfo {
        val zdt = instant.atZone(zone)
        val localTime = zdt.toLocalTime()

        val matchingShifts = shifts.filter { shift ->
            if (!shift.crossesMidnight) {
                (localTime >= shift.start) && (localTime < shift.end)
            } else {
                (localTime >= shift.start) || (localTime < shift.end)
            }
        }

        val selected = when {
            matchingShifts.isEmpty() -> shifts[0]
            matchingShifts.size == 1 -> matchingShifts[0]
            else -> {
                // Prioriza o turno anterior durante o cruzamento de horários (overlap),
                // escolhendo o turno que começou PRIMEIRO.
                matchingShifts.minByOrNull { 
                    if (it.crossesMidnight && localTime < it.end) it.start.toSecondOfDay() - 86400 
                    else it.start.toSecondOfDay()
                } ?: matchingShifts[0]
            }
        }

        val startDate = if (selected.crossesMidnight && localTime.isBefore(selected.end)) {
            zdt.toLocalDate().minusDays(1)
        } else {
            zdt.toLocalDate()
        }

        val workDateStart = ZonedDateTime.of(startDate, LocalTime.MIDNIGHT, zone)
        return ShiftInfo(selected.id, selected.displayName, Date.from(workDateStart.toInstant()))
    }

    /**
     * Calcula workDate para um turno específico e um momento no tempo.
     */
    fun getShiftInfoForShiftId(shiftId: Int, instant: Instant): ShiftInfo {
        val shift = shifts.find { it.id == shiftId } ?: shifts[0]
        val zdt = instant.atZone(ZoneId.of("America/Porto_Velho"))
        val localTime = zdt.toLocalTime()
        val startDate = if (shift.crossesMidnight && localTime.isBefore(shift.end)) zdt.toLocalDate().minusDays(1) else zdt.toLocalDate()
        val workDateStart = ZonedDateTime.of(startDate, LocalTime.MIDNIGHT, ZoneId.of("America/Porto_Velho"))
        return ShiftInfo(shift.id, shift.displayName, Date.from(workDateStart.toInstant()))
    }

    fun getShiftInfoForShiftIdAndWorkDate(shiftId: Int, workDateMillis: Long): ShiftInfo {
        val shift = shifts.find { it.id == shiftId } ?: shifts[0]
        return ShiftInfo(shift.id, shift.displayName, Date(workDateMillis))
    }
}
