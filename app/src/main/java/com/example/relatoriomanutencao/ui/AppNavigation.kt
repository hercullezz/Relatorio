package com.example.relatoriomanutencao.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.relatoriomanutencao.ui.MachineConfigurationScreen // Importação adicionada

object Routes {
    // const val AUTH = "auth" // Removido
    const val HOME = "home"
    const val REPORT = "report/{reportId}"
    const val MACHINE_CONFIGURATION = "machine_configuration" // Nova rota adicionada
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Inicia diretamente na HOME
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        /* Tela de Auth Removida
        composable(Routes.AUTH) {
            // ...
        }
        */
        composable(Routes.HOME) {
            HomeScreen(
                onReportClick = { reportId ->
                    navController.navigate("report/$reportId")
                },
                onLogout = {
                    // Como não há auth, logout pode fechar o app ou apenas limpar dados locais se houver
                    // Por enquanto, não faz nada ou volta pro início
                },
                onNavigateToMachineConfig = { // Novo parâmetro para navegação
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
        // Novo composable para a tela de configuração de máquinas
        composable(Routes.MACHINE_CONFIGURATION) {
            MachineConfigurationScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
