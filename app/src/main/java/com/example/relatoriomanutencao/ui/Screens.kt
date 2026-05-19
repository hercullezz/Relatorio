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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
        containerColor = Color.Transparent,
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Digite para buscar...") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (stockItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if(searchQuery.length < 2) "Digite para buscar no catálogo.\nUse o botão (+) ao lado do item para dar entrada." 
                            else "Nenhum item encontrado.", 
                            color = Color.LightGray,
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
    val cardColor = if (item.quantity > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(), 
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
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
                Card(colors = CardDefaults.cardColors(containerColor = cardColor), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp).sizeIn(minWidth = 44.dp), contentAlignment = Alignment.Center) { Text(text = item.quantity.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
    Scaffold(containerColor = Color.Transparent, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Relatórios Salvos", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            if (fileList.isEmpty()) Text("Nenhum relatório encontrado.", modifier = Modifier.padding(top = 16.dp), color = Color.LightGray)
            else {
                LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                    items(fileList) { file ->
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
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

    var itemToDelete by remember { mutableStateOf<MaintenanceItem?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    val editingItem by viewModel.itemToEdit.collectAsState()

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
                "PREVIOUS" -> {
                    val prevShift = ShiftManager.getPreviousShiftInfo()
                    val itemShift = item.shiftId ?: ShiftManager.getShiftInfo(Instant.ofEpochMilli(item.date)).shiftId
                    itemShift == prevShift.shiftId && isSameDay(itemWorkDate, prevShift.workDate.time)
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
         containerColor = Color.Transparent,
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
                             } else if (filterMode == "CURRENT") {
                                 ShiftManager.getCurrentShiftInfo()
                             } else if (filterMode == "PREVIOUS") {
                                 ShiftManager.getPreviousShiftInfo()
                             } else null
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
                 Text(text = "Histórico de Manutenção", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                 Spacer(modifier = Modifier.height(8.dp))
                 OutlinedTextField(
                     value = searchQuery, 
                     onValueChange = { searchQuery = it }, 
                     label = { Text("Buscar serviço") }, 
                     modifier = Modifier.fillMaxWidth(), 
                     shape = RoundedCornerShape(12.dp),
                     colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
                     leadingIcon = { Icon(Icons.Default.Search, null) }
                 )
                 Spacer(modifier = Modifier.height(8.dp))
                 Row(
                     modifier = Modifier.horizontalScroll(rememberScrollState()),
                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                    FilterChip(selected = (filterMode == "CURRENT"), onClick = { filterMode = "CURRENT" }, label = { Text("Atual") })
                    FilterChip(selected = (filterMode == "PREVIOUS"), onClick = { filterMode = "PREVIOUS" }, label = { Text("Anterior") })
                    FilterChip(selected = (filterMode == "DAY"), onClick = { filterMode = "DAY" }, label = { Text("Dia") })
                    FilterChip(selected = (filterMode == "RANGE"), onClick = { filterMode = "RANGE" }, label = { Text("Período") })
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))

                 val unsyncedCount = allItems.count { !it.isSynced }
                 if (unsyncedCount > 0) {
                     Card(
                         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                         modifier = Modifier.fillMaxWidth().clickable { viewModel.syncPendingItems() }.padding(bottom = 8.dp)
                     ) {
                         Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = MaterialTheme.colorScheme.onErrorContainer)
                             Spacer(modifier = Modifier.width(8.dp))
                             Text(
                                 text = "$unsyncedCount serviço(s) aguardando envio. Toque para sincronizar.",
                                 color = MaterialTheme.colorScheme.onErrorContainer,
                                 style = MaterialTheme.typography.bodyMedium,
                                 fontWeight = FontWeight.Bold
                             )
                         }
                     }
                 }

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
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nenhum serviço encontrado.", color = Color.LightGray) }
                 } else {
                     LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                         items(displayedItems) { item -> MaintenanceItemCard(item = item, onDelete = { itemToDelete = item }, onEdit = { viewModel.setItemToEdit(item) }) }
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
     if (editingItem != null) EditServiceDialog(
         item = editingItem!!,
         onDismiss = { viewModel.setItemToEdit(null) },
         onConfirm = { newDesc, newPhotos, newShiftId, newWorkDateMillis ->
             viewModel.updateMaintenanceItem(editingItem!!, newDesc, newPhotos, newShiftId, newWorkDateMillis)
             viewModel.setItemToEdit(null)
         }
     )
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
    
    Card(
        elevation = CardDefaults.cardElevation(8.dp), 
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = item.machine, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp).padding(end = 4.dp)) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.secondary) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "T$displayShiftId - $workDateStr", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = MaterialTheme.colorScheme.onPrimaryContainer, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (item.serviceType == "Gráfico de Produção") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gráfico", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                when {
                    item.isPendingDeletion -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aguardando exclusão", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    !item.isSynced || item.isPendingUpdate -> {
                        val label = if (!item.isSynced) "Aguardando envio" else "Alteração pendente"
                        val icon = if (!item.isSynced) Icons.Default.CloudUpload else Icons.Default.Sync
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Icon(icon, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "há $timeString", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceDialog(item: MaintenanceItem, onDismiss: () -> Unit, onConfirm: (String, String, Int?, Long?) -> Unit) {
    var description by remember { mutableStateOf(item.description) }
    var currentPhotos by remember { mutableStateOf(if (item.photoUris.isNotBlank()) item.photoUris.split(",").filter { it.isNotBlank() } else emptyList()) }

    // Turno atual do item e turno anterior calculado
    val itemShiftId = remember { item.shiftId ?: ShiftManager.getShiftInfo(java.time.Instant.ofEpochMilli(item.date)).shiftId }
    val previousShift = remember { ShiftManager.getPreviousShiftInfo() }

    // Controla qual turno está selecionado: null = mantém o original
    var selectedShiftId by remember { mutableStateOf<Int?>(null) }
    var selectedWorkDateMillis by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean -> 
        if (success) cameraUri?.let { uri -> currentPhotos = currentPhotos + uri.toString() } 
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris -> 
        val remainingSlots = 6 - currentPhotos.size
        if (remainingSlots > 0) {
            currentPhotos = currentPhotos + uris.take(remainingSlots).map { it.toString() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Editar Serviço", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(item.machine, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar")
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = { onConfirm(description, currentPhotos.joinToString(","), selectedShiftId, selectedWorkDateMillis) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar Alterações", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("O que foi feito?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        placeholder = { Text("Descreva o serviço realizado...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Seletor de Turno ---
                    Text("Turno do Serviço", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))

                    val activeShift = selectedShiftId ?: itemShiftId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Botão: turno original do item
                        FilterChip(
                            selected = (activeShift == itemShiftId && selectedShiftId == null),
                            onClick = {
                                selectedShiftId = null
                                selectedWorkDateMillis = null
                            },
                            label = {
                                Text(
                                    text = "T$itemShiftId — Atual",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = if (activeShift == itemShiftId && selectedShiftId == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        // Botão: turno anterior
                        FilterChip(
                            selected = (selectedShiftId == previousShift.shiftId),
                            onClick = {
                                selectedShiftId = previousShift.shiftId
                                selectedWorkDateMillis = previousShift.workDate.time
                            },
                            label = {
                                Text(
                                    text = "T${previousShift.shiftId} — Anterior",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = if (selectedShiftId == previousShift.shiftId) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (selectedShiftId != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "O serviço será movido para o ${previousShift.shiftName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fotos do Serviço", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${currentPhotos.size}/6", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { 
                                val storageDir = context.getExternalFilesDir("maintenance_photos")
                                val file = File(storageDir, "photo_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                cameraUri = uri
                                cameraLauncher.launch(uri)
                            },
                            enabled = currentPhotos.size < 6,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Câmera")
                        }
                        
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            enabled = currentPhotos.size < 6,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Galeria")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Photos Grid
                    if (currentPhotos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhuma foto anexada", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        currentPhotos.chunked(2).forEach { rowPhotos ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowPhotos.forEach { photo ->
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                        Card(
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(16.dp),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {
                                            AsyncImage(
                                                model = photo,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                        IconButton(
                                            onClick = { currentPhotos = currentPhotos - photo },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                                .size(28.dp)
                                                .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                if (rowPhotos.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
