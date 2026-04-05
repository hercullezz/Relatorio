package com.example.relatoriomanutencao.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import coil.compose.rememberAsyncImagePainter
import com.example.relatoriomanutencao.data.MaintenanceItem
import com.example.relatoriomanutencao.data.StockItem
import com.example.relatoriomanutencao.utils.PdfGenerator
import com.example.relatoriomanutencao.utils.ShiftManager
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
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
    val stockLocations by viewModel.stockLocations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var itemToAddStock by remember { mutableStateOf<StockItem?>(null) }
    var stockEntryConfirmation by remember { mutableStateOf<Triple<StockItem, Int, String>?>(null) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var itemToConsume by remember { mutableStateOf<StockItem?>(null) }

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
                    onClick = { showImportPasswordDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Upload, contentDescription = "Importar Planilha")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                                onAddClick = { itemToAddStock = item }
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), modifier = Modifier.fillMaxSize()) {}
                    Card(elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Processando...", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (itemToAddStock != null) {
        ManualEntryDialog(
            locations = stockLocations,
            initialCode = itemToAddStock!!.code,
            isCodeReadOnly = true,
            onDismiss = { itemToAddStock = null },
            onConfirm = { code, qty, loc ->
                stockEntryConfirmation = Triple(itemToAddStock!!, qty, loc)
                itemToAddStock = null
            }
        )
    }
    
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
    
    if (showImportPasswordDialog) {
        PasswordDialog(
            onDismiss = { showImportPasswordDialog = false },
            onConfirm = {
                showImportPasswordDialog = false
                excelPickerLauncher.launch(arrayOf(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.ms-excel",
                    "text/comma-separated-values",
                    "text/csv",
                    "text/plain",
                    "*/*"
                ))
            }
        )
    }

    if (itemToConsume != null) {
        var quantityToConsume by remember { mutableIntStateOf(1) }
        AlertDialog(
            onDismissRequest = { itemToConsume = null },
            icon = { Icon(Icons.Default.Output, null) },
            title = { Text("Baixa de Estoque") },
            text = {
                Column {
                    Text("Item: ${itemToConsume!!.description}")
                    Text("Saldo Atual: ${itemToConsume!!.quantity}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantityToConsume > 1) quantityToConsume-- }) { Icon(Icons.Default.Remove, null) }
                        Text(text = quantityToConsume.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
                        IconButton(onClick = { if (quantityToConsume < itemToConsume!!.quantity) quantityToConsume++ }) { Icon(Icons.Default.Add, null) }
                    }
                    if (itemToConsume!!.quantity == 0) {
                        Text("Sem saldo para baixar!", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.consumeStock(itemToConsume!!.code, quantityToConsume); itemToConsume = null }, enabled = itemToConsume!!.quantity > 0) { Text("Confirmar Baixa") }
            },
            dismissButton = { TextButton(onClick = { itemToConsume = null }) { Text("Cancelar") } }
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
                DetailRow("Local", location)
                DetailRow("Saldo Atual", item.quantity.toString())
                DetailRow("Qtd. a Adicionar", "+$quantityToAdd", Color(0xFF2E7D32))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DetailRow("Saldo Final", "${item.quantity + quantityToAdd}", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Corrigir") } }
    )
}

@Composable
fun DetailRow(label: String, value: String, color: Color = Color.Unspecified, fontWeight: FontWeight? = null) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = fontWeight, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(0.6f))
    }
}

