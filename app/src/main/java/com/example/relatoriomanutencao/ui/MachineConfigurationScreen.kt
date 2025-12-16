package com.example.relatoriomanutencao.ui

<<<<<<< HEAD
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
=======
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
<<<<<<< HEAD
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
=======
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.* // Importa todos os componentes Material3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MachineConfigurationScreen(
    viewModel: MainViewModel = viewModel()
) {
    val productionLines by viewModel.allProductionLines.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()
    val machinesWithoutLine by viewModel.machinesWithoutLine.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estados para adicionar/editar linha
    var showAddLineDialog by remember { mutableStateOf(false) }
    var showAddMachineDialog by remember { mutableStateOf(false) }
    var selectedLineForMachine by remember { mutableStateOf<ProductionLine?>(null) }

    // Estados para exclusão de linha
    var showDeleteLineDialog by remember { mutableStateOf(false) }
    var lineToDeleteId by remember { mutableStateOf<String?>(null) }
    var lineToDeleteName by remember { mutableStateOf<String?>(null) }

    // Estados para edição de linha
    var showEditLineDialog by remember { mutableStateOf(false) }
    var lineToEdit by remember { mutableStateOf<Line?>(null) }
    var lineEditName by remember { mutableStateOf("") }

    // Estados para gerenciamento de máquinas
    var showAddMachineDialog by remember { mutableStateOf(false) }
    var showEditMachineDialog by remember { mutableStateOf(false) }
    var showDeleteMachineDialog by remember { mutableStateOf(false) }

    var currentLineForMachineAction by remember { mutableStateOf<Line?>(null) }
    var machineToEditOrDelete by remember { mutableStateOf<String?>(null) }
    var newMachineName by remember { mutableStateOf("") }
    var oldMachineName by remember { mutableStateOf("") }

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
            ExtendedFloatingActionButton(
                onClick = { showAddLineDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nova Linha") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Gerenciamento de Máquinas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Organize suas linhas de produção e equipamentos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Button(onClick = { viewModel.clearError() }) {
                            Text("Ok")
                        }
                    }
                }
            }

