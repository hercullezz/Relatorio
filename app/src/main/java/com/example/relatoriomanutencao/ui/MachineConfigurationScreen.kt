package com.example.relatoriomanutencao.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.example.relatoriomanutencao.ui.admin.AdminUserContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MachineConfigurationScreen(
    viewModel: MainViewModel = viewModel()
) {
    val productionLines by viewModel.allProductionLines.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()
    val machinesWithoutLine by viewModel.machinesWithoutLine.collectAsState()
    val stockLocations by viewModel.stockLocations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados de Navegação Menu
    var currentScreen by remember { mutableStateOf("MENU") } // MENU, MACHINES, STOCK, USERS, CLEANUP

    var showAddLineDialog by remember { mutableStateOf(false) }
    var showAddMachineDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    
    // Estados para Edição
    var lineToEdit by remember { mutableStateOf<ProductionLine?>(null) }
    var machineToEdit by remember { mutableStateOf<Machine?>(null) }
    var locationToEdit by remember { mutableStateOf<String?>(null) }
    
    var selectedLineForMachine by remember { mutableStateOf<ProductionLine?>(null) }
    var showCleanCloudDialog by remember { mutableStateOf(false) }

    var lastError by remember { mutableStateOf<String?>(null) }

    fun showSnackbar(message: String, isError: Boolean = false) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = if (isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
        }
    }

    fun executeAction(action: () -> Unit, successMessage: String, errorMessage: String = "Erro ao executar ação") {
        try {
            action()
            showSnackbar(successMessage)
        } catch (e: Exception) {
            lastError = e.message ?: errorMessage
            showSnackbar(errorMessage, isError = true)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            "MENU" -> "Configurações"
                            "MACHINES" -> "Máquinas e Linhas"
                            "STOCK" -> "Locais de Estoque"
                            "USERS" -> "Gerenciar Usuários"
                            "CLEANUP" -> "Armazenamento e Limpeza"
                            else -> "App"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    if (currentScreen != "MENU") {
                        IconButton(onClick = { currentScreen = "MENU" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            executeAction(
                                action = { viewModel.syncMachineConfiguration() },
                                successMessage = "Configurações sincronizadas",
                                errorMessage = "Erro ao sincronizar configurações"
                            )
                        },
                        modifier = Modifier.semantics { contentDescription = "Sincronizar configurações" }
                    ) {
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
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            when (currentScreen) {
                "MACHINES" -> ExtendedFloatingActionButton(
                    onClick = { showAddLineDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nova Linha") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                "STOCK" -> SmallFloatingActionButton(
                    onClick = { showAddLocationDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Novo Local")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (currentScreen) {
                "MENU" -> SettingsMenu(
                    onNavigate = { currentScreen = it }
                )
                "MACHINES" -> MachinesTab(
                    productionLines = productionLines,
                    allMachines = allMachines,
                    machinesWithoutLine = machinesWithoutLine,
                    onAddLine = { showAddLineDialog = true },
                    onEditLine = { lineToEdit = it },
                    onDeleteLine = { line ->
                        executeAction(
                            action = { viewModel.deleteProductionLine(line) },
                            successMessage = "Linha deletada"
                        )
                    },
                    onAddMachine = { line ->
                        selectedLineForMachine = line
                        showAddMachineDialog = true
                    },
                    onEditMachine = { machineToEdit = it },
                    onDeleteMachine = { machine ->
                        executeAction(
                            action = { viewModel.deleteMachine(machine) },
                            successMessage = "Máquina deletada"
                        )
                    }
                )
                "STOCK" -> StockTab(
                    stockLocations = stockLocations,
                    onAddLocation = { showAddLocationDialog = true },
                    onEditLocation = { locationToEdit = it },
                    onDeleteLocation = { location ->
                        executeAction(
                            action = { viewModel.deleteStockLocation(location) },
                            successMessage = "Local deletado"
                        )
                    }
                )
                "USERS" -> AdminUserContent(viewModel = viewModel)
                "CLEANUP" -> CleanupTab(
                    onCleanCloud = { showCleanCloudDialog = true },
                    onCleanLocal = {
                        executeAction(
                            action = { viewModel.clearSyncedLocalData() },
                            successMessage = "Memória Local Otimizada"
                        )
                    }
                )
            }
        }
    }

    // Diálogos de Criação
    if (showAddLineDialog) {
        NameDialog(
            title = "Nova Linha de Produção",
            onDismiss = { showAddLineDialog = false },
            onConfirm = { name ->
                executeAction({ viewModel.addProductionLine(name) }, "Linha adicionada")
                showAddLineDialog = false
            }
        )
    }

    if (showAddLocationDialog) {
        NameDialog(
            title = "Novo Local de Estoque",
            onDismiss = { showAddLocationDialog = false },
            onConfirm = { name ->
                executeAction({ viewModel.addStockLocation(name) }, "Local adicionado")
                showAddLocationDialog = false
            }
        )
    }

    if (showAddMachineDialog) {
        NameDialog(
            title = if (selectedLineForMachine != null) "Nova Máquina em ${selectedLineForMachine?.name}" else "Nova Máquina Avulsa",
            onDismiss = { showAddMachineDialog = false },
            onConfirm = { name ->
                executeAction({ viewModel.addMachine(name, selectedLineForMachine?.id) }, "Máquina adicionada")
                showAddMachineDialog = false
            }
        )
    }

    // Diálogos de Edição
    lineToEdit?.let { line ->
        NameDialog(
            title = "Editar Linha",
            initialName = line.name,
            onDismiss = { lineToEdit = null },
            onConfirm = { newName ->
                executeAction({ viewModel.updateProductionLine(line.name, newName) }, "Linha atualizada")
                lineToEdit = null
            }
        )
    }

    machineToEdit?.let { machine ->
        NameDialog(
            title = "Editar Máquina",
            initialName = machine.name,
            onDismiss = { machineToEdit = null },
            onConfirm = { newName ->
                executeAction({ viewModel.updateMachine(machine.name, newName) }, "Máquina atualizada")
                machineToEdit = null
            }
        )
    }

    locationToEdit?.let { location ->
        NameDialog(
            title = "Editar Local",
            initialName = location,
            onDismiss = { locationToEdit = null },
            onConfirm = { newName ->
                executeAction({ viewModel.updateStockLocation(location, newName) }, "Local atualizado")
                locationToEdit = null
            }
        )
    }

    if (showCleanCloudDialog) {
        AlertDialog(
            onDismissRequest = { showCleanCloudDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Limpar Nuvem") },
            text = { Text("Deseja remover as FOTOS dos relatórios anteriores a 30 dias na nuvem?\nO texto dos relatórios permanecerá no histórico.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        executeAction(
                            action = { viewModel.cleanOldImagesOnly() },
                            successMessage = "Limpeza iniciada",
                            errorMessage = "Erro ao limpar"
                        )
                        showCleanCloudDialog = false
                    }
                ) { Text("Sim, Limpar Imagens") }
            },
            dismissButton = {
                TextButton(onClick = { showCleanCloudDialog = false }) { Text("Cancelar") }
            }
        )
    }

    lastError?.let { error ->
        AlertDialog(
            onDismissRequest = { lastError = null },
            title = { Text("Erro") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { lastError = null }) { Text("OK") }
            }
        )
    }
}

// --- Menu Dashboard Principal ---
@Composable
fun SettingsMenu(onNavigate: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsMenuItem(
                title = "🏭 Máquinas e Linhas",
                description = "Adicione ou edite linhas de produção e suas máquinas",
                onClick = { onNavigate("MACHINES") }
            )
        }
        item {
            SettingsMenuItem(
                title = "🏢 Locais de Estoque",
                description = "Gerencie os pontos de armazenamento de peças",
                onClick = { onNavigate("STOCK") }
            )
        }
        item {
            SettingsMenuItem(
                title = "👥 Usuários",
                description = "Gerencie aprovações e privilégios da equipe",
                onClick = { onNavigate("USERS") }
            )
        }
        item {
            SettingsMenuItem(
                title = "💾 Armazenamento e Limpeza",
                description = "Configure manutenção do app e libere espaço offline",
                onClick = { onNavigate("CLEANUP") }
            )
        }
    }
}