@Composable
fun PasswordDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acesso Restrito") },
        text = {
            Column {
                Text("Digite a senha administrativa:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; isError = false },
                    label = { Text("Senha") },
                    singleLine = true,
                    isError = isError,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) Text("Senha incorreta.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = { if (password == "9615") onConfirm() else isError = true }) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryDialog(locations: List<String>, initialCode: String = "", isCodeReadOnly: Boolean = false, onDismiss: () -> Unit, onConfirm: (String, Int, String) -> Unit) {
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
                OutlinedTextField(value = code, onValueChange = { if(!isCodeReadOnly) code = it }, label = { Text("Código") }, readOnly = isCodeReadOnly, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = quantityStr, onValueChange = { if (it.all { c -> c.isDigit() }) quantityStr = it }, label = { Text("Quantidade") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = isLocationExpanded, onExpandedChange = { isLocationExpanded = !isLocationExpanded }) {
                    OutlinedTextField(value = selectedLocation, onValueChange = {}, readOnly = true, label = { Text("Local") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLocationExpanded) }, colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), isError = locationError, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = isLocationExpanded, onDismissRequest = { isLocationExpanded = false }) {
                        locations.forEach { loc -> DropdownMenuItem(text = { Text(loc) }, onClick = { selectedLocation = loc; isLocationExpanded = false; locationError = false }) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { val qty = quantityStr.toIntOrNull() ?: 0; if (code.isNotBlank() && qty > 0 && selectedLocation.isNotBlank()) onConfirm(code, qty, selectedLocation) else if (selectedLocation.isBlank()) locationError = true }) { Text("Continuar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun StockItemCard(item: StockItem, onConsumeClick: () -> Unit, onAddClick: () -> Unit) {
    val cardColor = if (item.quantity > 0) Color(0xFF2E7D32) else Color.Gray
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.description, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cód: ${item.code}", style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(item.code)); Toast.makeText(context, "Copiado!", Toast.LENGTH_SHORT).show() }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) }
                }
                if (item.address.isNotBlank()) Text("Local: ${item.address}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Card(colors = CardDefaults.cardColors(containerColor = cardColor), shape = RoundedCornerShape(8.dp)) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).sizeIn(minWidth = 40.dp), contentAlignment = Alignment.Center) { Text(text = item.quantity.toString(), color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    FilledTonalIconButton(onClick = onAddClick, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)), modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = onConsumeClick, enabled = item.quantity > 0, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedReportsScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var fileList by remember { mutableStateOf(listOf<File>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }
    fun refreshFileList() {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        fileList = dir?.listFiles()?.filter { it.isFile && it.extension.equals("pdf", ignoreCase = true) }?.sortedBy { it.lastModified() } ?: emptyList()
    }
    LaunchedEffect(Unit) { refreshFileList() }
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Relatórios Salvos", style = MaterialTheme.typography.headlineSmall)
            if (fileList.isEmpty()) Text("Nenhum relatório encontrado.", modifier = Modifier.padding(top = 16.dp))
            else {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(fileList) { file ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, "application/pdf"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                            context.startActivity(Intent.createChooser(intent, "Abrir PDF"))
                        }) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(file.name, fontWeight = FontWeight.Bold)
                                    Text("${file.length() / 1024} KB", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply { type = "application/pdf"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                                    context.startActivity(Intent.createChooser(intent, "Compartilhar"))
                                }) { Icon(Icons.Default.Share, null) }
                                IconButton(onClick = { fileToDelete = file; showDeleteDialog = true }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                            }
                        }
                    }
                }
            }
        }
        if (showDeleteDialog) {
            AlertDialog(onDismissRequest = { showDeleteDialog = false }, title = { Text("Excluir") }, text = { Text("Excluir '${fileToDelete?.name}'?") },
                confirmButton = { Button(onClick = { fileToDelete?.delete(); refreshFileList(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Excluir") } },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") } })
        }
    }
}

