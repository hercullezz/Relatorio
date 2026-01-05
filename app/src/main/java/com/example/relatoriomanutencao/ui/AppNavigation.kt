package com.example.relatoriomanutencao.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.relatoriomanutencao.ui.MachineConfigurationScreen 

object Routes {
    const val HOME = "home"
    const val MACHINE_CONFIGURATION = "machine_configuration" 
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onReportClick = { reportId ->
                    // Redireciona para onde faria sentido, ou implementa detalhes
                },
                onLogout = {
                    // Sem auth por enquanto
                },
                onNavigateToMachineConfig = { 
                    navController.navigate(Routes.MACHINE_CONFIGURATION)
                }
            )
        }
        
        composable(Routes.MACHINE_CONFIGURATION) {
            MachineConfigurationScreen()
        }
    }
}
