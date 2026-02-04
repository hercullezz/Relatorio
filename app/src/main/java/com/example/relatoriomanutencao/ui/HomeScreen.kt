package com.example.relatoriomanutencao.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.relatoriomanutencao.utils.ShiftManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel(),
    onReportClick: (String) -> Unit, // Mantido para compatibilidade de assinatura, mas pode redirecionar para detalhes
    onLogout: () -> Unit,
    onNavigateToMachineConfig: () -> Unit
) {
    val maintenanceItems by viewModel.maintenanceItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var filterByCurrentShift by remember { mutableStateOf(true) }
    val currentShiftInfo = remember { ShiftManager.getCurrentShiftInfo() }

    fun sameDay(aMillis: Long, bMillis: Long): Boolean {
        val ca = Calendar.getInstance().apply { timeInMillis = aMillis }
        val cb = Calendar.getInstance().apply { timeInMillis = bMillis }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }
    
    // Estados para o diálogo de confirmação de exclusão
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<com.example.relatoriomanutencao.data.MaintenanceItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Serviços") },
                actions = {
                    IconButton(onClick = onNavigateToMachineConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar Máquinas")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // Redireciona para a rota "new" que é a tela de Novo Serviço
                onReportClick("new_service") 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Serviço")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            Column(modifier = Modifier.fillMaxWidth()) {
                // Filtro por turno atual
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Turno atual: ${currentShiftInfo.shiftName}", modifier = Modifier.weight(1f))
                    Switch(checked = filterByCurrentShift, onCheckedChange = { filterByCurrentShift = it })
                }

                val displayed = if (filterByCurrentShift) {
                    maintenanceItems.filter { it.shiftId == currentShiftInfo.shiftId && sameDay(it.date, currentShiftInfo.workDate.time) }
                } else maintenanceItems

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(displayed) { maintenance ->
                    val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(maintenance.date))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .combinedClickable(
                                onClick = { 
                                    // Detalhes ou Edição futura
                                },
                                onLongClick = { 
                                    itemToDelete = maintenance
                                    showDeleteDialog = true
                                }
                            ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = maintenance.machine, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = dateString, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = maintenance.serviceType, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = maintenance.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                        }
                    }
                }
                
                if (displayed.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum serviço registrado.\nClique no + para adicionar.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de confirmação de exclusão
        if (showDeleteDialog && itemToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Deseja excluir o serviço na máquina ${itemToDelete?.machine}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            itemToDelete?.let { viewModel.deleteMaintenanceItem(it) }
                            showDeleteDialog = false
                            itemToDelete = null
                        }
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showDeleteDialog = false
                        itemToDelete = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

}
