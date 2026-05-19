package com.example.relatoriomanutencao.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.relatoriomanutencao.utils.ShiftManager
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.example.relatoriomanutencao.ui.theme.IndustrialSecondaryDark
import com.example.relatoriomanutencao.ui.theme.IndustrialTertiaryDark
import com.example.relatoriomanutencao.ui.theme.ErrorRed
import com.parse.ParseUser
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CompactHeader(viewModel: MainViewModel, onLogout: () -> Unit) {

    // ── Dados do usuário logado ──────────────────────────────────────────────
    val user       = ParseUser.getCurrentUser()
    val fullName   = user?.getString("name") ?: user?.username ?: ""
    val firstName  = fullName.trim().split(" ").firstOrNull() ?: fullName
    
    // Tenta ler 'shiftId' (minúsculo). Se for 0, tenta 'ShiftId' (maiúsculo) para compatibilidade.
    val userShiftId = user?.getInt("shiftId").let { id ->
        if (id == null || id == 0) user?.getInt("ShiftId") ?: 0 else id
    } 

    // ── Estado do turno (reativo, atualiza a cada minuto) ───────────────────
    var shiftInfo      by remember { mutableStateOf(ShiftManager.getCurrentShiftInfo()) }
    var currentShiftId  by remember { mutableStateOf(ShiftManager.getCurrentShiftInfo().shiftId) }
    var visibleShiftIds by remember { mutableStateOf(ShiftManager.getVisibleShiftInfos().map { it.shiftId }.toSet()) }

    LaunchedEffect(Unit) {
        while (true) {
            shiftInfo       = ShiftManager.getCurrentShiftInfo()
            currentShiftId  = ShiftManager.getCurrentShiftInfo().shiftId
            visibleShiftIds = ShiftManager.getVisibleShiftInfos().map { it.shiftId }.toSet()
            delay(60_000L)
        }
    }

    // ── Determinar a cor do turno baseado no estado ─────────────────────────
    // Verde  → ATIVO (dentro do turno cadastrado)
    // Vermelho → HORA EXTRA (+1h de tolerância)
    // Cinza   → FORA DO TURNO
    val shiftColor = when {
        userShiftId == 0                        -> IndustrialSecondaryDark
        currentShiftId == userShiftId           -> IndustrialTertiaryDark
        visibleShiftIds.contains(userShiftId)   -> ErrorRed
        else                                    -> IndustrialSecondaryDark
    }

    // ── Formatação de exibição ───────────────────────────────────────────────
    val dateFormat = SimpleDateFormat("dd/MM", Locale.forLanguageTag("pt-BR")).apply {
        timeZone = TimeZone.getTimeZone("America/Porto_Velho")
    }
    val interval = when (shiftInfo.shiftId) {
        1    -> "05:00-13:40"
        2    -> "13:20-22:00"
        else -> "21:30-05:20"
    }

    // ── Layout ───────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            // Turno com cor indicativa de status
            Text(
                text = "T${shiftInfo.shiftId}",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = shiftColor
            )

            // Nome, data e intervalo no centro
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = firstName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "${dateFormat.format(shiftInfo.workDate)} • $interval",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }

            // Logout à direita
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sair",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}
