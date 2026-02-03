package com.example.relatoriomanutencao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.relatoriomanutencao.ui.MachineConfigurationScreen
import com.example.relatoriomanutencao.ui.NewMaintenanceScreen
import com.example.relatoriomanutencao.ui.SavedReportsScreen
import com.example.relatoriomanutencao.ui.ServicesListScreen
import com.example.relatoriomanutencao.ui.StockScreen
import com.example.relatoriomanutencao.ui.login.LoginScreen
import com.example.relatoriomanutencao.ui.theme.RelatorioManutencaoTheme
import com.example.relatoriomanutencao.viewmodel.MainViewModel
import com.parse.ParseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelatorioManutencaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthGate()
                }
            }
        }
    }
}

@Composable
fun AuthGate() {
    // A única fonte da verdade sobre o estado de autenticação.
    val currentUserState = remember { mutableStateOf(ParseUser.getCurrentUser()) }
    val user = currentUserState.value

    if (user == null) {
        // Se não há usuário, mostra a tela de login.
        LoginScreen(
            onLoginSuccess = {
                // Atualiza nosso estado quando o login for bem-sucedido.
                currentUserState.value = ParseUser.getCurrentUser()
            }
        )
    } else {
        // Se há um usuário, mostra a tela principal.
        MainApp(
            user = user, // Passa o usuário como um parâmetro.
            onLogout = {
                ParseUser.logOut()
                // Atualiza nosso estado para nulo após o logout.
                currentUserState.value = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(user: ParseUser, onLogout: () -> Unit) { // Recebe o usuário como parâmetro
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Usa diretamente o objeto de usuário recebido.
    val isAdmin = user.getBoolean("isAdmin")
    val displayName = user.getString("name") ?: user.username ?: "Usuário"


    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirmLogout = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Olá, $displayName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sair")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Novo") },
                    label = { Text("Novo") },
                    selected = currentRoute == "new",
                    onClick = {
                        navController.navigate("new") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Serviços") },
                    label = { Text("Serviços") },
                    selected = currentRoute == "services",
                    onClick = {
                        navController.navigate("services") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Save, contentDescription = "Salvos") },
                    label = { Text("Salvos") },
                    selected = currentRoute == "saved",
                    onClick = {
                        navController.navigate("saved") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Estoque") },
                    label = { Text("Estoque") },
                    selected = currentRoute == "stock",
                    onClick = {
                        navController.navigate("stock") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                if (isAdmin) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Config") },
                        label = { Text("Config") },
                        selected = currentRoute == "config",
                        onClick = {
                            navController.navigate("config") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "new",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("new") { NewMaintenanceScreen(viewModel) }
            composable("services") { ServicesListScreen(viewModel) }
            composable("saved") { SavedReportsScreen() }
            composable("stock") { StockScreen(viewModel) }
            composable("config") {
                if (isAdmin) {
                    MachineConfigurationScreen(viewModel)
                } else {
                    // Se um usuário não-admin tentar acessar, ele é jogado para a tela anterior.
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Saída") },
        text = { Text("Deseja realmente sair do seu usuário?") },
        confirmButton = {
            TextButton(onClick = onConfirmLogout) {
                Text("Sair")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
