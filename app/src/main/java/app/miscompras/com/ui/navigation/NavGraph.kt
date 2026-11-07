package app.miscompras.com.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.miscompras.com.ui.screens.history.HistoryDetailScreen
import app.miscompras.com.ui.screens.history.HistoryListScreen
import app.miscompras.com.ui.screens.history.MonthHistoryScreen
import app.miscompras.com.ui.screens.loyalty.LoyaltyCardScreen
import app.miscompras.com.ui.screens.recognition.ItemDetailScreen
import app.miscompras.com.ui.screens.recognition.KnownItemsScreen
import app.miscompras.com.ui.screens.recognition.RecognizeScreen
import app.miscompras.com.ui.screens.recognition.RegisterItemScreen
import app.miscompras.com.ui.screens.settings.SettingsScreen
import app.miscompras.com.ui.screens.settings.AppearanceScreen
import app.miscompras.com.ui.screens.settings.PurchaseSettingsScreen
import app.miscompras.com.ui.screens.settings.InfoScreen
import app.miscompras.com.ui.screens.shopping.ShoppingListScreen
import app.miscompras.com.ui.screens.supermarkets.SupermarketDetailScreen
import app.miscompras.com.ui.screens.supermarkets.SupermarketListScreen

object Routes {
    const val SUPERMARKET_LIST = "supermarkets"
    const val SUPERMARKET_DETAIL = "supermarket/{supermarketId}"
    const val SHOPPING_LIST = "shopping_list"
    const val LOYALTY_CARD = "supermarket/{supermarketId}/card"
    const val HISTORY = "history"
    const val HISTORY_MONTH = "history/month/{monthKey}"
    const val HISTORY_DETAIL = "history/{historyId}"
    const val RECOGNIZE = "recognize"
    const val KNOWN_ITEMS = "known_items"
    const val ITEM_DETAIL = "item/{itemId}"
    const val ITEM_REGISTER = "item_register?itemId={itemId}"
    const val SETTINGS = "settings"
    const val SETTINGS_APPEARANCE = "settings/appearance"
    const val SETTINGS_PURCHASE = "settings/purchase"
    const val SETTINGS_INFO = "settings/info"

    fun supermarketDetail(supermarketId: Long) = "supermarket/$supermarketId"
    fun loyaltyCard(supermarketId: Long) = "supermarket/$supermarketId/card"
    fun historyDetail(historyId: Long) = "history/$historyId"
    fun itemDetail(itemId: Long) = "item/$itemId"
    fun itemRegister(itemId: Long = -1L) = "item_register?itemId=$itemId"
    fun monthHistory(monthKey: String) = "history/month/$monthKey"
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
                }
            )
        }

        composable(
            route = Routes.SUPERMARKET_DETAIL,
            arguments = listOf(navArgument("supermarketId") { type = NavType.LongType }),
            // miscompras://supermarket/{id}: lo usan los accesos directos de la pantalla de inicio.
            deepLinks = listOf(navDeepLink { uriPattern = "miscompras://supermarket/{supermarketId}" })
        ) {
            SupermarketDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoyaltyCardClick = { navController.navigate(Routes.loyaltyCard(it)) }
            )
        }

        composable(
            route = Routes.LOYALTY_CARD,
            arguments = listOf(navArgument("supermarketId") { type = NavType.LongType })
        ) {
            LoyaltyCardScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SHOPPING_LIST) {
            ShoppingListScreen()
        }

        composable(Routes.HISTORY) {
            HistoryListScreen(
                onMonthClick = { monthKey ->
                    navController.navigate(Routes.monthHistory(monthKey))
                }
            )
        }

        composable(
            route = Routes.HISTORY_MONTH,
            arguments = listOf(navArgument("monthKey") { type = NavType.StringType })
        ) {
            MonthHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
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

        composable(Routes.RECOGNIZE) {
            RecognizeScreen(
                onItemRecognized = { navController.navigate(Routes.itemDetail(it)) },
                onRegisterClick = { navController.navigate(Routes.itemRegister()) },
                onKnownItemsClick = { navController.navigate(Routes.KNOWN_ITEMS) }
            )
        }

        composable(Routes.KNOWN_ITEMS) {
            KnownItemsScreen(
                onNavigateBack = { navController.popBackStack() },
                onItemClick = { navController.navigate(Routes.itemDetail(it)) },
                onRegisterClick = { navController.navigate(Routes.itemRegister()) }
            )
        }

        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) {
            ItemDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(Routes.itemRegister(it)) }
            )
        }

        composable(
            route = Routes.ITEM_REGISTER,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType; defaultValue = -1L })
        ) {
            RegisterItemScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onKnownItemsClick = { navController.navigate(Routes.KNOWN_ITEMS) },
                onAppearanceClick = { navController.navigate(Routes.SETTINGS_APPEARANCE) },
                onPurchaseClick = { navController.navigate(Routes.SETTINGS_PURCHASE) },
                onInfoClick = { navController.navigate(Routes.SETTINGS_INFO) }
            )
        }

        composable(Routes.SETTINGS_APPEARANCE) {
            AppearanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_PURCHASE) {
            PurchaseSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_INFO) {
            InfoScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
