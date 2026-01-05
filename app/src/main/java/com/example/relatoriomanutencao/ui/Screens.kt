package com.example.relatoriomanutencao.ui

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.relatoriomanutencao.data.MaintenanceItem
import com.example.relatoriomanutencao.data.StockItem
import com.example.relatoriomanutencao.utils.PdfGenerator
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- Stock Screen ---
@Composable
fun StockScreen(viewModel: MainViewModel) {
    val stockItems by viewModel.stockItems.collectAsState()
    val searchQuery by viewModel.stockSearchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState() // Observa o estado de carregamento
    
    var showSyncDialog by remember { mutableStateOf(false) }

    // Launcher para selecionar arquivo Excel ou CSV
    val excelPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importExcelData(uri)
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { 
                        // Abre o seletor permitindo CSV e Excel
                        excelPickerLauncher.launch(arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
                            "application/vnd.ms-excel", // .xls
                            "text/comma-separated-values", // .csv padrão
                            "text/csv", // .csv alternativo
                            "text/plain", // .txt ou csv genérico
                            "*/*" // Fallback para garantir que apareça em todos os dispositivos
                        ))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading // Desabilita botão durante o carregamento
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Importar CSV/Excel")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        if(searchQuery.length < 2) "Digite algo para buscar no catálogo online." 
                        else "Nenhum item encontrado.", 
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stockItems) { item ->
                        StockItemCard(item)
                    }
                }
            }
        }

        // Indicador de Carregamento (Loading)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp), 
                contentAlignment = Alignment.Center
            ) {
                // Fundo semi-transparente
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
                        Text("Buscando na nuvem...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StockItemCard(item: StockItem) {
    val cardColor = if (item.quantity > 0) Color(0xFF2E7D32) else Color.White // Dark Green vs White
    val contentColor = if (item.quantity > 0) Color.White else Color.Black

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Código: ${item.code}", fontWeight = FontWeight.Bold)
                Text("Descrição: ${item.description}")
                Text("Endereço: ${item.address}")
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .sizeIn(minWidth = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.quantity.toString(),
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Saved PDFs Screen ---
@Composable
fun SavedReportsScreen() {
    val context = LocalContext.current
    var fileList by remember { mutableStateOf(listOf<File>()) }
    
    // Refresh file list
    LaunchedEffect(Unit) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        fileList = dir?.listFiles()?.filter { it.name.endsWith(".pdf") }?.toList() ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
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
                                Text("Tamanho: ${file.length() / 1024} KB")
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
                                if (file.delete()) {
                                    // Refresh list
                                    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                                    fileList = dir?.listFiles()?.filter { it.name.endsWith(".pdf") }?.toList() ?: emptyList()
                                    Toast.makeText(context, "Arquivo excluído", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Services List Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesListScreen(viewModel: MainViewModel) {
     val allItems by viewModel.maintenanceItems.collectAsState()
     val context = LocalContext.current
     
     // Estado de Filtro
     var showOnlyCurrentShift by remember { mutableStateOf(true) }
     var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
     var showDatePicker by remember { mutableStateOf(false) }
     
     // Estado para controle de edição e exclusão
     var editingItem by remember { mutableStateOf<MaintenanceItem?>(null) }
     var itemToDelete by remember { mutableStateOf<MaintenanceItem?>(null) } // Novo estado para exclusão
     
     // Estado de carregamento do PDF
     var isGeneratingPdf by remember { mutableStateOf(false) }
     val coroutineScope = rememberCoroutineScope()

     // Filter Logic - Turno Adaptável ou Data Específica
     val displayedItems = remember(allItems, showOnlyCurrentShift, selectedDateMillis) {
         if (showOnlyCurrentShift) {
             val now = Calendar.getInstance()
             val currentHour = now.get(Calendar.HOUR_OF_DAY)
             
             // Lógica para o 3º Turno:
             // Se for de manhã (antes das 12h), pertence ao turno que começou ontem às 18h
             // Se for a tarde/noite, pertence ao turno que começou hoje às 18h
             val startFilterTime = Calendar.getInstance()
             if (currentHour < 12) {
                 startFilterTime.add(Calendar.DAY_OF_YEAR, -1)
                 startFilterTime.set(Calendar.HOUR_OF_DAY, 18)
                 startFilterTime.set(Calendar.MINUTE, 0)
             } else {
                 startFilterTime.set(Calendar.HOUR_OF_DAY, 18)
                 startFilterTime.set(Calendar.MINUTE, 0)
             }
             
             val filterTimestamp = startFilterTime.timeInMillis
             allItems.filter { it.date >= filterTimestamp }
         } else {
             // Lógica para Data Específica (Dia Calendário: 00:00 às 23:59)
             val targetCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
             val targetDay = targetCal.get(Calendar.DAY_OF_YEAR)
             val targetYear = targetCal.get(Calendar.YEAR)
             
             allItems.filter { 
                 val itemCal = Calendar.getInstance().apply { timeInMillis = it.date }
                 itemCal.get(Calendar.DAY_OF_YEAR) == targetDay && 
                 itemCal.get(Calendar.YEAR) == targetYear
             }
         }
     }
     
     // Date Picker Logic
     if (showDatePicker) {
         val datePickerState = rememberDatePickerState(
             initialSelectedDateMillis = selectedDateMillis
         )
         DatePickerDialog(
             onDismissRequest = { showDatePicker = false },
             confirmButton = {
                 TextButton(onClick = {
                     datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                     showDatePicker = false
                 }) {
                     Text("OK")
                 }
             },
             dismissButton = {
                 TextButton(onClick = { showDatePicker = false }) {
                     Text("Cancelar")
                 }
             }
         ) {
             DatePicker(state = datePickerState)
         }
     }

     Scaffold(
         floatingActionButton = {
             ExtendedFloatingActionButton(
                 onClick = {
                     if (displayedItems.isNotEmpty()) {
                         coroutineScope.launch {
                             isGeneratingPdf = true
                             PdfGenerator.generateConsolidatedReport(context, displayedItems)
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
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.SpaceBetween
                 ) {
                     Text(
                         text = "Histórico",
                         style = MaterialTheme.typography.headlineSmall,
                         fontWeight = FontWeight.Bold
                     )
                     
                     // Toggle: Turno Atual vs Data
                     FilterChip(
                         selected = showOnlyCurrentShift,
                         onClick = { showOnlyCurrentShift = !showOnlyCurrentShift },
                         label = { Text("Turno Atual") },
                         leadingIcon = {
                             if (showOnlyCurrentShift) Icon(Icons.Default.Check, null)
                         }
                     )
                 }

                 Spacer(modifier = Modifier.height(8.dp))

                 // Se NÃO for Turno Atual, mostra seletor de Data
                 if (!showOnlyCurrentShift) {
                     val dateFormat = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
                     val dateString = dateFormat.format(Date(selectedDateMillis))
                     
                     OutlinedButton(
                         onClick = { showDatePicker = true },
                         modifier = Modifier.fillMaxWidth(),
                         shape = RoundedCornerShape(8.dp)
                     ) {
                         Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                         Spacer(modifier = Modifier.width(8.dp))
                         Text(text = dateString)
                         Spacer(modifier = Modifier.weight(1f))
                         Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                     }
                 } else {
                     // Texto explicativo do Turno Atual
                     Text(
                         text = "Exibindo serviços registrados nas últimas horas (Turno Atual).",
                         style = MaterialTheme.typography.bodySmall,
                         color = Color.Gray
                     )
                 }
                 
                 Spacer(modifier = Modifier.height(16.dp))

                 // --- LISTA DE ITENS ---
                 if (displayedItems.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                             Spacer(modifier = Modifier.height(8.dp))
                             Text(
                                 if(showOnlyCurrentShift) "Nenhum serviço neste turno." 
                                 else "Nenhum serviço nesta data.", 
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
                                    modifier = Modifier.fillMaxSize().clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
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
