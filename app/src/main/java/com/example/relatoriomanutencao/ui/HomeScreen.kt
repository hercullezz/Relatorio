package com.example.relatoriomanutencao.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings // Importação adicionada
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.viewmodel.ReportViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    // authViewModel: AuthViewModel = viewModel(), // Removido temporariamente
    reportViewModel: ReportViewModel = viewModel(),
    onReportClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToMachineConfig: () -> Unit // Novo parâmetro adicionado
) {
    val reports by reportViewModel.reports.collectAsState()
    val isLoading by reportViewModel.isLoading.collectAsState()
    val error by reportViewModel.error.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Estados para o diálogo de confirmação de exclusão
    var showDeleteDialog by remember { mutableStateOf(false) }
    var reportToDeleteId by remember { mutableStateOf<String?>(null) }
    var reportToDeleteTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatórios") },
                actions = {
                    // Botão para navegar para a tela de configuração de máquinas
                    IconButton(onClick = onNavigateToMachineConfig) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurar Máquinas")
                    }
                    /* Logout desabilitado por enquanto
                    IconButton(onClick = {
                        // authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                    */
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                coroutineScope.launch {
                    val dailyReport = reportViewModel.createOrOpenDailyReport()
                    dailyReport?.let { report ->
                        onReportClick(report.id)
                    }
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Novo Relatório")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Aviso: ${error}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(reports) { report ->
                    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                    val displayDateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                    
                    val formattedTitle = try {
                        val date = dateFormatter.parse(report.title)
                        date?.let { displayDateFormatter.format(it) } ?: report.title
                    } catch (e: Exception) {
                        report.title // Fallback if parsing fails
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .combinedClickable(
                                onClick = { onReportClick(report.id) },
                                onLongClick = { 
                                    reportToDeleteId = report.id
                                    reportToDeleteTitle = formattedTitle // Usar o título formatado para a confirmação
                                    showDeleteDialog = true
                                }
                            ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = formattedTitle, style = MaterialTheme.typography.titleMedium)
                            Text(text = "Criado por: ${report.createdBy}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                if (reports.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum relatório encontrado.\nClique no + para criar um novo.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de confirmação de exclusão
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar Exclusão") },
                text = { Text("Você tem certeza que deseja excluir o relatório do dia ${reportToDeleteTitle}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            reportToDeleteId?.let { id ->
                                reportViewModel.deleteReport(id)
                            }
                            showDeleteDialog = false
                            reportToDeleteId = null
                            reportToDeleteTitle = null
                        }
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showDeleteDialog = false
                        reportToDeleteId = null
                        reportToDeleteTitle = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