<<<<<<< HEAD
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
=======
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(lines) { line ->
                    LineItem(
                        line = line,
                        viewModel = viewModel,
                        onDeleteLineLongClick = { 
                            lineToDeleteId = line.id
                            lineToDeleteName = line.name
                            showDeleteLineDialog = true
                        },
                        onEditLineClick = { 
                            lineToEdit = line
                            lineEditName = line.name
                            showEditLineDialog = true
                        },
                        onAddMachineClick = { 
                            currentLineForMachineAction = line
                            newMachineName = ""
                            showAddMachineDialog = true
                        },
                        onEditMachineClick = { selectedLine, oldName -> 
                            currentLineForMachineAction = selectedLine
                            oldMachineName = oldName
                            newMachineName = oldName
                            showEditMachineDialog = true
                        },
                        onDeleteMachineClick = { selectedLine, name -> 
                            currentLineForMachineAction = selectedLine
                            machineToEditOrDelete = name
                            showDeleteMachineDialog = true
                        }
                    )
                }

                if (lines.isEmpty() && !isLoading && error == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhuma linha configurada.\nClique no + para adicionar uma nova.",
                                style = MaterialTheme.typography.bodyMedium
                            )
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
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
<<<<<<< HEAD
=======

        // Diálogo para adicionar nova linha
        if (showAddLineDialog) {
            AlertDialog(
                onDismissRequest = { showAddLineDialog = false },
                title = { Text("Adicionar Nova Linha") },
                text = {
                    OutlinedTextField(
                        value = newLineEditName,
                        onValueChange = { newLineEditName = it },
                        label = { Text("Nome da Linha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newLineEditName.isNotBlank()) {
                                viewModel.addLine(newLineEditName)
                                newLineEditName = ""
                                showAddLineDialog = false
                            }
                        }
                    ) {
                        Text("Adicionar")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        newLineEditName = ""
                        showAddLineDialog = false 
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo de confirmação de exclusão de linha
        if (showDeleteLineDialog) { // <-- Variável correta usada aqui
            AlertDialog(
                onDismissRequest = { showDeleteLineDialog = false },
                title = { Text("Confirmar Exclusão de Linha") },
                text = { Text("Você tem certeza que deseja excluir a linha '${lineToDeleteName}' e todas as suas máquinas?") },
                confirmButton = {
                    Button(
                        onClick = {
                            lineToDeleteId?.let { id ->
                                viewModel.deleteLine(id)
                            }
                            showDeleteLineDialog = false // <-- Correção aqui
                            lineToDeleteId = null
                            lineToDeleteName = null
                        }
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showDeleteLineDialog = false // <-- Correção aqui
                        lineToDeleteId = null
                        lineToDeleteName = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para editar nome da linha
        if (showEditLineDialog) {
            AlertDialog(
                onDismissRequest = { showEditLineDialog = false },
                title = { Text("Editar Linha") },
                text = {
                    OutlinedTextField(
                        value = lineEditName,
                        onValueChange = { lineEditName = it },
                        label = { Text("Nome da Linha") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lineToEdit?.let { line ->
                                if (lineEditName.isNotBlank() && lineEditName != line.name) {
                                    viewModel.updateLine(line.copy(name = lineEditName))
                                }
                            }
                            showEditLineDialog = false
                            lineToEdit = null
                            lineEditName = ""
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showEditLineDialog = false
                        lineToEdit = null
                        lineEditName = ""
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para adicionar máquina
        if (showAddMachineDialog) {
            AlertDialog(
                onDismissRequest = { showAddMachineDialog = false },
                title = { Text("Adicionar Máquina a ${currentLineForMachineAction?.name}") },
                text = {
                    OutlinedTextField(
                        value = newMachineName,
                        onValueChange = { newMachineName = it },
                        label = { Text("Nome da Máquina") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentLineForMachineAction?.let { line ->
                                if (newMachineName.isNotBlank()) {
                                    viewModel.addMachineToLine(line.id, newMachineName)
                                }
                            }
                            showAddMachineDialog = false
                            newMachineName = ""
                            currentLineForMachineAction = null
                        }
                    ) {
                        Text("Adicionar")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showAddMachineDialog = false
                        newMachineName = ""
                        currentLineForMachineAction = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo para editar máquina
        if (showEditMachineDialog) {
            AlertDialog(
                onDismissRequest = { showEditMachineDialog = false },
                title = { Text("Editar Máquina em ${currentLineForMachineAction?.name}") },
                text = {
                    OutlinedTextField(
                        value = newMachineName,
                        onValueChange = { newMachineName = it },
                        label = { Text("Novo Nome da Máquina") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentLineForMachineAction?.let { line ->
                                machineToEditOrDelete?.let { old ->
                                    if (newMachineName.isNotBlank() && newMachineName != old) {
                                        viewModel.editMachineInLine(line.id, old, newMachineName)
                                    }
                                }
                            }
                            showEditMachineDialog = false
                            newMachineName = ""
                            oldMachineName = ""
                            machineToEditOrDelete = null
                            currentLineForMachineAction = null
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showEditMachineDialog = false
                        newMachineName = ""
                        oldMachineName = ""
                        machineToEditOrDelete = null
                        currentLineForMachineAction = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Diálogo de confirmação de exclusão de máquina
        if (showDeleteMachineDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteMachineDialog = false },
                title = { Text("Confirmar Exclusão de Máquina") },
                text = { Text("Você tem certeza que deseja excluir a máquina '${machineToEditOrDelete}' da linha '${currentLineForMachineAction?.name}'?") },
                confirmButton = {
                    Button(
                        onClick = {
                            currentLineForMachineAction?.let { line ->
                                machineToEditOrDelete?.let { machineName ->
                                    viewModel.removeMachineFromLine(line.id, machineName)
                                }
                            }
                            showDeleteMachineDialog = false
                            machineToEditOrDelete = null
                            currentLineForMachineAction = null
                        }
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    Button(onClick = { 
                        showDeleteMachineDialog = false
                        machineToEditOrDelete = null
                        currentLineForMachineAction = null
                    }) {
                        Text("Cancelar")
                    }
                }
            )
        }
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
    }
} // Corrigido: Adicionada a chave de fechamento que faltava para o Scaffold

@OptIn(ExperimentalFoundationApi::class)
@Composable
<<<<<<< HEAD
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
=======
fun LineItem(
    line: Line,
    viewModel: MachineConfigurationViewModel,
    onDeleteLineLongClick: (Line) -> Unit,
    onEditLineClick: (Line) -> Unit,
    onAddMachineClick: (Line) -> Unit,
    onEditMachineClick: (Line, String) -> Unit,
    onDeleteMachineClick: (Line, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = { /* Não faz nada com clique simples na linha por enquanto */ },
                onLongClick = { onDeleteLineLongClick(line) }
            ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = line.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    IconButton(onClick = { onEditLineClick(line) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Linha")
                    }
                    IconButton(onClick = { onDeleteLineLongClick(line) }) { // Ícone de lixeira para exclusão rápida/alternativa
                        Icon(Icons.Default.Delete, contentDescription = "Excluir Linha")
                    }
                    IconButton(onClick = { onAddMachineClick(line) }) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Máquina")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (line.machines.isNotEmpty()) {
                Text("Máquinas:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    line.machines.forEach { machine ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "- $machine",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row {
                                IconButton(onClick = { onEditMachineClick(line, machine) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Máquina")
                                }
                                IconButton(onClick = { onDeleteMachineClick(line, machine) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir Máquina")
                                }
                            }
                        }
                    }
                }
            } else {
                Text("Nenhuma máquina nesta linha.", style = MaterialTheme.typography.bodySmall)
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
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
