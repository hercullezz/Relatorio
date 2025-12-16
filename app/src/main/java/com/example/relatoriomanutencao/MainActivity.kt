package com.example.relatoriomanutencao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
<<<<<<< HEAD
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.relatoriomanutencao.ui.CloudScreen
import com.example.relatoriomanutencao.ui.MachineConfigurationScreen
import com.example.relatoriomanutencao.ui.NewMaintenanceScreen
import com.example.relatoriomanutencao.ui.SavedReportsScreen
import com.example.relatoriomanutencao.ui.ServicesListScreen
import com.example.relatoriomanutencao.ui.StockScreen
import com.example.relatoriomanutencao.ui.theme.RelatorioTheme
import com.example.relatoriomanutencao.viewmodel.MainViewModel
=======
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.relatoriomanutencao.ui.AppNavigation
import com.example.relatoriomanutencao.ui.theme.RelatorioManutencaoTheme
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
<<<<<<< HEAD
            RelatorioTheme(darkTheme = true) { // Enforcing Dark Mode as requested
=======
            RelatorioManutencaoTheme {
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
<<<<<<< HEAD
                    MainApp()
=======
                    AppNavigation()
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
                }
            }
        }
    }
<<<<<<< HEAD
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = viewModel()

    Scaffold(
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
                    icon = { Icon(Icons.Default.List, contentDescription = "Serviços") },
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
                    icon = { Icon(Icons.Default.Cloud, contentDescription = "Nuvem") },
                    label = { Text("Nuvem") },
                    selected = currentRoute == "cloud",
                    onClick = {
                        navController.navigate("cloud") {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "new",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("new") { NewMaintenanceScreen(viewModel) }
            composable("services") { ServicesListScreen(viewModel) }
            composable("saved") { SavedReportsScreen() }
            composable("cloud") { CloudScreen() }
            composable("stock") { StockScreen(viewModel) }
            composable("config") { MachineConfigurationScreen(viewModel) }
        }
    }
}
=======
}
>>>>>>> f969efbb0a5b1a468ff385c3f3c78fc47956aa19
