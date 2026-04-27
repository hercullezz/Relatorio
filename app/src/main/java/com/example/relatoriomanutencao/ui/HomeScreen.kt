package com.example.relatoriomanutencao.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.TimeZone
import java.util.Calendar
import com.example.relatoriomanutencao.utils.ShiftManager

@Composable
fun HomeScreen(
    viewModel: MainViewModel = viewModel(),
    onReportClick: (String) -> Unit,
    onLogout: () -> Unit,
    onNavigateToMachineConfig: () -> Unit
) {
    val maintenanceItems by viewModel.maintenanceItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(16.dp)) {
        // Cabeçalho simplificado (o header real fica no topo global)
        Text(text = "Histórico de Manutenção", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { androidx.compose.material3.Text("Buscar") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            val itemsFiltered = maintenanceItems.filter {
                it.machine.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(itemsFiltered) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            /* Simplificado: apenas exibe título/descrição */
                            Column {
                                Text(text = item.machine, fontWeight = FontWeight.Bold)
                                Text(text = item.description, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
