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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.relatoriomanutencao.data.Machine
import com.example.relatoriomanutencao.data.ProductionLine
import com.example.relatoriomanutencao.utils.ShiftAccessStatus
import com.example.relatoriomanutencao.utils.ShiftManager
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.parse.ParseUser
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMaintenanceScreen(viewModel: MainViewModel, onBack: () -> Unit = {}) {
    // Modo de entrada: 0 = Serviço, 1 = Gráfico
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val isGraphMode = selectedTabIndex == 1

    // Estados do formulário
    var description by rememberSaveable { mutableStateOf("") }
    var serviceType by rememberSaveable { mutableStateOf("Corretiva") }

    val uriListSaver = androidx.compose.runtime.saveable.listSaver<List<Uri>, String>(
        save = { it.map { uri -> uri.toString() } },
        restore = { it.map { str -> Uri.parse(str) } }
    )
    var selectedImageUris by rememberSaveable(stateSaver = uriListSaver) { mutableStateOf(emptyList<Uri>()) }

    // Estados para seleção de Máquina e Linha
    var selectedLineId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedMachineId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isLineDropdownExpanded by remember { mutableStateOf(false) }
    var isMachineDropdownExpanded by remember { mutableStateOf(false) }

    // Coletando dados do ViewModel
    val productionLines by viewModel.allProductionLines.collectAsState()
    val machinesWithoutLine by viewModel.machinesWithoutLine.collectAsState()
    val allMachines by viewModel.allMachines.collectAsState()
    val allMaintenanceItems by viewModel.maintenanceItems.collectAsState()

    val selectedLine = remember(selectedLineId, productionLines) { productionLines.find { it.id == selectedLineId } }
    val selectedMachine = remember(selectedMachineId, allMachines) { allMachines.find { it.id == selectedMachineId } }

    val filteredMachines = remember(selectedLineId, allMachines, machinesWithoutLine) {
        if (selectedLineId == null) machinesWithoutLine else allMachines.filter { it.lineId == selectedLineId }
    }

    val context = LocalContext.current

    // ── Dados do usuário e controle de acesso ───────────────────────────────
    val user = ParseUser.getCurrentUser()
    val userShiftId = user?.getInt("shiftId").let { id ->
        if (id == null || id == 0) user?.getInt("ShiftId") ?: 0 else id
    }
    val userRole = user?.getString("role") ?: ""
    val isSupervisorOrAdmin = userRole == "Supervisor" || userRole == "Administrador"

    // Estado reativo do acesso (atualiza a cada minuto)
    var shiftAccess by remember {
        mutableStateOf(ShiftManager.canUserAddService(userShiftId))
    }
    LaunchedEffect(userShiftId) {
        while (true) {
            shiftAccess = ShiftManager.canUserAddService(userShiftId)
            delay(60_000L)
        }
    }

    // Supervisor/Admin sempre pode adicionar → acesso ACTIVE
    val isAccessAllowed = isSupervisorOrAdmin || shiftAccess.status != ShiftAccessStatus.BLOCKED
    val isOvertime = !isSupervisorOrAdmin && shiftAccess.status == ShiftAccessStatus.OVERTIME

    val currentShift = remember { ShiftManager.getCurrentShiftInfo() }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("America/Porto_Velho")
    }

    // Câmera e Galeria
    var cameraUriStr by rememberSaveable { mutableStateOf<String?>(null) }

    val existingGraph = remember(isGraphMode, selectedLine, allMaintenanceItems, currentShift) {
        if (!isGraphMode || selectedLine == null) null
        else {
            val expectedMachineName = "LINHA ${selectedLine.name}"
            allMaintenanceItems.find { item ->
                item.machine == expectedMachineName && 
                item.serviceType == "Gráfico de Produção" &&
                item.shiftId == currentShift.shiftId &&
                (item.workDateMillisFromServer == currentShift.workDate.time ||
                 ShiftManager.getShiftInfo(java.time.Instant.ofEpochMilli(item.date)).workDate.time == currentShift.workDate.time)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val cameraUri = cameraUriStr?.let { Uri.parse(it) }
        if (success && cameraUri != null) {
            if (selectedImageUris.size < 3) {
                selectedImageUris = selectedImageUris + cameraUri
            }
        }
        cameraUriStr = null
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val remainingSlots = 3 - selectedImageUris.size
        if (remainingSlots > 0) {
            selectedImageUris = selectedImageUris + uris.take(remainingSlots)
        }
        if (uris.size > remainingSlots) {
            Toast.makeText(context, "Apenas 3 fotos permitidas.", Toast.LENGTH_SHORT).show()
        }
    }

    fun createImageUri(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .apply { timeZone = TimeZone.getTimeZone("America/Porto_Velho") }.format(Date())
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "JPEG_${timeStamp}.jpg")
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_PICTURES + "/RelatorioManutencao"
                )
            }
        }
        return context.contentResolver.insert(
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )
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
                    onClick = {
                        if (selectedTabIndex != 0) {
                            selectedTabIndex = 0
                            description = ""
                            selectedImageUris = emptyList()
                            selectedMachineId = null
                            serviceType = "Corretiva"
                        }
                    },
                    text = { Text("Serviço") },
                    icon = { Icon(Icons.Default.Build, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        if (selectedTabIndex != 1) {
                            selectedTabIndex = 1
                            description = ""
                            selectedImageUris = emptyList()
                            selectedMachineId = null
                            serviceType = "Gráfico de Produção"
                        }
                    },
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

                // ── Banner de status de acesso ao turno ─────────────────────
                ShiftAccessBanner(
                    isSupervisorOrAdmin = isSupervisorOrAdmin,
                    status = shiftAccess.status,
                    userShiftId = userShiftId,
                    nextShiftStartTime = shiftAccess.nextShiftStartTime,
                    workDateText = dateFormat.format(Date(currentShift.workDate.time))
                )

                // ── Formulário (desabilitado se BLOCKED) ─────────────────────

                // --- Seleção de Linha ---
                ExposedDropdownMenuBox(
                    expanded = isLineDropdownExpanded && isAccessAllowed,
                    onExpandedChange = { if (isAccessAllowed) isLineDropdownExpanded = !isLineDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLine?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = isAccessAllowed,
                        label = { Text(if (isGraphMode) "Linha de Produção (Obrigatório)" else "Linha de Produção") },
                        placeholder = { Text("Selecione a Linha") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLineDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isLineDropdownExpanded,
                        onDismissRequest = { isLineDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Limpar Seleção") },
                            onClick = {
                                selectedLineId = null
                                selectedMachineId = null
                                isLineDropdownExpanded = false
                            }
                        )
                        productionLines.forEach { line ->
                            DropdownMenuItem(
                                text = { Text(line.name) },
                                onClick = {
                                    selectedLineId = line.id
                                    selectedMachineId = null
                                    isLineDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (!isGraphMode) {
                    // --- Seleção de Máquina ---
                    ExposedDropdownMenuBox(
                        expanded = isMachineDropdownExpanded && isAccessAllowed,
                        onExpandedChange = { if (isAccessAllowed) isMachineDropdownExpanded = !isMachineDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMachine?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = isAccessAllowed,
                            label = { Text("Máquina") },
                            placeholder = {
                                Text(if (selectedLine == null) "Selecione máquina sem linha" else "Selecione máquina da linha")
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isMachineDropdownExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
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
                                            selectedMachineId = machine.id
                                            isMachineDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- Tipo de Serviço ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val types = listOf("Corretiva", "Preventiva", "Preditiva", "Informação")
                        types.forEach { type ->
                            val isInfo = type == "Informação"
                            FilterChip(
                                selected = serviceType == type,
                                onClick = { if (isAccessAllowed) serviceType = type },
                                label = { Text(type, maxLines = 1) },
                                enabled = isAccessAllowed,
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
                    enabled = isAccessAllowed,
                    label = { Text(if (isGraphMode) "Observações (Opcional)" else "Descrição") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isGraphMode) 100.dp else 150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    maxLines = 10
                )

                // --- Fotos ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Fotos", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = if (selectedImageUris.size == 3)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text("${selectedImageUris.size}/3", modifier = Modifier.padding(4.dp))
                    }
                }

                if (selectedImageUris.size < 3) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info, contentDescription = null,
                            tint = Color.LightGray, modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Máximo de 3 fotos para o layout do relatório.",
                            style = MaterialTheme.typography.bodySmall, color = Color.LightGray
                        )
                    }
                } else {
                    Text(
                        "Limite de fotos atingido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val uri = createImageUri()
                            if (uri != null) {
                                cameraUriStr = uri.toString()
                                cameraLauncher.launch(uri)
                            } else {
                                Toast.makeText(context, "Erro ao acessar armazenamento.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = selectedImageUris.size < 3 && isAccessAllowed
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Câmera")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        enabled = selectedImageUris.size < 3 && isAccessAllowed
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
                                IconButton(
                                    onClick = { selectedImageUris = selectedImageUris.filter { it != uri } },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close, contentDescription = "Remover",
                                        tint = Color.Red, modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (existingGraph != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF991B1B).copy(alpha = 0.9f))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFCA5A5))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Atenção: Já existe um gráfico salvo para esta linha neste turno. Ao salvar, você criará um registro duplicado.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- Botão Salvar ---
                val isFormValid = if (isGraphMode) {
                    selectedLine != null && selectedImageUris.isNotEmpty()
                } else {
                    selectedMachine != null && description.isNotBlank()
                }

                Button(
                    onClick = {
                        if (isFormValid && isAccessAllowed) {
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

                            val finalDescription =
                                if (isGraphMode && description.isBlank()) "Registro de Gráfico de Produção" else description

                            viewModel.addMaintenanceItem(
                                machine = finalMachineName,
                                serviceType = if (isGraphMode) "Gráfico de Produção" else serviceType,
                                description = finalDescription,
                                photoUris = urisString,
                                overtime = isOvertime
                            )

                            description = ""
                            selectedImageUris = emptyList()
                            if (!isGraphMode) selectedMachineId = null
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isFormValid && isAccessAllowed
                ) {
                    Text(if (isGraphMode) "Salvar Gráfico" else "Salvar Registro")
                }
            }
        }
    }
}

/**
 * Banner de status de acesso ao turno.
 * ACTIVE  → badge verde discreto
 * OVERTIME → card laranja de aviso com info de hora extra
 * BLOCKED  → card vermelho bloqueador com horário do próximo turno
 */
@Composable
private fun ShiftAccessBanner(
    isSupervisorOrAdmin: Boolean,
    status: ShiftAccessStatus,
    userShiftId: Int,
    nextShiftStartTime: String,
    workDateText: String
) {
    when {
        isSupervisorOrAdmin -> {
            // Acesso irrestrito — sem banner
        }

        status == ShiftAccessStatus.ACTIVE -> {
            // Turno Ativo — banner removido pois a indicação já existe no header
        }

        status == ShiftAccessStatus.OVERTIME -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7C2D12).copy(alpha = 0.9f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Hora Extra — T$userShiftId",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "Você está na janela de 1h após o fim do seu turno.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "O registro será marcado como Hora Extra.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        status == ShiftAccessStatus.BLOCKED -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Fora do Seu Turno",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Você só pode adicionar serviços durante o T$userShiftId.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "Seu turno começa às $nextShiftStartTime.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}
