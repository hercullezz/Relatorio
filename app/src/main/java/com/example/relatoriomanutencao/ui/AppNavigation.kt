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
    const val REPORT = "report/{reportId}"
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
                    navController.navigate("report/$reportId")
                },
                onLogout = {
                    // Sem auth por enquanto
                },
                onNavigateToMachineConfig = { 
                    navController.navigate(Routes.MACHINE_CONFIGURATION)
                }
            )
        }
        composable(
            route = Routes.REPORT,
            arguments = listOf(navArgument("reportId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: return@composable
            ReportScreen(
                reportId = reportId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MACHINE_CONFIGURATION) {
            MachineConfigurationScreen()
        }
    }
}
