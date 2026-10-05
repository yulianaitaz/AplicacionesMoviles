package co.edu.mipuente.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.edu.mipuente.ui.screens.AboutScreen
import co.edu.mipuente.ui.screens.AddMovementScreen
import co.edu.mipuente.ui.screens.GoalsScreen
import co.edu.mipuente.ui.screens.HomeScreen
import co.edu.mipuente.ui.screens.MovementsScreen
import co.edu.mipuente.ui.screens.ProfileScreen
import co.edu.mipuente.ui.screens.SplashScreen
import co.edu.mipuente.ui.viewmodel.FinanceViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Routes.ADD) },
                onMovementsClick = { navController.navigate(Routes.MOVEMENTS) },
                onGoalsClick = { navController.navigate(Routes.GOALS) }
            )
        }
        composable(Routes.MOVEMENTS) {
            MovementsScreen(viewModel = viewModel)
        }
        composable(Routes.ADD) {
            AddMovementScreen(
                viewModel = viewModel,
                onSaved = {
                    navController.navigate(Routes.MOVEMENTS) {
                        popUpTo(Routes.ADD) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Routes.GOALS) {
            GoalsScreen(viewModel = viewModel)
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = viewModel,
                onAboutClick = { navController.navigate(Routes.ABOUT) }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
