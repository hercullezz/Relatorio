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

// --- Cloud Screen (Revised) ---
@Composable
fun CloudScreen(viewModel: MainViewModel = viewModel()) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CloudSync, 
            contentDescription = null, 
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Sincronização Automática Ativa",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Todos os relatórios gerados são salvos automaticamente na nuvem para segurança.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Gerenciar Armazenamento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Para economizar espaço, você pode apagar registros com mais de 30 dias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showConfirmationDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Limpar Antigos (>30 dias)")
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Confirmar Limpeza") },
            text = { Text("Tem certeza que deseja apagar permanentemente todos os registros da nuvem anteriores a 30 dias atrás? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cleanOldCloudData()
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Sim, Apagar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// --- Services List Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesListScreen(viewModel: MainViewModel) {
     val allItems by viewModel.maintenanceItems.collectAsState()
     val context = LocalContext.current
     var showOnlyCurrentShift by remember { mutableStateOf(true) } // Renomeado de "Today" para "CurrentShift"
     
     // Estado para controle de edição
     var editingItem by remember { mutableStateOf<MaintenanceItem?>(null) }
     
     // Estado de carregamento do PDF
     var isGeneratingPdf by remember { mutableStateOf(false) }
     val coroutineScope = rememberCoroutineScope()

     // Filter Logic - Turno Adaptável
     val displayedItems = if (showOnlyCurrentShift) {
         val now = Calendar.getInstance()
         val currentHour = now.get(Calendar.HOUR_OF_DAY)
         val currentMinute = now.get(Calendar.MINUTE)
         
         // Lógica para o 3º Turno:
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
         allItems
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
                     Text(if (isGeneratingPdf) "Gerando PDF..." else "Gerar Relatório Consolidado") 
                 }
             )
         }
     ) { paddingValues ->
         Box(modifier = Modifier.fillMaxSize()) {
             Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                 
                 // Header com Filtro
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.SpaceBetween
                 ) {
                     Text(
                         text = if (showOnlyCurrentShift) "Turno Atual" else "Histórico Completo",
                         style = MaterialTheme.typography.headlineSmall,
                         fontWeight = FontWeight.Bold
                     )
                     FilterChip(
                         selected = showOnlyCurrentShift,
                         onClick = { showOnlyCurrentShift = !showOnlyCurrentShift },
                         label = { Text("Turno Atual") },
                         leadingIcon = {
                             if (showOnlyCurrentShift) {
                                 Icon(Icons.Default.Check, contentDescription = null)
                             }
                         }
                     )
                 }
                 Text(
                     text = if (showOnlyCurrentShift) "Exibindo itens das últimas horas (início ~18:00)" else "Exibindo todos os itens salvos",
                     style = MaterialTheme.typography.bodySmall,
                     color = Color.Gray
                 )

                 if (displayedItems.isEmpty()) {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text("Nenhum serviço encontrado no período.", color = Color.Gray)
                     }
                 } else {
                     LazyColumn(
                         modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                         verticalArrangement = Arrangement.spacedBy(8.dp),
                         contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
                     ) {
                         items(displayedItems) { item ->
                             MaintenanceItemCard(
                                 item = item, 
                                 onDelete = { viewModel.deleteMaintenanceItem(item) },
                                 onEdit = { editingItem = item }
                             )
                         }
                     }
                 }
             }
             
             // Loading Overlay (Opcional, se quiser bloquear a tela inteira)
             if (isGeneratingPdf) {
                 Surface(
                     modifier = Modifier.fillMaxSize(),
                     color = Color.Black.copy(alpha = 0.3f)
                 ) {
                     Box(contentAlignment = Alignment.Center) {
                        Card {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Baixando imagens e gerando PDF...", fontWeight = FontWeight.Bold)
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
