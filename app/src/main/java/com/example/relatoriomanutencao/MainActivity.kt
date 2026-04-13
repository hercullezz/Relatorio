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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.relatoriomanutencao.ui.login.SignUpScreen
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

// Data class para representar um item da barra de navegação
private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AuthGate() {
    val currentUserState = remember { mutableStateOf(ParseUser.getCurrentUser()) }
    val user = currentUserState.value

    if (user == null) {
        // Se não há usuário, mostra a navegação de autenticação (Login/Cadastro).
        val authNavController = rememberNavController()
        NavHost(navController = authNavController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        // Atualiza nosso estado quando o login for bem-sucedido.
                        currentUserState.value = ParseUser.getCurrentUser()
                    },
                    onNavigateToSignUp = { // Ação para navegar para o cadastro
                        authNavController.navigate("signup")
                    }
                )
            }
            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = { // Após cadastro, volta para o login
                        authNavController.navigate("login") { popUpTo("login") { inclusive = true } }
                    },
                    onNavigateToLogin = { // Ação para voltar para o login
                        authNavController.popBackStack()
                    }
                )
            }
        }
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

                // 1. Define a lista de itens de navegação como "fonte da verdade".
                // Usamos `remember(isAdmin)` para recriar a lista apenas se o status de admin mudar.
                val bottomNavItems = remember(isAdmin) {
                    val baseItems = listOf(
                        BottomNavItem(label = "Novo", icon = Icons.Default.Add, route = "new"),
                        BottomNavItem(label = "Serviços", icon = Icons.AutoMirrored.Filled.List, route = "services"),
                        BottomNavItem(label = "Salvos", icon = Icons.Default.Save, route = "saved"),
                        BottomNavItem(label = "Estoque", icon = Icons.Default.Inventory, route = "stock")
                    )
                    if (isAdmin) {
                        baseItems + BottomNavItem(label = "Config", icon = Icons.Default.Settings, route = "config")
                    } else {
                        baseItems
                    }
                }

                // 2. Itera sobre a lista para criar cada item dinamicamente.
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // Lógica de navegação centralizada para evitar repetição.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                        label = { Text(text = item.label) }
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
            composable("new") { NewMaintenanceScreen(viewModel, onBack = { navController.popBackStack() }) }
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
