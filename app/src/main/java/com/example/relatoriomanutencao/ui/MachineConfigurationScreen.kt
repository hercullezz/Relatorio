package com.example.relatoriomanutencao.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MachineConfigurationScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val productionLines by viewModel.allProductionLines.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()
    val machinesWithoutLine by viewModel.machinesWithoutLine.collectAsState()
    val stockLocations by viewModel.stockLocations.collectAsState() // Novos locais
    val isLoading by viewModel.isLoading.collectAsState()

    // Estados para adicionar/editar
    var showAddLineDialog by remember { mutableStateOf(false) }
    var showAddMachineDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) } // Novo diálogo
    var selectedLineForMachine by remember { mutableStateOf<ProductionLine?>(null) }
    
    // Estado para Limpeza de Nuvem
    var showCleanCloudDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
             CenterAlignedTopAppBar(
                 title = { 
                     Text(
                         "Configuração", 
                         style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                     ) 
                 },
                 actions = {
                     IconButton(onClick = { viewModel.syncMachineConfiguration() }) {
                         if (isLoading) {
                             CircularProgressIndicator(
                                 modifier = Modifier.size(24.dp),
                                 color = MaterialTheme.colorScheme.primary,
                                 strokeWidth = 2.dp
                             )
                         } else {
                             Icon(
                                 imageVector = Icons.Filled.Refresh,
                                 contentDescription = "Sincronizar",
                                 tint = MaterialTheme.colorScheme.primary
                             )
                         }
                     }
                 },
                 colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                     containerColor = MaterialTheme.colorScheme.background,
                     titleContentColor = MaterialTheme.colorScheme.onBackground
                 )
             )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFloatingActionButton(
                    onClick = { showAddLocationDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Novo Local")
                }

                ExtendedFloatingActionButton(
                    onClick = { showAddLineDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nova Linha") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Seção de Nuvem ---
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Filled.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(
                                "Limpeza de Nuvem",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Libere espaço removendo as fotos dos relatórios antigos (>30 dias). O texto do histórico será mantido.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCleanCloudDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Limpar Fotos Antigas")
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            }

            // --- Seção Locais de Estoque ---
            item {
                SectionHeader("Locais de Estoque")
                Text(
                    text = "Defina os armazéns, prateleiras ou caixas para organizar seu estoque.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (stockLocations.isNotEmpty()) {
                items(stockLocations) { location ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(location, style = MaterialTheme.typography.bodyLarge)
                            }
                            IconButton(onClick = { viewModel.deleteStockLocation(location) }) {
                                Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            } else {
                item {
                    EmptyStateMessage("Nenhum local cadastrado. Use o botão (+) menor.")
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Gerenciamento de Máquinas",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Máquinas sem Linha (Avulsas)
            item {
                SectionHeader("Máquinas Avulsas")
            }
            
            if (machinesWithoutLine.isNotEmpty()) {
                items(
                    items = machinesWithoutLine,
                    key = { it.id }
                ) { machine ->
                    MachineItem(
                        machine = machine,
                        onDelete = { viewModel.deleteMachine(machine) }
                    )
                }
            } else {
                 item {
                     EmptyStateMessage("Nenhuma máquina avulsa.")
                 }
            }

            item {
                 OutlinedButton(
                    onClick = { 
                        selectedLineForMachine = null // Avulsa
                        showAddMachineDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar Máquina Avulsa")
                }
            }

            item {
                 HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                 SectionHeader("Linhas de Produção")
            }

            if (productionLines.isEmpty()) {
                item {
                    EmptyStateMessage("Nenhuma linha de produção cadastrada.")
                }
            }

            // Linhas de Produção
            items(
                items = productionLines,
                key = { it.id }
            ) { line ->
                ProductionLineItem(
                    line = line,
                    machines = allMachines.filter { it.lineId == line.id },
                    onDeleteLine = { viewModel.deleteProductionLine(line) },
                    onAddMachineToLine = {
                        selectedLineForMachine = line
                        showAddMachineDialog = true
                    },
                    onDeleteMachine = { viewModel.deleteMachine(it) }
                )
            }
            
            // Botão de Logout Adicionado
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sair (Logout)", color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Spacer(modifier = Modifier.height(24.dp)) // Espaço extra no final
            }
        }
    }

    if (showAddLineDialog) {
        AddNameDialog(
            title = "Nova Linha de Produção",
            onDismiss = { showAddLineDialog = false },
            onConfirm = { name ->
                viewModel.addProductionLine(name)
                showAddLineDialog = false
            }
        )
    }

    if (showAddLocationDialog) {
        AddNameDialog(
            title = "Novo Local de Estoque",
            onDismiss = { showAddLocationDialog = false },
            onConfirm = { name ->
                viewModel.addStockLocation(name)
                showAddLocationDialog = false
            }
        )
    }

    if (showAddMachineDialog) {
        AddNameDialog(
            title = if (selectedLineForMachine != null) "Nova Máquina em ${selectedLineForMachine?.name}" else "Nova Máquina Avulsa",
            onDismiss = { showAddMachineDialog = false },
            onConfirm = { name ->
                viewModel.addMachine(name, selectedLineForMachine?.id)
                showAddMachineDialog = false
            }
        )
    }
    
    // Dialogo de confirmação de limpeza
    if (showCleanCloudDialog) {
        AlertDialog(
            onDismissRequest = { showCleanCloudDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Confirmar Limpeza") },
            text = { Text("Deseja remover as FOTOS dos relatórios anteriores a 30 dias?\n\nO texto dos relatórios PERMANECERÁ no histórico, mas as imagens não serão mais exibidas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cleanOldImagesOnly()
                        showCleanCloudDialog = false
                    }
                ) {
                    Text("Sim, Remover Fotos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanCloudDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun EmptyStateMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontStyle = FontStyle.Italic,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductionLineItem(
    line: ProductionLine,
    machines: List<Machine>,
    onDeleteLine: () -> Unit,
    onAddMachineToLine: () -> Unit,
    onDeleteMachine: (Machine) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { expanded = !expanded }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ViewList, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = line.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!expanded) {
                            Text(
                                text = "${machines.size} máquinas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = "Expandir",
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                    IconButton(onClick = onDeleteLine) {
                        Icon(
                            Icons.Outlined.Delete, 
                            contentDescription = "Deletar Linha", 
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant, 
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    if (machines.isNotEmpty()) {
                        machines.forEach { machine ->
                            MachineItem(machine = machine, onDelete = { onDeleteMachine(machine) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                         Text(
                            text = "Nenhuma máquina cadastrada.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 44.dp, bottom = 8.dp)
                        )
                    }

                    FilledTonalButton(
                        onClick = onAddMachineToLine,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                         shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar Máquina na Linha")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MachineItem(
    machine: Machine,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Build, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = machine.name, 
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = onDelete, 
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete, 
                    contentDescription = "Deletar Máquina", 
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddNameDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name) }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}
