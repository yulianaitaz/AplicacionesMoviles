package co.edu.mipuente.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.edu.mipuente.navigation.AppNavHost
import co.edu.mipuente.navigation.Routes
import co.edu.mipuente.ui.components.AppBottomBar
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

@Composable
fun MiPuenteApp(financeViewModel: FinanceViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.SPLASH && currentRoute != Routes.ABOUT && currentRoute != null

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            viewModel = financeViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
