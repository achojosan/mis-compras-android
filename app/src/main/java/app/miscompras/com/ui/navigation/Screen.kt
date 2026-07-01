package app.miscompras.com.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Supermarkets : Screen(
        route = "supermarkets",
        title = "Tiendas",
        icon = Icons.Default.Store
    )

    data object ShoppingList : Screen(
        route = "shopping_list",
        title = "Lista",
        icon = Icons.Default.ShoppingCart
    )

    data object History : Screen(
        route = "history",
        title = "Gastos",
        icon = Icons.Default.History
    )

    data object Settings : Screen(
        route = "settings",
        title = "Ajustes",
        icon = Icons.Default.Settings
    )

    companion object {
        val bottomNavItems = listOf(Supermarkets, ShoppingList, History, Settings)
    }
}
