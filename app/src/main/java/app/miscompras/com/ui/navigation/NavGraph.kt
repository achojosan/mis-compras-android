package app.miscompras.com.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.miscompras.com.ui.screens.history.HistoryDetailScreen
import app.miscompras.com.ui.screens.history.HistoryListScreen
import app.miscompras.com.ui.screens.products.AddProductScreen
import app.miscompras.com.ui.screens.settings.SettingsScreen
import app.miscompras.com.ui.screens.shopping.ShoppingListScreen
import app.miscompras.com.ui.screens.supermarkets.AddSupermarketScreen
import app.miscompras.com.ui.screens.supermarkets.SupermarketDetailScreen
import app.miscompras.com.ui.screens.supermarkets.SupermarketListScreen

object Routes {
    const val SUPERMARKET_LIST = "supermarkets"
    const val SUPERMARKET_DETAIL = "supermarket/{supermarketId}"
    const val ADD_SUPERMARKET = "add_supermarket"
    const val EDIT_SUPERMARKET = "edit_supermarket/{supermarketId}"
    const val ADD_PRODUCT = "add_product/{supermarketId}"
    const val SHOPPING_LIST = "shopping_list"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "history/{historyId}"
    const val SETTINGS = "settings"

    fun supermarketDetail(supermarketId: Long) = "supermarket/$supermarketId"
    fun editSupermarket(supermarketId: Long) = "edit_supermarket/$supermarketId"
    fun addProduct(supermarketId: Long) = "add_product/$supermarketId"
    fun historyDetail(historyId: Long) = "history/$historyId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Supermarkets.route,
        modifier = modifier
    ) {
        composable(Screen.Supermarkets.route) {
            SupermarketListScreen(
                onSupermarketClick = { supermarketId ->
                    navController.navigate(Routes.supermarketDetail(supermarketId))
                },
                onAddSupermarketClick = {
                    navController.navigate(Routes.ADD_SUPERMARKET)
                }
            )
        }

        composable(
            route = Routes.SUPERMARKET_DETAIL,
            arguments = listOf(navArgument("supermarketId") { type = NavType.LongType })
        ) {
            SupermarketDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddProductClick = { supermarketId ->
                    navController.navigate(Routes.addProduct(supermarketId))
                },
                onEditSupermarket = { supermarketId ->
                    navController.navigate(Routes.editSupermarket(supermarketId))
                }
            )
        }

        composable(Routes.ADD_SUPERMARKET) {
            AddSupermarketScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_SUPERMARKET,
            arguments = listOf(navArgument("supermarketId") { type = NavType.LongType })
        ) {
            AddSupermarketScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ADD_PRODUCT,
            arguments = listOf(navArgument("supermarketId") { type = NavType.LongType })
        ) {
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SHOPPING_LIST) {
            ShoppingListScreen()
        }

        composable(Routes.HISTORY) {
            HistoryListScreen(
                onHistoryItemClick = { historyId ->
                    navController.navigate(Routes.historyDetail(historyId))
                }
            )
        }

        composable(
            route = Routes.HISTORY_DETAIL,
            arguments = listOf(navArgument("historyId") { type = NavType.LongType })
        ) {
            HistoryDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
    }
}