@Composable
fun SettingsMenuItem(title: String, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// --- Componentes das Abas Internas ---

@Composable
fun CleanupTab(onCleanCloud: () -> Unit, onCleanLocal: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Limpeza Local (Recomendado)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Remove todos os relatórios offline que já foram sincronizados com sucesso na nuvem. Isso libera muita memória no seu celular.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCleanLocal,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Limpar Lixo Local") }
                }
            }
        }
        
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Limpeza da Nuvem",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Libere espaço removendo as FOTOS dos relatórios de todos os aparelhos antigos (>30 dias).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCleanCloud,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Limpar Imagens da Nuvem") }
                }
            }
        }
    }
}

@Composable
fun MachinesTab(
    productionLines: List<ProductionLine>,
    allMachines: List<Machine>,
    machinesWithoutLine: List<Machine>,
    onAddLine: () -> Unit,
    onEditLine: (ProductionLine) -> Unit,
    onDeleteLine: (ProductionLine) -> Unit,
    onAddMachine: (ProductionLine?) -> Unit,
    onEditMachine: (Machine) -> Unit,
    onDeleteMachine: (Machine) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionHeader("Máquinas Avulsas") }

        if (machinesWithoutLine.isNotEmpty()) {
            items(machinesWithoutLine, key = { it.id }) { machine ->
                MachineItem(
                    machine = machine,
                    onEdit = { onEditMachine(machine) },
                    onDelete = { onDeleteMachine(machine) }
                )
            }
        } else {
            item { EmptyStateMessage("Nenhuma máquina avulsa.", "Adicione máquinas independentes de linhas.") }
        }

        item {
            OutlinedButton(
                onClick = { onAddMachine(null) },
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
            item { EmptyStateMessage("Nenhuma linha cadastrada.", "Crie linhas para organizar.") }
        }

        items(productionLines, key = { it.id }) { line ->
            ProductionLineItem(
                line = line,
                machines = allMachines.filter { it.lineId == line.id },
                onEditLine = { onEditLine(line) },
                onDeleteLine = { onDeleteLine(line) },
                onAddMachineToLine = { onAddMachine(line) },
                onEditMachine = onEditMachine,
                onDeleteMachine = onDeleteMachine
            )
        }
    }
}