// --- Services List Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesListScreen(viewModel: MainViewModel) {
    val allItems by viewModel.maintenanceItems.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.refreshMaintenanceList()
    }

    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("CURRENT") }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var rangeStartMillis by remember { mutableStateOf(System.currentTimeMillis() - 7 * 86400000L) }
    var rangeEndMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDayPicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var shift1 by remember { mutableStateOf(true) }
    var shift2 by remember { mutableStateOf(true) }
    var shift3 by remember { mutableStateOf(true) }

    var editingItem by remember { mutableStateOf<MaintenanceItem?>(null) }
    var itemToDelete by remember { mutableStateOf<MaintenanceItem?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun isSameDay(millisA: Long, millisB: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = millisA }
        val calB = Calendar.getInstance().apply { timeInMillis = millisB }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
               calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }

    val displayedItems = remember(
        allItems, filterMode, selectedDateMillis, rangeStartMillis, rangeEndMillis,
        shift1, shift2, shift3, searchQuery
    ) {
        val lowerQuery = searchQuery.trim().lowercase(Locale.getDefault())

        allItems.filter { item ->
            val matchesSearch = if (lowerQuery.isEmpty()) true else {
                item.machine.lowercase().contains(lowerQuery) || 
                item.description.lowercase().contains(lowerQuery) || 
                item.serviceType.lowercase().contains(lowerQuery)
            }
            if (!matchesSearch) return@filter false

            val itemWorkDate = item.workDateMillisFromServer ?: run {
                ShiftManager.getShiftInfo(Instant.ofEpochMilli(item.date)).workDate.time
            }

            when (filterMode) {
                "CURRENT" -> {
                    val visibleShifts = ShiftManager.getVisibleShiftInfos()
                    visibleShifts.any { shift ->
                        val itemShift = item.shiftId ?: ShiftManager.getShiftInfo(Instant.ofEpochMilli(item.date)).shiftId
                        itemShift == shift.shiftId && isSameDay(itemWorkDate, shift.workDate.time)
                    }
                }
                "DAY" -> {
                    val selectedShifts = mutableListOf<Int>()
                    if (shift1) selectedShifts.add(1)
                    if (shift2) selectedShifts.add(2)
                    if (shift3) selectedShifts.add(3)
                    
                    val dateMatches = isSameDay(itemWorkDate, selectedDateMillis)
                    val itemShift = item.shiftId ?: ShiftManager.getShiftInfo(Instant.ofEpochMilli(item.date)).shiftId
                    val shiftMatches = if (selectedShifts.isEmpty()) true else selectedShifts.contains(itemShift)
                    dateMatches && shiftMatches
                }
                else -> { // RANGE
                    val start = min(rangeStartMillis, rangeEndMillis)
                    val end = max(rangeStartMillis, rangeEndMillis)
                    val calItem = Calendar.getInstance().apply { timeInMillis = itemWorkDate }
                    calItem.set(Calendar.HOUR_OF_DAY, 0); calItem.set(Calendar.MINUTE, 0); calItem.set(Calendar.SECOND, 0); calItem.set(Calendar.MILLISECOND, 0)
                    val itemDayStart = calItem.timeInMillis
                    itemDayStart in start..end
                }
            }
        }
    }

    if (showDayPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(onDismissRequest = { showDayPicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { selectedDateMillis = it }; showDayPicker = false }) { Text("OK") } }) { DatePicker(state = datePickerState) }
    }
    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = rangeStartMillis)
        DatePickerDialog(onDismissRequest = { showStartPicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { rangeStartMillis = it }; showStartPicker = false }) { Text("OK") } }) { DatePicker(state = datePickerState) }
    }
    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = rangeEndMillis)
        DatePickerDialog(onDismissRequest = { showEndPicker = false }, confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { rangeEndMillis = it }; showEndPicker = false }) { Text("OK") } }) { DatePicker(state = datePickerState) }
    }

    Scaffold(
         floatingActionButton = {
             ExtendedFloatingActionButton(
                 onClick = {
                     if (displayedItems.isNotEmpty()) {
                         coroutineScope.launch {
                             isGeneratingPdf = true
                             val shiftInfoForPdf = if (filterMode == "DAY") {
                                 val selectedShifts = mutableListOf<Int>()
                                 if (shift1) selectedShifts.add(1)
                                 if (shift2) selectedShifts.add(2)
                                 if (shift3) selectedShifts.add(3)
                                 if (selectedShifts.size == 1) ShiftManager.getShiftInfoForShiftIdAndWorkDate(selectedShifts[0], selectedDateMillis) else null
                             } else if (filterMode == "CURRENT") ShiftManager.getCurrentShiftInfo() else null
                             PdfGenerator.generateConsolidatedReport(context, displayedItems, shiftInfoForPdf)
                             isGeneratingPdf = false
                         }
                     } else Toast.makeText(context, "Lista vazia!", Toast.LENGTH_SHORT).show()
                 },
                 icon = { Icon(Icons.Default.PictureAsPdf, "PDF") },
                 text = { Text("Relatório (${displayedItems.size})") }
             )
         }
     ) { paddingValues ->
         Box(modifier = Modifier.fillMaxSize()) {
             Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                 Text(text = "Histórico de Manutenção", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                 Spacer(modifier = Modifier.height(8.dp))
                 OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Buscar serviço") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Search, null) })
                 Spacer(modifier = Modifier.height(8.dp))
                 Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = (filterMode == "CURRENT"), onClick = { filterMode = "CURRENT" }, label = { Text("Atual") })
                    FilterChip(selected = (filterMode == "DAY"), onClick = { filterMode = "DAY" }, label = { Text("Dia") })
                    FilterChip(selected = (filterMode == "RANGE"), onClick = { filterMode = "RANGE" }, label = { Text("Período") })
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))

                 when (filterMode) {
                    "DAY" -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = shift1, onCheckedChange = { shift1 = it }); Text("T1", style = MaterialTheme.typography.bodySmall) }
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = shift2, onCheckedChange = { shift2 = it }); Text("T2", style = MaterialTheme.typography.bodySmall) }
                            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = shift3, onCheckedChange = { shift3 = it }); Text("T3", style = MaterialTheme.typography.bodySmall) }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { showDayPicker = true }) { Text(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(selectedDateMillis))) }
                        }
                    }
                    "RANGE" -> {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("De:", style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { showStartPicker = true }) { Text(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(rangeStartMillis))) }
                            Text("Até:", style = MaterialTheme.typography.bodySmall)
                            TextButton(onClick = { showEndPicker = true }) { Text(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(rangeEndMillis))) }
                        }
                    }
                 }

                 Spacer(modifier = Modifier.height(16.dp))
                 if (displayedItems.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum serviço encontrado.", color = Color.Gray) }
                 } else {
                     LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                         items(displayedItems) { item -> MaintenanceItemCard(item = item, onDelete = { itemToDelete = item }, onEdit = { editingItem = item }) }
                     }
                 }
             }
             if (isGeneratingPdf) {
                 Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.5f)) {
                     Box(contentAlignment = Alignment.Center) { Card { Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(); Spacer(Modifier.height(16.dp)); Text("Gerando PDF...", fontWeight = FontWeight.Bold) } } }
                 }
             }
         }
     }
     if (editingItem != null) EditServiceDialog(item = editingItem!!, onDismiss = { editingItem = null }, onConfirm = { newDesc, newPhotos -> viewModel.updateMaintenanceItem(editingItem!!, newDesc, newPhotos); editingItem = null })
     if (itemToDelete != null) AlertDialog(onDismissRequest = { itemToDelete = null }, title = { Text("Excluir") }, text = { Text("Excluir este serviço?") }, confirmButton = { Button(onClick = { viewModel.deleteMaintenanceItem(itemToDelete!!); itemToDelete = null }) { Text("Excluir") } }, dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancelar") } })
}

