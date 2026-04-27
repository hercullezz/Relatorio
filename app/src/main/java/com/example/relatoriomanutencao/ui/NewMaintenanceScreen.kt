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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import java.util.TimeZone
import com.example.relatoriomanutencao.utils.ShiftManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMaintenanceScreen(viewModel: MainViewModel, onBack: () -> Unit = {}) {
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
    val previousShift = remember { ShiftManager.getPreviousShiftInfo() }
    val usePreviousShift by viewModel.usePreviousShift.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setUsePreviousShift(false)
        }
    }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }

    // Câmera e Galeria
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri != null) {
            if (selectedImageUris.size < 3) {
                selectedImageUris = selectedImageUris + cameraUri!!
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }.format(Date())
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

    Scaffold(containerColor = Color.Transparent) { paddingValues ->
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
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface),
                    maxLines = 10
                )

                // --- Fotos ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Fotos", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(containerColor = if (selectedImageUris.size == 3) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer) {
                        Text("${selectedImageUris.size}/3", modifier = Modifier.padding(4.dp))
                    }
                }

                // Aviso sobre limite
                if (selectedImageUris.size < 3) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Máximo de 3 fotos para o layout do relatório.", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
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
                            cameraUri = uri
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

                // --- Informações do turno e opção de turno anterior ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Linha: turno atual
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Turno atual: ${currentShift.shiftName}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Text(
                            text = dateFormat.format(Date(currentShift.workDate.time)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }

                    // Checkbox turno anterior
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = usePreviousShift,
                            onCheckedChange = { viewModel.setUsePreviousShift(it) },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.surface, uncheckedColor = Color.White, checkmarkColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (usePreviousShift) Color.White else Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Adicionar ao turno anterior",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (usePreviousShift) Color.White else Color.LightGray
                        )
                    }

                    // Card informativo quando turno anterior está selecionado
                    if (usePreviousShift) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Será salvo no turno anterior:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = previousShift.shiftName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = dateFormat.format(Date(previousShift.workDate.time)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
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
                                overrideShiftId = if (usePreviousShift) previousShift.shiftId else null,
                                overrideWorkDateMillis = if (usePreviousShift) previousShift.workDate.time else null
                            )

                            description = ""
                            selectedImageUris = emptyList()
                            if (!isGraphMode) {
                                selectedMachine = null
                            }
                            viewModel.setUsePreviousShift(false)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    enabled = isFormValid
                ) {
                    Text(if (isGraphMode) "Salvar Gráfico" else "Salvar Registro")
                }
            }
        }
    }
}
