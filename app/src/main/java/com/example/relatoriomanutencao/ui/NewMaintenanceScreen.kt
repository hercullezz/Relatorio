package com.example.relatoriomanutencao.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
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
import coil.compose.rememberAsyncImagePainter
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.DatePickerDialog
import com.example.relatoriomanutencao.utils.ShiftManager
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMaintenanceScreen(viewModel: MainViewModel) {
    // Modo de entrada: 0 = Serviço, 1 = Gráfico
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val isGraphMode = selectedTabIndex == 1

    // Estados do formulário
    var description by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("Corretiva") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // Estados para seleção de Máquina e Linha
    var selectedLine by remember { mutableStateOf<ProductionLine?>(null) }
    var selectedMachine by remember { mutableStateOf<Machine?>(null) }
    var isLineDropdownExpanded by remember { mutableStateOf(false) }
    var isMachineDropdownExpanded by remember { mutableStateOf(false) }

    // Coletando dados do ViewModel
    val productionLines by viewModel.allProductionLines.collectAsState()
    val machinesWithoutLine by viewModel.machinesWithoutLine.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()

    // Filtrando as máquinas
    val filteredMachines = remember(selectedLine, allMachines, machinesWithoutLine) {
        if (selectedLine == null) machinesWithoutLine else allMachines.filter { it.lineId == selectedLine?.id }
    }

    val context = LocalContext.current
    val currentShift = remember { ShiftManager.getCurrentShiftInfo() }
    var overrideShiftEnabled by remember { mutableStateOf(false) }
    var overrideSelectedShiftId by remember { mutableStateOf(currentShift.shiftId) }
    var overrideDateMillis by remember { mutableStateOf(currentShift.workDate.time) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Câmera e Galeria
    val cameraUri = remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri.value != null) {
            if (selectedImageUris.size < 3) {
                selectedImageUris = selectedImageUris + cameraUri.value!!
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        // Limita a adição para não ultrapassar 3 no total
        val remainingSlots = 3 - selectedImageUris.size
        if (remainingSlots > 0) {
            selectedImageUris = selectedImageUris + uris.take(remainingSlots)
        }
        if (uris.size > remainingSlots) {
             Toast.makeText(context, "Apenas 3 fotos permitidas.", Toast.LENGTH_SHORT).show()
        }
    }

    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("maintenance_photos")
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    // Resetar campos ao trocar de aba
    LaunchedEffect(selectedTabIndex) {
        description = ""
        selectedImageUris = emptyList()
        selectedMachine = null
        serviceType = if (isGraphMode) "Gráfico de Produção" else "Corretiva"
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            // --- Abas Superiores ---
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Serviço") },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Gráfico") },
                    icon = { Icon(Icons.Default.Image, contentDescription = null) }
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isGraphMode) "Novo Registro de Gráfico" else "Novo Relatório de Manutenção",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // --- Seleção de Linha (Comum aos dois) ---
                ExposedDropdownMenuBox(
                    expanded = isLineDropdownExpanded,
                    onExpandedChange = { isLineDropdownExpanded = !isLineDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLine?.name ?: "", 
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(if (isGraphMode) "Linha de Produção (Obrigatório)" else "Linha de Produção") },
                        placeholder = { Text("Selecione a Linha") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLineDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isLineDropdownExpanded,
                        onDismissRequest = { isLineDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Limpar Seleção") },
                            onClick = {
                                selectedLine = null
                                selectedMachine = null
                                isLineDropdownExpanded = false
                            }
                        )
                        productionLines.forEach { line ->
                            DropdownMenuItem(
                                text = { Text(line.name) },
                                onClick = {
                                    selectedLine = line
                                    selectedMachine = null
                                    isLineDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (!isGraphMode) {
                    // --- Seleção de Máquina (Apenas modo Serviço) ---
                    ExposedDropdownMenuBox(
                        expanded = isMachineDropdownExpanded,
                        onExpandedChange = { isMachineDropdownExpanded = !isMachineDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMachine?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Máquina") },
                            placeholder = { 
                                Text(if (selectedLine == null) "Selecione máquina sem linha" else "Selecione máquina da linha") 
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMachineDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isMachineDropdownExpanded,
                            onDismissRequest = { isMachineDropdownExpanded = false }
                        ) {
                            if (filteredMachines.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Nenhuma máquina encontrada") },
                                    onClick = { isMachineDropdownExpanded = false },
                                    enabled = false
                                )
                            } else {
                                filteredMachines.forEach { machine ->
                                    DropdownMenuItem(
                                        text = { Text(machine.name) },
                                        onClick = {
                                            selectedMachine = machine
                                            isMachineDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- Tipo de Serviço (chips iguais) ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val types = listOf("Corretiva", "Preventiva", "Preditiva", "Informação")
                        types.forEach { type ->
                            val isInfo = type == "Informação"
                            FilterChip(
                                selected = serviceType == type,
                                onClick = { serviceType = type },
                                label = { Text(type, maxLines = 1) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = if (isInfo) FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ) else FilterChipDefaults.filterChipColors()
                            )
                        }
                    }
                } else {
                    // --- Modo Gráfico Informativo ---
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(
                            text = "Este registro será salvo como 'Gráfico de Produção'. Adicione a foto da tela abaixo.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // --- Descrição ---
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isGraphMode) "Observações (Opcional)" else "Descrição") },
                    modifier = Modifier.fillMaxWidth().height(if (isGraphMode) 100.dp else 150.dp),
                    maxLines = 10
                )

                // --- Fotos ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Fotos", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = if (selectedImageUris.size == 3) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                        Text("${selectedImageUris.size}/3", modifier = Modifier.padding(4.dp))
                    }
                }
                
                // Aviso sobre limite
                if (selectedImageUris.size < 3) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Máximo de 3 fotos para o layout do relatório.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                     }
                } else {
                     Text("Limite de fotos atingido.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val uri = createImageUri()
                            cameraUri.value = uri
                            cameraLauncher.launch(uri)
                        },
                        enabled = selectedImageUris.size < 3
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Câmera")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        enabled = selectedImageUris.size < 3
                    ) {
                        Text("Galeria")
                    }
                }

                if (selectedImageUris.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedImageUris) { uri ->
                            Box(modifier = Modifier.size(100.dp)) {
                                Card(modifier = Modifier.fillMaxSize()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                // Botão X para remover foto
                                IconButton(
                                    onClick = { 
                                        selectedImageUris = selectedImageUris.filter { it != uri } 
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Informações do turno atual e opção de override ---
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Turno atual: ${currentShift.shiftName}", modifier = Modifier.weight(1f))
                        Text(text = dateFormat.format(Date(currentShift.workDate.time)), style = MaterialTheme.typography.bodySmall)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
                        Checkbox(checked = overrideShiftEnabled, onCheckedChange = { overrideShiftEnabled = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Alterar turno/data")
                    }

                    if (overrideShiftEnabled) {
                        // Seleção simples de turno
                        val shiftOptions = listOf(1 to "1º Turno (05:00-13:40)", 2 to "2º Turno (13:20-22:00)", 3 to "3º Turno (21:30-05:20)")
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            OutlinedTextField(
                                value = shiftOptions.first { it.first == overrideSelectedShiftId }.second,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Turno") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                shiftOptions.forEach { (id, label) ->
                                    DropdownMenuItem(text = { Text(label) }, onClick = { overrideSelectedShiftId = id; expanded = false })
                                }
                            }
                        }

                        // Seleção de data via DatePicker
                        val cal = Calendar.getInstance().apply { timeInMillis = overrideDateMillis }
                        val datePicker = DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val c = Calendar.getInstance()
                                c.set(y, m, d, 0, 0, 0)
                                c.set(Calendar.MILLISECOND, 0)
                                overrideDateMillis = c.timeInMillis
                            },
                            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                        )

                        OutlinedTextField(
                            value = dateFormat.format(Date(overrideDateMillis)),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Data do turno") },
                            modifier = Modifier.fillMaxWidth().clickable { datePicker.show() }
                        )
                    }
                }

                // --- Botão Salvar ---
                val isFormValid = if (isGraphMode) {
                    selectedLine != null && selectedImageUris.isNotEmpty()
                } else {
                    selectedMachine != null && description.isNotBlank()
                }

                Button(
                    onClick = {
                        if (isFormValid) {
                            val urisString = selectedImageUris.joinToString(",") { it.toString() }
                            
                            val finalMachineName = if (isGraphMode) {
                                "LINHA ${selectedLine!!.name}" 
                            } else {
                                if (selectedMachine!!.lineId != null) {
                                    val line = productionLines.find { it.id == selectedMachine!!.lineId }
                                    if (line != null) "${line.name} - ${selectedMachine!!.name}" else selectedMachine!!.name
                                } else {
                                    selectedMachine!!.name
                                }
                            }
                            
                            val finalDescription = if (isGraphMode && description.isBlank()) "Registro de Gráfico de Produção" else description

                            viewModel.addMaintenanceItem(
                                machine = finalMachineName,
                                serviceType = if (isGraphMode) "Gráfico de Produção" else serviceType,
                                description = finalDescription,
                                photoUris = urisString,
                                overrideShiftId = if (overrideShiftEnabled) overrideSelectedShiftId else null,
                                overrideWorkDateMillis = if (overrideShiftEnabled) overrideDateMillis else null
                            )
                            
                            description = ""
                            selectedImageUris = emptyList()
                            if (!isGraphMode) { 
                                selectedMachine = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid
                ) {
                    Text(if (isGraphMode) "Salvar Gráfico" else "Salvar Registro")
                }
            }
        }
    }
}