@Composable
fun MaintenanceItemCard(item: MaintenanceItem, onDelete: () -> Unit, onEdit: () -> Unit) {
    val sdf24h = SimpleDateFormat("HH:mm:ss", Locale("pt", "BR"))
    sdf24h.timeZone = TimeZone.getTimeZone("America/Porto_Velho")
    val timeString = sdf24h.format(Date(item.date))
    
    val workDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }
    val workDateStr = workDateFormat.format(Date(item.workDateMillisFromServer ?: item.date))
    
    val displayShiftId = item.shiftId ?: ShiftManager.getShiftInfo(Instant.ofEpochMilli(item.date)).shiftId
    
    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.machine, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "T$displayShiftId - $workDateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "adicionado às $timeString", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EditServiceDialog(item: MaintenanceItem, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var description by remember { mutableStateOf(item.description) }
    var currentPhotos by remember { mutableStateOf(if (item.photoUris.isNotBlank()) item.photoUris.split(",").filter { it.isNotBlank() } else emptyList()) }
    val context = LocalContext.current
    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) cameraUri.value?.let { currentPhotos = currentPhotos + it.toString() } }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris -> currentPhotos = currentPhotos + uris.map { it.toString() } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     Button(onClick = { 
                         val file = File(context.getExternalFilesDir("photos"), "photo_${System.currentTimeMillis()}.jpg")
                         val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                         cameraUri.value = uri
                         cameraLauncher.launch(uri)
                     }) { Text("Foto") }
                     Button(onClick = { galleryLauncher.launch("image/*") }) { Text("Galeria") }
                }
                LazyRow(modifier = Modifier.padding(top = 8.dp)) {
                    items(currentPhotos) { photo ->
                        Box(modifier = Modifier.size(80.dp).padding(4.dp)) {
                            AsyncImage(model = photo, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
                            IconButton(onClick = { currentPhotos = currentPhotos - photo }, modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.White, CircleShape)) {
                                Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(description, currentPhotos.joinToString(",")) }) { Text("Salvar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Sair") } }
    )
}
