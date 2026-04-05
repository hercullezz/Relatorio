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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Group
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados simplificados e centralizados
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Máquinas, 1: Estoque, 2: Admin
    var showAddLineDialog by remember { mutableStateOf(false) }
    var showAddMachineDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var selectedLineForMachine by remember { mutableStateOf<ProductionLine?>(null) }
    var showCleanCloudDialog by remember { mutableStateOf(false) }

    // Estados de erro
    var lastError by remember { mutableStateOf<String?>(null) }

    // Função utilitária para mostrar snackbar
    fun showSnackbar(message: String, isError: Boolean = false) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = if (isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
        }
    }

    // Função para executar ações com tratamento de erro
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
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Configuração",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> ExtendedFloatingActionButton(
                    onClick = { showAddLineDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nova Linha") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                1 -> SmallFloatingActionButton(
                    onClick = { showAddLocationDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.semantics { contentDescription = "Adicionar novo local de estoque" }
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Novo Local")
                }
                else -> {} // No FAB for admin tab
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Tabs para separar seções
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Máquinas") },
                    icon = { Icon(Icons.Filled.Build, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Estoque") },
                    icon = { Icon(Icons.Filled.LocationOn, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Usuários") },
                    icon = { Icon(Icons.Filled.Group, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> MachinesTab(
                    productionLines = productionLines,
                    allMachines = allMachines,
                    machinesWithoutLine = machinesWithoutLine,
                    onAddLine = { showAddLineDialog = true },
                    onAddMachine = { line ->
                        selectedLineForMachine = line
                        showAddMachineDialog = true
                    },
                    onDeleteLine = { line ->
                        executeAction(
                            action = { viewModel.deleteProductionLine(line) },
                            successMessage = "Linha deletada",
                            errorMessage = "Erro ao deletar linha"
                        )
                    },
                    onDeleteMachine = { machine ->
                        executeAction(
                            action = { viewModel.deleteMachine(machine) },
                            successMessage = "Máquina deletada",
                            errorMessage = "Erro ao deletar máquina"
                        )
                    }
                )
                1 -> StockTab(
                    stockLocations = stockLocations,
                    onAddLocation = { showAddLocationDialog = true },
                    onDeleteLocation = { location ->
                        executeAction(
                            action = { viewModel.deleteStockLocation(location) },
                            successMessage = "Local deletado",
                            errorMessage = "Erro ao deletar local"
                        )
                    },
                    onCleanCloud = { showCleanCloudDialog = true }
                )
                2 -> AdminUserContent(viewModel = viewModel)
            }
        }
    }

    // Diálogos

    if (showAddLineDialog) {
        AddNameDialog(
            title = "Nova Linha de Produção",
            onDismiss = { showAddLineDialog = false },
            onConfirm = { name ->
                executeAction(
                    action = { viewModel.addProductionLine(name) },
                    successMessage = "Linha adicionada",
                    errorMessage = "Erro ao adicionar linha"
                )
                showAddLineDialog = false
            }
        )
    }

    if (showAddLocationDialog) {
        AddNameDialog(
            title = "Novo Local de Estoque",
            onDismiss = { showAddLocationDialog = false },
            onConfirm = { name ->
                executeAction(
                    action = { viewModel.addStockLocation(name) },
                    successMessage = "Local adicionado",
                    errorMessage = "Erro ao adicionar local"
                )
                showAddLocationDialog = false
            }
        )
    }

    if (showAddMachineDialog) {
        AddNameDialog(
            title = if (selectedLineForMachine != null) "Nova Máquina em ${selectedLineForMachine?.name}" else "Nova Máquina Avulsa",
            onDismiss = { showAddMachineDialog = false },
            onConfirm = { name ->
                executeAction(
                    action = { viewModel.addMachine(name, selectedLineForMachine?.id) },
                    successMessage = "Máquina adicionada",
                    errorMessage = "Erro ao adicionar máquina"
                )
                showAddMachineDialog = false
            }
        )
    }

    if (showCleanCloudDialog) {
        AlertDialog(
            onDismissRequest = { showCleanCloudDialog = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
            title = { Text("Confirmar Limpeza") },
            text = { Text("Deseja remover as FOTOS dos relatórios anteriores a 30 dias?\n\nO texto dos relatórios PERMANECERÁ no histórico, mas as imagens não serão mais exibidas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        executeAction(
                            action = { viewModel.cleanOldImagesOnly() },
                            successMessage = "Fotos antigas removidas",
                            errorMessage = "Erro ao limpar fotos"
                        )
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

    // Diálogo de erro global
    lastError?.let { error ->
        AlertDialog(
            onDismissRequest = { lastError = null },
            title = { Text("Erro") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { lastError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

// --- Componentes das Abas ---

@Composable
fun MachinesTab(
    productionLines: List<ProductionLine>,
    allMachines: List<Machine>,
    machinesWithoutLine: List<Machine>,
    onAddLine: () -> Unit,
    onAddMachine: (ProductionLine?) -> Unit,
    onDeleteLine: (ProductionLine) -> Unit,
    onDeleteMachine: (Machine) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                    onDelete = { onDeleteMachine(machine) }
                )
            }
        } else {
            item {
                EmptyStateMessage("Nenhuma máquina avulsa.", "Adicione máquinas independentes de linhas.")
            }
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
            item {
                EmptyStateMessage("Nenhuma linha de produção cadastrada.", "Crie linhas para organizar suas máquinas.")
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
                onDeleteLine = { onDeleteLine(line) },
                onAddMachineToLine = { onAddMachine(line) },
                onDeleteMachine = onDeleteMachine
            )
        }
    }
}

@Composable
fun StockTab(
    stockLocations: List<String>,
    onAddLocation: () -> Unit,
    onDeleteLocation: (String) -> Unit,
    onCleanCloud: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Seção de Nuvem
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth(),
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
                        onClick = onCleanCloud,
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

        // Seção Locais de Estoque
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
                        IconButton(
                            onClick = { onDeleteLocation(location) },
                            modifier = Modifier.semantics { contentDescription = "Deletar local $location" }
                        ) {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            item {
                EmptyStateMessage("Nenhum local cadastrado.", "Adicione locais para organizar seu estoque.")
            }
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
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun EmptyStateMessage(text: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
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
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.semantics { contentDescription = "Expandir linha ${line.name}" }
                    ) {
                        Icon(
                            Icons.Filled.ExpandMore,
                            contentDescription = "Expandir",
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                    IconButton(
                        onClick = onDeleteLine,
                        modifier = Modifier.semantics { contentDescription = "Deletar linha ${line.name}" }
                    ) {
                        Icon(
                            Icons.Filled.Delete,
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
                modifier = Modifier.size(24.dp).semantics { contentDescription = "Deletar máquina ${machine.name}" }
            ) {
                Icon(
                    Icons.Filled.Delete,
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