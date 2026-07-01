package app.miscompras.com.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
            SupermarketsPlaceholder()
        }
        composable(Screen.ShoppingList.route) {
            ShoppingListPlaceholder()
        }
        composable(Screen.History.route) {
            HistoryPlaceholder()
        }
        composable(Screen.Nearby.route) {
            NearbyPlaceholder()
        }
    }
}

@Composable
fun SupermarketsPlaceholder() {
    androidx.compose.material3.Text("Pantalla de Supermercados")
}

@Composable
fun ShoppingListPlaceholder() {
    androidx.compose.material3.Text("Pantalla de Lista de Compra")
}

@Composable
fun HistoryPlaceholder() {
    androidx.compose.material3.Text("Pantalla de Historial")
}

@Composable
fun NearbyPlaceholder() {
    androidx.compose.material3.Text("Pantalla de Comercios Cercanos")
}
