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

    // ============================================================================
    // TESTES DE TURNO 1 (05:00 - 13:40)
    // ============================================================================

    @Test
    fun testShift1_atStart_05_00() {
        val millis = toMillis(2026, 2, 4, 5, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 1 às 05:00", 1, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate deve ser 04/02", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift1_midShift_09_00() {
        val millis = toMillis(2026, 2, 4, 9, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 1 às 09:00", 1, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate deve ser 04/02", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift1_justBeforeEnd_13_39() {
        val millis = toMillis(2026, 2, 4, 13, 39)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 1 às 13:39", 1, info.shiftId)
    }

    // ============================================================================
    // TESTES DE TRANSIÇÃO 1 → 2 (Exatamente 13:40 - SEM OVERLAP)
    // ============================================================================

    @Test
    fun testBoundary_13_40_TransitionToShift2() {
        // Exatamente 13:40 = início do Turno 2
        val millis = toMillis(2026, 2, 4, 13, 40)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Turno muda para 2 às 13:40 (sem ambiguidade)", 2, info.shiftId)
    }

    // ============================================================================
    // TESTES DE TURNO 2 (13:40 - 22:00)
    // ============================================================================

    @Test
    fun testShift2_justAfterStart_13_40() {
        val millis = toMillis(2026, 2, 4, 13, 40)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 2 às 13:40", 2, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate deve ser 04/02", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift2_midShift_18_00() {
        val millis = toMillis(2026, 2, 4, 18, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 2 às 18:00", 2, info.shiftId)
    }

    @Test
    fun testShift2_justBeforeEnd_21_59() {
        val millis = toMillis(2026, 2, 4, 21, 59)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 2 às 21:59", 2, info.shiftId)
    }

    // ============================================================================
    // TESTES DE TRANSIÇÃO 2 → 3 (Exatamente 22:00 - SEM OVERLAP)
    // ============================================================================

    @Test
    fun testBoundary_22_00_TransitionToShift3() {
        // Exatamente 22:00 = início do Turno 3
        val millis = toMillis(2026, 2, 4, 22, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Turno muda para 3 às 22:00 (sem ambiguidade)", 3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate ainda é 04/02 (início do turno 3)", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    // ============================================================================
    // TESTES DE TURNO 3 (22:00 - 05:00 do DIA SEGUINTE)
    // ============================================================================

    @Test
    fun testShift3_justAfterStart_22_00() {
        val millis = toMillis(2026, 2, 4, 22, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 3 às 22:00", 3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate deve ser 04/02 (início do turno)", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift3_midNight_00_30() {
        // Depois de meia-noite, mas ainda Turno 3
        val millis = toMillis(2026, 2, 5, 0, 30)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 3 às 00:30", 3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        // IMPORTANTE: O turno 3 COMEÇOU em 04/02 às 22:00
        // Então a workDate fica 04/02, NÃO muda para 05/02
        assertEquals("WorkDate fica 04/02 (dia que turno começou)", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testShift3_beforeEnd_04_59() {
        // Um minuto antes do Turno 3 terminar
        val millis = toMillis(2026, 2, 5, 4, 59)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Deve ser Turno 3 às 04:59", 3, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate continua sendo 04/02", 4, cal.get(Calendar.DAY_OF_MONTH))
    }

    // ============================================================================
    // TESTES DE TRANSIÇÃO 3 → 1 (Exatamente 05:00 - SEM OVERLAP)
    // ============================================================================

    @Test
    fun testBoundary_05_00_TransitionToShift1() {
        // Exatamente 05:00 = início do Turno 1 (dia seguinte)
        val millis = toMillis(2026, 2, 5, 5, 0)
        val info = ShiftManager.getShiftInfo(millis, zone)
        assertEquals("Turno muda para 1 às 05:00 (dia seguinte)", 1, info.shiftId)
        val cal = Calendar.getInstance().apply { time = info.workDate }
        assertEquals("WorkDate muda para 05/02 (novo turno 1)", 5, cal.get(Calendar.DAY_OF_MONTH))
    }

    // ============================================================================
    // TESTES DE VALIDAÇÃO
    // ============================================================================

    @Test
    fun testValidateShiftData_correctData() {
        val millis = toMillis(2026, 2, 4, 9, 0)
        val shiftInfo = ShiftManager.getShiftInfo(millis, zone)
        
        // Dados corretos devem passar na validação
        val isValid = ShiftManager.validateShiftData(
            shiftInfo.shiftId,
            shiftInfo.workDate,
            java.time.Instant.ofEpochMilli(millis),
            zone
        )
        assertEquals("Dados consistentes devem validar", true, isValid)
    }

    @Test
    fun testValidateShiftData_wrongShiftId() {
        val millis = toMillis(2026, 2, 4, 9, 0)  // Turno 1
        val shiftInfo = ShiftManager.getShiftInfo(millis, zone)
        
        // Se passar shiftId errado, deve falhar
        val isValid = ShiftManager.validateShiftData(
            2,  // Passando turno 2, mas hora é de turno 1
            shiftInfo.workDate,
            java.time.Instant.ofEpochMilli(millis),
            zone
        )
        assertEquals("Dados inconsistentes devem falhar validação", false, isValid)
    }

    @Test
    fun testGetShiftInfoForShiftIdAndWorkDate_3rdShiftAfterMidnight() {
        // Situação típica do bug: serviço às 01:00 do dia 23/03 é registrado com workDate 22/03 e shiftId=3
        val workDateMillis = toMillis(2026, 2, 22, 0, 0)
        val info = ShiftManager.getShiftInfoForShiftIdAndWorkDate(3, workDateMillis)
        val cal = Calendar.getInstance().apply { time = info.workDate }

        assertEquals("workDate deve manter 22/03 para turno 3", 22, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals("shiftId deve ser 3", 3, info.shiftId)
    }

    @Test
    fun testValidateShiftData_nullData() {
        val millis = toMillis(2026, 2, 4, 9, 0)
        
        // Dados nulos devem falhar
        val isValid = ShiftManager.validateShiftData(
            null,
            null,
            java.time.Instant.ofEpochMilli(millis),
            zone
        )
        assertEquals("Dados nulos devem falhar", false, isValid)
    }
}
