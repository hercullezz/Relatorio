package com.example.relatoriomanutencao.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit // Importação adicionada
import androidx.compose.material.icons.filled.Refresh // Importação adicionada
import androidx.compose.material3.* // Importa todos os componentes Material3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.viewmodel.ReportViewModel
import com.example.relatoriomanutencao.data.ReportItem // Importação adicionada para ReportItem
import com.example.relatoriomanutencao.data.Line // Importação adicionada para Line

private const val TAG = "ReportScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportId: String,
    reportViewModel: ReportViewModel = viewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(reportId) {
        reportViewModel.fetchReport(reportId)
    }

    val report by reportViewModel.currentReport.collectAsState()
    val lines by reportViewModel.lines.collectAsState() // Coleta as linhas do ViewModel

    // Log para debug
    LaunchedEffect(report) {
        Log.d(TAG, "Relatório atualizado: ${report?.title}, itens: ${report?.items?.size ?: 0}")
    }

    var newItemDescription by remember { mutableStateOf("") }
    var isMachineSpecific by remember { mutableStateOf(false) } // Estado para alternar entre geral/máquina

    // Estados para seleção de máquina/linha/serviço
    var expandedLine by remember { mutableStateOf(false) }
    var selectedLine by remember { mutableStateOf<Line?>(null) }
    
    var expandedMachine by remember { mutableStateOf(false) }
    var selectedMachine by remember { mutableStateOf<String?>(null) }

    var expandedServiceType by remember { mutableStateOf(false) }
    val serviceTypes = listOf("Informações gerais", "Corretiva", "Preventiva", "Preditiva")
    var selectedServiceType by remember { mutableStateOf<String?>("Corretiva") } // Padrão "Corretiva"

    // Estados para Edição e Exclusão
    var itemToEdit by remember { mutableStateOf<ReportItem?>(null) }
    var showDeleteItemDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ReportItem?>(null) }

    // Função para resetar todos os campos de entrada
    val resetInputFields: () -> Unit = { 
        newItemDescription = ""
        isMachineSpecific = false
        selectedLine = null
        selectedMachine = null
        selectedServiceType = "Corretiva"
        itemToEdit = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(report?.title ?: "Carregando...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = { // Adicionado o bloco de ações
                    IconButton(onClick = { reportViewModel.fetchReport(reportId) }) { // Botão de recarregar
                        Icon(Icons.Default.Refresh, contentDescription = "Recarregar Relatório")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Alternador para tipo de entrada
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Informação Específica da Máquina")
                    Switch(
                        checked = isMachineSpecific,
                        onCheckedChange = { 
                            isMachineSpecific = it
                            selectedLine = null
                            selectedMachine = null
                            selectedServiceType = if (it) "Corretiva" else null // Define padrão "Corretiva" se for máquina específica
                        },
                        enabled = itemToEdit == null // Desabilita o switch durante a edição
                    )
                }

                if (isMachineSpecific) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Seletor de Linha
                    ExposedDropdownMenuBox(
                        expanded = expandedLine,
                        onExpandedChange = { expandedLine = !expandedLine },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedLine?.name ?: "Selecionar Linha",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Linha") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLine) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLine,
                            onDismissRequest = { expandedLine = false }
                        ) {
                            lines.forEach { line ->
                                DropdownMenuItem(text = { Text(line.name) }, onClick = {
                                    selectedLine = line
                                    selectedMachine = null // Reseta máquina ao mudar a linha
                                    selectedServiceType = "Corretiva" // Mantém Corretiva como padrão
                                    expandedLine = false
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Seletor de Máquina (habilitado apenas se uma linha for selecionada)
                    ExposedDropdownMenuBox(
                        expanded = expandedMachine,
                        onExpandedChange = { 
                            if (selectedLine != null) expandedMachine = !expandedMachine 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMachine ?: "Selecionar Máquina",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Máquina") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMachine) },
                            enabled = selectedLine != null,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMachine,
                            onDismissRequest = { expandedMachine = false }
                        ) {
                            selectedLine?.machines?.forEach { machine ->
                                DropdownMenuItem(text = { Text(machine) }, onClick = {
                                    selectedMachine = machine
                                    expandedMachine = false
                                })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Seletor de Tipo de Serviço
                    ExposedDropdownMenuBox(
                        expanded = expandedServiceType,
                        onExpandedChange = { expandedServiceType = !expandedServiceType },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedServiceType ?: "Selecionar Tipo de Serviço",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Serviço") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedServiceType) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedServiceType,
                            onDismissRequest = { expandedServiceType = false }
                        ) {
                            serviceTypes.forEach { type ->
                                DropdownMenuItem(text = { Text(type) }, onClick = {
                                    selectedServiceType = type
                                    expandedServiceType = false
                                })
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newItemDescription,
                    onValueChange = { newItemDescription = it },
                    label = { Text("Descrição do Serviço") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newItemDescription.isNotBlank()) {
                            val newItem = ReportItem(
                                machine = selectedLine?.name ?: "",
                                subMachine = selectedMachine,
                                serviceType = selectedServiceType ?: "", 
                                description = newItemDescription
                            )

                            Log.d(TAG, "Adicionando item: $newItem")

                            if (itemToEdit != null) {
                                // Modo de Edição
                                reportViewModel.updateReportItem(reportId, itemToEdit!!, newItem)
                            } else {
                                // Modo de Adição
                                reportViewModel.addReportItem(reportId, newItem)
                                Log.d(TAG, "addReportItem chamado, resetando campos")
                            }
                            resetInputFields()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newItemDescription.isNotBlank() && 
                              (!isMachineSpecific || (selectedLine != null && selectedMachine != null))
                ) {
                    Text(if (itemToEdit != null) "Salvar Alterações" else "Adicionar ao Relatório")
                }
                // Botão de Cancelar Edição (se estiver em modo de edição)
                if (itemToEdit != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = resetInputFields, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar Edição")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(8.dp)) {
            items(report?.items ?: emptyList()) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                if (item.machine.isNotBlank() || !item.subMachine.isNullOrBlank()) { // Corrigido: usando !isNullOrBlank()
                                    Text(
                                        text = "Máquina: ${item.machine} ${item.subMachine?.let { "- $it" } ?: ""}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    if (item.serviceType.isNotBlank() && item.serviceType != "Informações gerais") { // Exibe o tipo de serviço apenas se não for vazio e não for "Informações gerais"
                                        Text(
                                            text = "Tipo: ${item.serviceType}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else if (item.serviceType == "Informações gerais") { // Se for "Informações gerais" da máquina
                                        Text(
                                            text = "Informações gerais da máquina",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                } else { // Informação geral do relatório
                                    Text("Informação Geral", style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                            // Botões de Ação (Editar e Excluir)
                            Row {
                                IconButton(onClick = {
                                    itemToEdit = item
                                    newItemDescription = item.description
                                    isMachineSpecific = item.machine.isNotBlank() || !item.subMachine.isNullOrBlank() || item.serviceType.isNotBlank() // CORRIGIDO AQUI NOVAMENTE
                                    selectedLine = lines.firstOrNull { line -> line.name == item.machine }
                                    selectedMachine = item.subMachine
                                    selectedServiceType = item.serviceType?.ifBlank { null } ?: "Corretiva"
                                    // TODO: Possivelmente adicionar scroll para o topo ou para o bottomBar
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Item")
                                }
                                IconButton(onClick = {
                                    itemToDelete = item
                                    showDeleteItemDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir Item")
                                }
                            }
                        }
                        Text(text = item.description, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Por: ${item.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Diálogo de confirmação de exclusão de item
        if (showDeleteItemDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteItemDialog = false },
                title = { Text("Confirmar Exclusão de Item") },
                text = { Text("Você tem certeza que deseja excluir este item do relatório?") },
                confirmButton = {
                    Button(
                        onClick = {
                            itemToDelete?.let { item ->
                                reportViewModel.deleteReportItem(reportId, item)
                            }
                            showDeleteItemDialog = false
                            itemToDelete = null
                        }
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showDeleteItemDialog = false
                        itemToDelete = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}