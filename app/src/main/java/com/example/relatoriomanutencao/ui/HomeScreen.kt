package com.example.relatoriomanutencao.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.example.relatoriomanutencao.utils.ShiftManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel(),
    onReportClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToMachineConfig: () -> Unit
) {
    val maintenanceItems by viewModel.maintenanceItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var filterByCurrentShift by remember { mutableStateOf(true) }
    val nowShift = ShiftManager.getCurrentShiftInfo()
    val visibleShifts = ShiftManager.getVisibleShiftInfos()
    
    var searchQuery by remember { mutableStateOf("") }

    fun isSameDay(millisA: Long, millisB: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = millisA }
        val calB = Calendar.getInstance().apply { timeInMillis = millisB }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
               calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<com.example.relatoriomanutencao.data.MaintenanceItem?>(null) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.background)
            )
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Relatório de Manutenção", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToMachineConfig) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuração", tint = Color.White)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onReportClick("new_service") },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Novo", tint = Color.White)
                }
            }
        ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar máquina ou descrição") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val shiftLabel = if (visibleShifts.size > 1) "Transição de Turno" else nowShift.shiftName
                Text(
                    text = "Filtrar por Turno ($shiftLabel)",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Switch(checked = filterByCurrentShift, onCheckedChange = { filterByCurrentShift = it })
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredList = remember(maintenanceItems, filterByCurrentShift, searchQuery, visibleShifts) {
                    var list = maintenanceItems
                    if (filterByCurrentShift) {
                        list = list.filter { item ->
                            visibleShifts.any { shift ->
                                val itemWorkDate = item.workDateMillisFromServer ?: run {
                                    ShiftManager.getShiftInfo(java.time.Instant.ofEpochMilli(item.date)).workDate.time
                                }
                                item.shiftId == shift.shiftId && isSameDay(itemWorkDate, shift.workDate.time)
                            }
                        }
                    }
                    if (searchQuery.isNotBlank()) {
                        val q = searchQuery.lowercase()
                        list = list.filter { it.machine.lowercase().contains(q) || it.description.lowercase().contains(q) }
                    }
                    list
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredList) { item ->
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }.format(Date(item.date))
                        val workDateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }.format(Date(item.workDateMillisFromServer ?: item.date))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).combinedClickable(
                                onClick = { /* Edição */ },
                                onLongClick = { itemToDelete = item; showDeleteDialog = true }
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = item.machine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(text = "T${item.shiftId} - $workDateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Text(text = item.serviceType, style = MaterialTheme.typography.labelSmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "Registrado às $timeStr", 
                                    style = MaterialTheme.typography.labelSmall, // CORRIGIDO: de labelExtraSmall para labelSmall
                                    modifier = Modifier.align(Alignment.End),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                    
                    if (filteredList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Nenhum serviço encontrado.", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Excluir Registro") },
                text = { Text("Deseja realmente excluir este serviço?") },
                confirmButton = {
                    Button(onClick = { 
                        itemToDelete?.let { viewModel.deleteMaintenanceItem(it) }
                        showDeleteDialog = false 
                    }) { Text("Excluir") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            )
        }
    }
    }
}
