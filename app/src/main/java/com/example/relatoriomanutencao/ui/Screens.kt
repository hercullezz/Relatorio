package com.example.relatoriomanutencao.ui

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.relatoriomanutencao.data.MaintenanceItem
import com.example.relatoriomanutencao.data.StockItem
import com.example.relatoriomanutencao.utils.PdfGenerator
import com.example.relatoriomanutencao.utils.ShiftManager
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.parse.ParseUser
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*
import kotlin.math.max
import kotlin.math.min

// --- Stock Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: MainViewModel) {
    val stockItems by viewModel.stockItems.collectAsState()
    val searchQuery by viewModel.stockSearchQuery.collectAsState()
    val stockLocations by viewModel.stockLocations.collectAsState() // Locais para Entrada Manual
    val isLoading by viewModel.isLoading.collectAsState() // Observa o estado de carregamento
    
    // Estado para Entrada via Lista (Item Específico)
    var itemToAddStock by remember { mutableStateOf<StockItem?>(null) }
    
    // Estado para Confirmação de Entrada (Dados a confirmar)
    var stockEntryConfirmation by remember { mutableStateOf<Triple<StockItem, Int, String>?>(null) }
    
    // Estado para proteção de importação
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    
    // Estados para Confirmação de Baixa
    var itemToConsume by remember { mutableStateOf<StockItem?>(null) }

    // Launcher para selecionar arquivo Excel ou CSV
    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importExcelData(uri)
            }
        }
    )

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFloatingActionButton(
                    onClick = { 
                        // Pede senha antes de abrir o seletor
                        showImportPasswordDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Upload, contentDescription = "Importar Planilha")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = { Text("Pesquisar (Código ou Descrição)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Digite para buscar...") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (stockItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if(searchQuery.length < 2) "Digite para buscar no catálogo.\nUse o botão (+) ao lado do item para dar entrada." 
                            else "Nenhum item encontrado.", 
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(stockItems) { item ->
                            StockItemCard(
                                item = item,
                                onConsumeClick = { itemToConsume = item },
                                onAddClick = { 
                                    itemToAddStock = item
                                }
                            )
                        }
                    }
                }
            }

            // Indicador de Carregamento (Loading)
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(), 
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processando...", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGOS ---

    // 1. Entrada Específica (Vindo da Lista - Código Travado)
    if (itemToAddStock != null) {
        ManualEntryDialog(
            locations = stockLocations,
            initialCode = itemToAddStock!!.code,
            isCodeReadOnly = true, // Trava o campo de código
            onDismiss = { itemToAddStock = null },
            onConfirm = { code, qty, loc ->
                // Prepara para confirmação em vez de salvar direto
                stockEntryConfirmation = Triple(itemToAddStock!!, qty, loc)
                itemToAddStock = null
            }
        )
    }
    
    // 1.1 Confirmação de Entrada Detalhada
    if (stockEntryConfirmation != null) {
        val (item, qty, loc) = stockEntryConfirmation!!
        StockEntryConfirmationDialog(
            item = item,
            quantityToAdd = qty,
            location = loc,
            onDismiss = { stockEntryConfirmation = null },
            onConfirm = {
                viewModel.addStockEntry(item.code, qty, loc)
                stockEntryConfirmation = null
            }
        )
    }
    
    // 2. Senha para Importação
    if (showImportPasswordDialog) {
        PasswordDialog(
            onDismiss = { showImportPasswordDialog = false },
            onConfirm = {
                showImportPasswordDialog = false
                // Abre o seletor permitindo CSV e Excel após senha correta
                excelPickerLauncher.launch(arrayOf(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                    "application/vnd.ms-excel", // .xls
                    "text/comma-separated-values", // .csv padrão
                    "text/csv", // .csv alternativo
                    "text/plain", // .txt ou csv genérico
                    "*/*" // Fallback
                ))
            }
        )
    }

    // 3. Confirmação de Baixa
    if (itemToConsume != null) {
        var quantityToConsume by remember { mutableIntStateOf(1) }
        
        AlertDialog(
            onDismissRequest = { itemToConsume = null },
            icon = { Icon(Icons.Default.Output, contentDescription = null) },
            title = { Text("Baixa de Estoque") },
            text = {
                Column {
                    Text("Item: ${itemToConsume!!.description}")
                    Text("Saldo Atual: ${itemToConsume!!.quantity}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantityToConsume > 1) quantityToConsume-- }) {
                            Icon(Icons.Default.Remove, null)
                        }
                        Text(
                            text = quantityToConsume.toString(),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        IconButton(onClick = { 
                            if (quantityToConsume < itemToConsume!!.quantity) quantityToConsume++ 
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                    if (itemToConsume!!.quantity == 0) {
                        Text("Sem saldo para baixar!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.consumeStock(itemToConsume!!.code, quantityToConsume)
                        itemToConsume = null
                    },
                    enabled = itemToConsume!!.quantity > 0
                ) {
                    Text("Confirmar Baixa")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToConsume = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StockEntryConfirmationDialog(
    item: StockItem,
    quantityToAdd: Int,
    location: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Entrada") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Verifique os dados antes de confirmar:")
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                DetailRow("Código", item.code)
                DetailRow("Descrição", item.description)
                // Se houver descrição detalhada no futuro, adicionar aqui
                DetailRow("Local", location)
                DetailRow("Saldo Atual", item.quantity.toString())
                DetailRow("Qtd. a Adicionar", "+$quantityToAdd", Color(0xFF2E7D32)) // Verde
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                DetailRow(
                    "Saldo Final", 
                    "${item.quantity + quantityToAdd}", 
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Corrigir")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, color: Color = Color.Unspecified, fontWeight: FontWeight? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium, 
            color = color, 
            fontWeight = fontWeight,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.6f) // Limita largura para não quebrar layout
        )
    }
}

@Composable
fun PasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acesso Restrito") },
        text = {
            Column {
                Text("Digite a senha administrativa para importar dados:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        isError = false
                    },
                    label = { Text("Senha") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text("Senha incorreta.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (password == "9615") {
                    onConfirm()
                } else {
                    isError = true
                }
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryDialog(
    locations: List<String>,
    initialCode: String = "",
    isCodeReadOnly: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var code by remember { mutableStateOf(initialCode) }
    var quantityStr by remember { mutableStateOf("1") }
    var selectedLocation by remember { mutableStateOf("") }
    var isLocationExpanded by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(isCodeReadOnly) "Adicionar ao Estoque" else "Entrada Manual") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { if(!isCodeReadOnly) code = it },
                    label = { Text("Código da Peça") },
                    readOnly = isCodeReadOnly,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if(isCodeReadOnly) OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) else OutlinedTextFieldDefaults.colors()
                )
                
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantityStr = it },
                    label = { Text("Quantidade (Adicionar)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = isLocationExpanded,
                    onExpandedChange = { isLocationExpanded = !isLocationExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Local (Obrigatório)") },
                        placeholder = { Text("Selecione o local") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLocationExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        isError = locationError,
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isLocationExpanded,
                        onDismissRequest = { isLocationExpanded = false }
                    ) {
                        if (locations.isEmpty()) {
                             DropdownMenuItem(
                                text = { Text("Nenhum local cadastrado em Config.") },
                                onClick = { isLocationExpanded = false },
                                enabled = false
                            )
                        } else {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc) },
                                    onClick = {
                                        selectedLocation = loc
                                        isLocationExpanded = false
                                        locationError = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (locationError) {
                    Text("Selecione um local!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toIntOrNull() ?: 0
                    if (code.isNotBlank() && qty > 0 && selectedLocation.isNotBlank()) {
                        onConfirm(code, qty, selectedLocation)
                    } else {
                        if (selectedLocation.isBlank()) {
                            locationError = true
                        }
                    }
                }
            ) {
                Text("Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StockItemCard(
    item: StockItem,
    onConsumeClick: () -> Unit,
    onAddClick: () -> Unit // Nova ação
) {
    val cardColor = if (item.quantity > 0) Color(0xFF2E7D32) else Color.Gray // Verde vs Cinza
    val contentColor = Color.White
    
    // Ferramentas para Copiar
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description, 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                // Código com Botão Copiar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cód: ${item.code}", style = MaterialTheme.typography.bodySmall)
                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(item.code))
                            Toast.makeText(context, "Código copiado: ${item.code}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar Código",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (item.address.isNotBlank()) {
                    Text("Local: ${item.address}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .sizeIn(minWidth = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.quantity.toString(),
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botões de Ação: Entrada (+) e Baixa (-)
                Row {
                    // Botão Entrada (Verde)
                    FilledTonalIconButton(
                        onClick = onAddClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color(0xFFE8F5E9), 
                            contentColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Entrada", modifier = Modifier.size(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Botão Baixa (Padrão)
                    FilledTonalIconButton(
                        onClick = onConsumeClick,
                        enabled = item.quantity > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Baixa", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// --- Saved PDFs Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedReportsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var fileList by remember { mutableStateOf(listOf<File>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    // Função para refresh da lista
    fun refreshFileList() {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        fileList = dir?.listFiles()?.filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            ?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }

    // Refresh automático ao abrir a aba
    LaunchedEffect(Unit) {
        refreshFileList()
    }

    // Formatação de data
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Text("Relatórios Salvos", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (fileList.isEmpty()) {
                Text("Nenhum relatório encontrado.")
            } else {
                LazyColumn {
                    items(fileList) { file ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    // Lógica para ABRIR o PDF
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Nenhum leitor de PDF encontrado.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(file.name, fontWeight = FontWeight.Bold)
                                    Text("Tamanho: ${file.length() / 1024} KB", style = MaterialTheme.typography.bodySmall)
                                    Text("Criado em: ${dateFormat.format(file.lastModified())}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text("(Toque para abrir)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }

                                IconButton(onClick = {
                                    // Share Intent
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Compartilhar PDF"))
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                                }

                                IconButton(onClick = {
                                    fileToDelete = file
                                    showDeleteDialog = true
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diálogo de confirmação de exclusão
        if (showDeleteDialog && fileToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Excluir Relatório") },
                text = { Text("Tem certeza que deseja excluir o relatório '${fileToDelete?.name}'? Esta ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = {
                            try {
                                if (fileToDelete?.delete() == true) {
                                    refreshFileList()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Relatório excluído com sucesso")
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Erro ao excluir o relatório")
                                    }
                                }
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Erro: ${e.message}")
                                }
                            }
                            showDeleteDialog = false
                            fileToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// --- Services List Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesListScreen(viewModel: MainViewModel) {
    val allItems by viewModel.maintenanceItems.collectAsState()
    val context = LocalContext.current

    // Busca
    var searchQuery by remember { mutableStateOf("") }

    // Estado de Filtro: modos -> "CURRENT", "DAY", "WEEK", "RANGE"
    var filterMode by remember { mutableStateOf("CURRENT") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var rangeStartMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var rangeEndMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    // Seleção de Turnos (multi-select)
    var shift1 by remember { mutableStateOf(true) }
    var shift2 by remember { mutableStateOf(true) }
    var shift3 by remember { mutableStateOf(true) }

    // Estado para controle de edição e exclusão
    var editingItem by remember { mutableStateOf<MaintenanceItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MaintenanceItem?>(null) }

    // Estado de carregamento do PDF
    var isGeneratingPdf by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Filter Logic: date range + shift multi-select + search
    val displayedItems = remember(
        allItems, filterMode, selectedDateMillis, rangeStartMillis, rangeEndMillis,
        shift1, shift2, shift3, searchQuery
    ) {
        val lowerQuery = searchQuery.trim().lowercase(Locale.getDefault())

        if (filterMode == "CURRENT") {
            // Use device current shift/time as source of truth for 'Turno Atual'
            val now = Instant.now()
            val currentShiftInfo = ShiftManager.getCurrentShiftInfo()
            val currentShiftId = currentShiftInfo.shiftId
            val currentWorkDateMillis = currentShiftInfo.workDate.time

            // Shift sequence definitions mirror ShiftManager
            val shiftDef = listOf(
                ShiftManager.Shift(1, "", LocalTime.of(5, 0), LocalTime.of(13, 40), false),
                ShiftManager.Shift(2, "", LocalTime.of(13, 40), LocalTime.of(22, 0), false),
                ShiftManager.Shift(3, "", LocalTime.of(22, 0), LocalTime.of(5, 0), true)
            ).find { it.id == currentShiftId } ?: ShiftManager.Shift(1, "", LocalTime.of(5, 0), LocalTime.of(13, 40), false)

            val workLocalDate = Instant.ofEpochMilli(currentWorkDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val windowStart = LocalDateTime.of(workLocalDate, shiftDef.start)
            val windowEnd = if (shiftDef.crossesMidnight) {
                LocalDateTime.of(workLocalDate.plusDays(1), shiftDef.end)
            } else {
                LocalDateTime.of(workLocalDate, shiftDef.end)
            }

            val windowStartInstant = windowStart.atZone(ZoneId.systemDefault()).toInstant()
            val windowEndInstant = windowEnd.atZone(ZoneId.systemDefault()).toInstant()

            allItems.filter { item ->
                val itemInstant = Instant.ofEpochMilli(item.date)

                // Prefer shift/workDate salvos pelo servidor (se estiverem presentes)
                val itemWorkDateMillis = item.workDateMillisFromServer ?: item.date
                val itemShift = item.shiftId ?: currentShiftId.takeIf { itemWorkDateMillis == currentWorkDateMillis }
                    ?: ShiftManager.getShiftInfo(itemInstant).shiftId

                val inCurrentWindow = itemInstant >= windowStartInstant && itemInstant <= windowEndInstant
                val matchesShiftAndWorkDate = (itemShift == currentShiftId && itemWorkDateMillis == currentWorkDateMillis)
                val matchesWorkDateOnly = (itemWorkDateMillis == currentWorkDateMillis && item.shiftId == null)

                val matchesSearch = if (lowerQuery.isEmpty()) true else {
                    (item.machine.lowercase().contains(lowerQuery) || item.description.lowercase().contains(lowerQuery) || item.serviceType.lowercase().contains(lowerQuery))
                }

                (inCurrentWindow || matchesShiftAndWorkDate || matchesWorkDateOnly) && matchesSearch
            }
        } else {
            // determine date window
            val calStart = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
            val startMillis: Long
            val endMillis: Long

            when (filterMode) {
                "DAY" -> {
                    // Para DAY, o intervalo é baseado nos turnos selecionados
                    val selectedLocalDate = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                    
                    val shiftWindows = mutableListOf<Pair<Long, Long>>()
                    
                    if (shift1) {
                        val start = LocalDateTime.of(selectedLocalDate, LocalTime.of(5, 0))
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val end = LocalDateTime.of(selectedLocalDate, LocalTime.of(13, 40))
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        shiftWindows.add(start to end)
                    }
                    
                    if (shift2) {
                        val start = LocalDateTime.of(selectedLocalDate, LocalTime.of(13, 40))
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val end = LocalDateTime.of(selectedLocalDate, LocalTime.of(22, 0))
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        shiftWindows.add(start to end)
                    }
                    
                    if (shift3) {
                        val start = LocalDateTime.of(selectedLocalDate, LocalTime.of(21, 30))
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val nextDay = selectedLocalDate.plusDays(1)
                        val end = LocalDateTime.of(nextDay, LocalTime.of(6, 30))
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        shiftWindows.add(start to end)
                    }
                    
                    if (shiftWindows.isNotEmpty()) {
                        startMillis = shiftWindows.minOf { it.first }
                        endMillis = shiftWindows.maxOf { it.second }
                    } else {
                        // Fallback se nenhum turno selecionado (não deve acontecer)
                        calStart.set(Calendar.HOUR_OF_DAY, 0); calStart.set(Calendar.MINUTE, 0); calStart.set(Calendar.SECOND, 0); calStart.set(Calendar.MILLISECOND, 0)
                        startMillis = calStart.timeInMillis
                        calStart.add(Calendar.DAY_OF_YEAR, 1)
                        endMillis = calStart.timeInMillis - 1
                    }
                }
                "WEEK" -> {
                    val firstDay = calStart.getFirstDayOfWeek()
                    while (calStart.get(Calendar.DAY_OF_WEEK) != firstDay) calStart.add(Calendar.DAY_OF_YEAR, -1)
                    calStart.set(Calendar.HOUR_OF_DAY, 0); calStart.set(Calendar.MINUTE, 0); calStart.set(Calendar.SECOND, 0); calStart.set(Calendar.MILLISECOND, 0)
                    startMillis = calStart.timeInMillis
                    calStart.add(Calendar.DAY_OF_YEAR, 7)
                    endMillis = calStart.timeInMillis - 1
                }
                else -> { // RANGE
                    val normalizedStart = min(rangeStartMillis, rangeEndMillis)
                    val normalizedEnd = max(rangeStartMillis, rangeEndMillis)
                    rangeStartMillis = normalizedStart
                    rangeEndMillis = normalizedEnd
                    val sCal = Calendar.getInstance().apply { timeInMillis = normalizedStart }
                    sCal.set(Calendar.HOUR_OF_DAY, 0); sCal.set(Calendar.MINUTE, 0); sCal.set(Calendar.SECOND, 0); sCal.set(Calendar.MILLISECOND, 0)
                    val eCal = Calendar.getInstance().apply { timeInMillis = normalizedEnd }
                    eCal.set(Calendar.HOUR_OF_DAY, 23); eCal.set(Calendar.MINUTE, 59); eCal.set(Calendar.SECOND, 59); eCal.set(Calendar.MILLISECOND, 999)
                    startMillis = sCal.timeInMillis
                    endMillis = eCal.timeInMillis
                }
            }

            val selectedShifts = mutableSetOf<Int>()
            if (shift1) selectedShifts.add(1)
            if (shift2) selectedShifts.add(2)
            if (shift3) selectedShifts.add(3)

            // Calcular workDates esperadas e janela de tempo para cada turno no dia selecionado
            val selectedLocalDate = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
            val shiftWindowMap = mutableMapOf<Int, Pair<Long, Long>>() // shiftId -> (startMillis, endMillis)
            
            // Turno 1: 05:00 - 13:40 (mesmo dia)
            if (shift1) {
                val start = LocalDateTime.of(selectedLocalDate, LocalTime.of(5, 0))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = LocalDateTime.of(selectedLocalDate, LocalTime.of(13, 40))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                shiftWindowMap[1] = start to end
            }
            
            // Turno 2: 13:40 - 22:00 (mesmo dia)
            if (shift2) {
                val start = LocalDateTime.of(selectedLocalDate, LocalTime.of(13, 40))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val end = LocalDateTime.of(selectedLocalDate, LocalTime.of(22, 0))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                shiftWindowMap[2] = start to end
            }
            
            // Turno 3: 21:30 (dia selecionado) até 06:30 (dia seguinte)
            if (shift3) {
                val start = LocalDateTime.of(selectedLocalDate, LocalTime.of(21, 30))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val nextDay = selectedLocalDate.plusDays(1)
                val end = LocalDateTime.of(nextDay, LocalTime.of(6, 30))
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                shiftWindowMap[3] = start to end
            }

            allItems.filter { item ->
                val inRange = item.date in startMillis..endMillis
                
                if (!inRange) return@filter false

                val itemInstant = Instant.ofEpochMilli(item.date)

                // Se há turnos selecionados, verificar se item cai dentro da janela horária de pelo menos um turno selecionado
                val matchesShift = if (selectedShifts.isEmpty()) {
                    true // Se nenhum turno selecionado, aceita tudo dentro do dia
                } else {
                    // Verificar se itemInstant está dentro de qualquer janela de turno selecionado
                    selectedShifts.any { shiftId ->
                        val window = shiftWindowMap[shiftId]
                        window != null && itemInstant.toEpochMilli() >= window.first && itemInstant.toEpochMilli() <= window.second
                    }
                }

                val matchesSearch = if (lowerQuery.isEmpty()) true else {
                    (item.machine.lowercase().contains(lowerQuery) || item.description.lowercase().contains(lowerQuery) || item.serviceType.lowercase().contains(lowerQuery))
                }
                matchesShift && matchesSearch
            }
        }
    }

    // Date Picker Logic
    if (showDayPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDayPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val localMidnight = java.time.Instant.ofEpochMilli(utcMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        selectedDateMillis = localMidnight
                    }
                    showDayPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDayPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = rangeStartMillis)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val localMidnight = java.time.Instant.ofEpochMilli(utcMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        rangeStartMillis = localMidnight
                    }
                    showStartPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }
    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = rangeEndMillis)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val localMidnight = java.time.Instant.ofEpochMilli(utcMillis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                            .atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        rangeEndMillis = localMidnight
                    }
                    showEndPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
         floatingActionButton = {
             ExtendedFloatingActionButton(
                 onClick = {
                     if (displayedItems.isNotEmpty()) {
                         coroutineScope.launch {
                             isGeneratingPdf = true
                             val shiftInfoForPdf = when (filterMode) {
                                 "CURRENT" -> ShiftManager.getCurrentShiftInfo()
                                 "DAY" -> if (selectedShifts.size == 1) {
                                     val selectedShiftId = selectedShifts.first()
                                     val workDateMillis = Instant.ofEpochMilli(selectedDateMillis).atZone(ZoneId.systemDefault())
                                         .toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                     ShiftManager.getShiftInfoForShiftIdAndWorkDate(selectedShiftId, workDateMillis)
                                 } else null
                                 else -> null
                             }
                             PdfGenerator.generateConsolidatedReport(context, displayedItems, shiftInfoForPdf)
                             isGeneratingPdf = false
                         }
                     } else {
                         Toast.makeText(context, "A lista está vazia!", Toast.LENGTH_SHORT).show()
                     }
                 },
                 icon = { 
                     if (isGeneratingPdf) {
                         CircularProgressIndicator(
                             modifier = Modifier.size(24.dp),
                             color = MaterialTheme.colorScheme.onPrimaryContainer,
                             strokeWidth = 2.dp
                         )
                     } else {
                         Icon(Icons.Default.PictureAsPdf, "Gerar Relatório")
                     }
                 },
                 text = { 
                     Text(if (isGeneratingPdf) "Gerando PDF..." else "Gerar PDF (${displayedItems.size})") 
                 }
             )
         }
     ) { paddingValues ->
         Box(modifier = Modifier.fillMaxSize()) {
             Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                 
                // --- HEADER E FILTROS ---
                Text(
                    text = "Histórico",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar (máquina, descrição, tipo)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = (filterMode == "CURRENT"), onClick = { filterMode = "CURRENT" }, label = { Text("Turno Atual") })
                    FilterChip(selected = (filterMode == "DAY"), onClick = { filterMode = "DAY" }, label = { Text("Dia") })
                    FilterChip(selected = (filterMode == "WEEK"), onClick = { filterMode = "WEEK" }, label = { Text("Semana") })
                    FilterChip(selected = (filterMode == "RANGE"), onClick = { filterMode = "RANGE" }, label = { Text("Período") })
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = {
                        filterMode = "RANGE"
                        val now = System.currentTimeMillis()
                        rangeEndMillis = now
                        rangeStartMillis = now - 7L * 24 * 60 * 60 * 1000
                    }, modifier = Modifier.weight(1f)) {
                        Text("Últimos 7 dias")
                    }
                    OutlinedButton(onClick = {
                        filterMode = "RANGE"
                        val now = System.currentTimeMillis()
                        rangeEndMillis = now
                        rangeStartMillis = now - 30L * 24 * 60 * 60 * 1000
                    }, modifier = Modifier.weight(1f)) {
                        Text("Últimos 30 dias")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("${displayedItems.size} serviços encontrados", style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = {
                        filterMode = "CURRENT"
                        selectedDateMillis = System.currentTimeMillis()
                        rangeStartMillis = System.currentTimeMillis()
                        rangeEndMillis = System.currentTimeMillis()
                        searchQuery = ""
                        shift1 = true; shift2 = true; shift3 = true
                    }) {
                        Text("Limpar filtros")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Shifts multi-select
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shift1, onCheckedChange = { shift1 = it })
                        Text("1º turno")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shift2, onCheckedChange = { shift2 = it })
                        Text("2º turno")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shift3, onCheckedChange = { shift3 = it })
                        Text("3º turno")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (filterMode) {
                    "DAY" -> {
                        val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
                        val dateString = dateFormat.format(Date(selectedDateMillis))
                        OutlinedButton(onClick = { showDayPicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = dateString)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    "WEEK" -> {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        val firstDay = cal.getFirstDayOfWeek()
                        while (cal.get(Calendar.DAY_OF_WEEK) != firstDay) cal.add(Calendar.DAY_OF_YEAR, -1)
                        val start = dateFormat.format(Date(cal.timeInMillis))
                        cal.add(Calendar.DAY_OF_YEAR, 6)
                        val end = dateFormat.format(Date(cal.timeInMillis))
                        Text("Semana: $start - $end", style = MaterialTheme.typography.bodySmall)
                    }
                    "RANGE" -> {
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showStartPicker = true }, modifier = Modifier.weight(1f)) { Text(dateFormat.format(Date(rangeStartMillis))) }
                            OutlinedButton(onClick = { showEndPicker = true }, modifier = Modifier.weight(1f)) { Text(dateFormat.format(Date(rangeEndMillis))) }
                        }
                    }
                    else -> {
                        val current = ShiftManager.getCurrentShiftInfo()
                        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text("Turno atual: ${current.shiftName} (data de trabalho: ${df.format(current.workDate)})", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                 // --- LISTA DE ITENS ---
                 if (displayedItems.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(
                                 if(filterMode == "CURRENT") "Nenhum serviço neste turno." else "Nenhum serviço encontrado.",
                                 color = Color.Gray
                             )
                         }
                     }
                 } else {
                     LazyColumn(
                         modifier = Modifier.fillMaxSize(),
                         verticalArrangement = Arrangement.spacedBy(8.dp),
                         contentPadding = PaddingValues(bottom = 80.dp) // Espaço para o FAB
                     ) {
                         items(displayedItems) { item ->
                             MaintenanceItemCard(
                                 item = item, 
                                 onDelete = { itemToDelete = item }, // Aciona o diálogo
                                 onEdit = { editingItem = item }
                             )
                         }
                     }
                 }
             }
             
             // Loading Overlay (Opcional)
             if (isGeneratingPdf) {
                 Surface(
                     modifier = Modifier.fillMaxSize(),
                     color = Color.Black.copy(alpha = 0.5f)
                 ) {
                     Box(contentAlignment = Alignment.Center) {
                        Card {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Processando imagens...", fontWeight = FontWeight.Bold)
                                Text("Gerando PDF Otimizado", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                     }
                 }
             }
         }
     }
     
     // Diálogo de Edição
     if (editingItem != null) {
         EditServiceDialog(
             item = editingItem!!,
             onDismiss = { editingItem = null },
             onConfirm = { newDescription, newPhotos ->
                 viewModel.updateMaintenanceItem(editingItem!!, newDescription, newPhotos)
                 editingItem = null
             }
         )
     }
     
     // Diálogo de Confirmação de Exclusão
     if (itemToDelete != null) {
         AlertDialog(
             onDismissRequest = { itemToDelete = null },
             title = { Text("Excluir Serviço") },
             text = { Text("Tem certeza que deseja excluir o serviço da máquina '${itemToDelete?.machine}'? Essa ação não pode ser desfeita.") },
             confirmButton = {
                 Button(
                     onClick = {
                         itemToDelete?.let { viewModel.deleteMaintenanceItem(it) }
                         itemToDelete = null
                     },
                     colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                 ) {
                     Text("Excluir")
                 }
             },
             dismissButton = {
                 TextButton(onClick = { itemToDelete = null }) {
                     Text("Cancelar")
                 }
             }
         )
     }
}

@Composable
fun EditServiceDialog(
    item: MaintenanceItem,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit // Agora recebe descrição e fotos
) {
    var description by remember { mutableStateOf(item.description) }
    
    // Lista mutável de strings (URLs ou URIs locais)
    // Inicializa com as fotos existentes (separadas por vírgula)
    var currentPhotos by remember { 
        mutableStateOf(
            if (item.photoUris.isNotBlank()) 
                item.photoUris.split(",").filter { it.isNotBlank() } 
            else emptyList()
        ) 
    }
    
    val context = LocalContext.current
    
    // Camera Logic
    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri.value != null) {
            currentPhotos = currentPhotos + cameraUri.value.toString()
        }
    }
    
    // Gallery Logic
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        currentPhotos = currentPhotos + uris.map { it.toString() }
    }
    
    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("maintenance_photos")
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Serviço") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Máquina: ${item.machine}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 10
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gerenciamento de Fotos
                Text("Fotos", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     Button(onClick = {
                        val uri = createImageUri()
                        cameraUri.value = uri
                        cameraLauncher.launch(uri)
                     }, modifier = Modifier.weight(1f)) {
                         Icon(Icons.Default.CameraAlt, null)
                         Spacer(Modifier.width(4.dp))
                         Text("Câmera")
                     }
                     OutlinedButton(onClick = { 
                         galleryLauncher.launch("image/*") 
                     }, modifier = Modifier.weight(1f)) {
                         Icon(Icons.Default.PhotoLibrary, null)
                         Spacer(Modifier.width(4.dp))
                         Text("Galeria")
                     }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (currentPhotos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(currentPhotos) { photoPath ->
                            Box(modifier = Modifier.size(100.dp)) {
                                AsyncImage(
                                    model = photoPath,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                // Botão Remover
                                IconButton(
                                    onClick = { 
                                        currentPhotos = currentPhotos.filter { it != photoPath } 
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .background(Color.White, CircleShape)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close, 
                                        contentDescription = "Remover", 
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("Sem fotos.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                // Junta a lista final em uma string separada por vírgula
                val finalPhotosString = currentPhotos.joinToString(",")
                onConfirm(description, finalPhotosString) 
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceItemCard(
    item: MaintenanceItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit // Parâmetro para editar
) {
    val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.date))
    
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.machine, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(item.serviceType, modifier = Modifier.padding(4.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = dateString, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