@Composable
fun StockTab(
    stockLocations: List<String>,
    onAddLocation: () -> Unit,
    onEditLocation: (String) -> Unit,
    onDeleteLocation: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    shape = RoundedCornerShape(24.dp),
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
                        Row {
                            IconButton(onClick = { onEditLocation(location) }) {
                                Icon(Icons.Filled.Edit, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { onDeleteLocation(location) }) {
                                Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        } else {
            item { EmptyStateMessage("Nenhum local cadastrado.", "Adicione locais para seu estoque.") }
        }
    }
}


// --- Componentes Reutilizáveis ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun EmptyStateMessage(text: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.Info, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f), fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductionLineItem(
    line: ProductionLine,
    machines: List<Machine>,
    onEditLine: () -> Unit,
    onDeleteLine: () -> Unit,
    onAddMachineToLine: () -> Unit,
    onEditMachine: (Machine) -> Unit,
    onDeleteMachine: (Machine) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "rotation")

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f).clickable { expanded = !expanded }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ViewList, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(line.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (!expanded) {
                            Text("${machines.size} máquinas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Filled.ExpandMore, "Expandir", modifier = Modifier.rotate(rotationState))
                    }
                    IconButton(onClick = onEditLine) {
                        Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDeleteLine) {
                        Icon(Icons.Filled.Delete, "Deletar", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                    if (machines.isNotEmpty()) {
                        machines.forEach { machine ->
                            MachineItem(machine = machine, onEdit = { onEditMachine(machine) }, onDelete = { onDeleteMachine(machine) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        Text("Nenhuma máquina cadastrada.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(start = 44.dp, bottom = 8.dp))
                    }

                    FilledTonalButton(
                        onClick = onAddMachineToLine,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adicionar Máquina")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MachineItem(machine: Machine, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Build, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(machine.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).padding(end = 4.dp)) {
                    Icon(Icons.Filled.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, "Deletar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NameDialog(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Nome não pode estar vazio", color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    } else {
                        isError = true
                    }
                }
            ) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(28.dp)
    )
}