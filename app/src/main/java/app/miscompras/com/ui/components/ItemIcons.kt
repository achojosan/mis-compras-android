package app.miscompras.com.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/** Icono con nombre para distinguir un artículo sin tener que leer su nombre. */
data class ItemIcon(val key: String, val label: String, val vector: ImageVector)

object ItemIcons {
    val all: List<ItemIcon> = listOf(
        ItemIcon("pescado", "Pescado", Icons.Default.SetMeal),
        ItemIcon("huevo", "Huevo", Icons.Default.Egg),
        ItemIcon("mascota", "Mascota", Icons.Default.Pets),
        ItemIcon("verdura", "Verdura", Icons.Default.Eco),
        ItemIcon("bebida", "Bebida", Icons.Default.LocalDrink),
        ItemIcon("agua", "Agua", Icons.Default.WaterDrop),
        ItemIcon("pan", "Pan", Icons.Default.BakeryDining),
        ItemIcon("dulce", "Dulce", Icons.Default.Cake),
        ItemIcon("helado", "Helado", Icons.Default.Icecream),
        ItemIcon("comida", "Comida", Icons.Default.LunchDining),
        ItemIcon("preparado", "Preparado", Icons.Default.Fastfood),
        ItemIcon("higiene", "Higiene", Icons.Default.Spa)
    )

    fun byKey(key: String?): ItemIcon? = all.firstOrNull { it.key == key }
}
