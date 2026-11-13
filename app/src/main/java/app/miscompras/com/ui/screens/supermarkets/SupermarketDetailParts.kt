package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.nearby.OpeningHours
import app.miscompras.com.ui.components.CheckCircle
import app.miscompras.com.ui.components.DashedDivider
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.components.SwipeToDelete
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.ui.theme.AppShapes
import app.miscompras.com.ui.theme.LocalAccessibilityConfig
import app.miscompras.com.ui.theme.TabularNumbers
import app.miscompras.com.ui.theme.TicketEdgeShape
import app.miscompras.com.ui.theme.isLargeText
import app.miscompras.com.ui.theme.pass
import app.miscompras.com.ui.theme.resolvePass
import app.miscompras.com.util.formatEuros
import app.miscompras.com.util.quantityLabel
import java.time.LocalDateTime

/**
 * Cabecera de la ficha: la tienda es su pase. Logo, nombre, si está abierta, cuántos productos quedan
 * y «Cómo llegar» como acción principal. Deja 56 dp libres arriba para los botones flotantes.
 * Con texto grande el logo, el nombre y la cifra se apilan; nada se encoge.
 */
@Composable
internal fun StoreHero(
    supermarket: Supermarket,
    pendingCount: Int,
    purchasedCount: Int,
    total: Double,
    showCounts: Boolean,
    onDirections: () -> Unit
) {
    val resolved = resolvePass(supermarket.pass)
    val content = resolved.content
    val secondary = content.copy(alpha = 0.88f)
    val largeText = LocalAccessibilityConfig.current.isLargeText
    val raw = supermarket.openingHours
    val status = remember(raw) { OpeningHours.parse(raw)?.statusText(LocalDateTime.now()) }
    val itemCount = pendingCount + purchasedCount

    Surface(
        shape = AppShapes.pass,
        color = resolved.container,
        contentColor = content,
        border = if (resolved.outlined) BorderStroke(2.dp, MaterialTheme.colorScheme.outline) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(56.dp))

            val identity: @Composable () -> Unit = {
                Text(supermarket.name, style = MaterialTheme.typography.headlineMedium)
                if (status != null) {
                    Text(status, style = MaterialTheme.typography.bodyLarge, color = secondary)
                } else if (!raw.isNullOrBlank()) {
                    // Formato que no sé interpretar con seguridad: se muestra tal cual.
                    Text("Horario: $raw", style = MaterialTheme.typography.bodyMedium, color = secondary)
                }
            }
            if (largeText) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroLogo(supermarket)
                    identity()
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HeroLogo(supermarket)
                    Column(modifier = Modifier.weight(1f)) { identity() }
                }
            }

            if (showCounts) {
                when {
                    itemCount == 0 -> Text("Sin productos todavía", style = MaterialTheme.typography.bodyLarge, color = secondary)
                    pendingCount == 0 -> Text("Todo comprado", style = MaterialTheme.typography.headlineMedium)
                    else -> {
                        val figure: @Composable () -> Unit = {
                            Text(
                                text = pendingCount.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = TabularNumbers)
                            )
                        }
                        val caption: @Composable () -> Unit = {
                            Text(
                                text = if (pendingCount == 1) "pendiente" else "pendientes",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (total > 0) {
                                Text("${formatEuros(total)} estimados", style = MaterialTheme.typography.bodyMedium, color = secondary)
                            }
                        }
                        if (largeText) {
                            Column { figure(); caption() }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                figure()
                                Column { caption() }
                            }
                        }
                    }
                }
                if (itemCount > 0) {
                    LinearProgressIndicator(
                        progress = { purchasedCount.toFloat() / itemCount },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = content,
                        trackColor = content.copy(alpha = 0.25f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }
            }

            // Botón claro sobre el color del pase: el contraste sale del propio par del pase.
            Button(
                onClick = onDirections,
                shape = AppShapes.button,
                colors = ButtonDefaults.buttonColors(containerColor = content, contentColor = resolved.container),
                modifier = Modifier.fillMaxWidth().heightIn(min = AccessibilityDefaults.MinTouchTarget)
            ) {
                Icon(Icons.Default.Directions, null, Modifier.size(AccessibilityDefaults.SmallIconSize))
                Spacer(Modifier.width(8.dp))
                Text("Cómo llegar", style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun HeroLogo(supermarket: Supermarket) {
    SupermarketLogoPlate(
        supermarketName = supermarket.name,
        logoPath = supermarket.logoPath,
        height = 60.dp,
        maxWidth = 150.dp,
        adaptWidth = true,
        cornerRadius = 16.dp,
        contentPadding = 8.dp,
        shadowElevation = 0.dp,
        showBorder = false,
        containerColor = Color.White
    )
}

internal data class DetailAction(
    val icon: ImageVector,
    val label: String,
    val description: String = label,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/**
 * Acciones secundarias como mosaicos con icono y texto siempre visibles. Cuatro por fila con texto
 * normal y dos con texto grande, para que cada etiqueta quepa entera.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ActionTiles(actions: List<DetailAction>) {
    if (actions.isEmpty()) return
    val max = if (LocalAccessibilityConfig.current.isLargeText) 2 else 4
    val rows = (actions.size + max - 1) / max
    val perRow = (actions.size + rows - 1) / rows // reparto parejo: 5 → 3 + 2, no 4 + 1
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        maxItemsInEachRow = perRow,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            Surface(
                onClick = action.onClick,
                enabled = action.enabled,
                shape = AppShapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, if (action.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxRowHeight()
                    .defaultMinSize(minHeight = 76.dp)
                    .semantics { contentDescription = action.description }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = null,
                        tint = if (action.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun ProductSortOrder.shortLabel(): String = when (this) {
    ProductSortOrder.NameAsc -> "A–Z"
    ProductSortOrder.NameDesc -> "Z–A"
    ProductSortOrder.PriceAsc -> "Precio ↑"
    ProductSortOrder.PriceDesc -> "Precio ↓"
    ProductSortOrder.Status -> "Pendientes primero"
}

private fun ProductSortOrder.longLabel(): String = when (this) {
    ProductSortOrder.NameAsc -> "Nombre, de la A a la Z"
    ProductSortOrder.NameDesc -> "Nombre, de la Z a la A"
    ProductSortOrder.PriceAsc -> "Precio, de menor a mayor"
    ProductSortOrder.PriceDesc -> "Precio, de mayor a menor"
    ProductSortOrder.Status -> "Pendientes primero"
}

/** Un solo selector en lugar de cinco chips: menos ruido y una única diana grande. */
@Composable
private fun SortSelector(sortOrder: ProductSortOrder, onChange: (ProductSortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            onClick = { expanded = true },
            shape = AppShapes.button,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ordenar: ${sortOrder.shortLabel()}", style = MaterialTheme.typography.labelLarge)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ProductSortOrder.entries.forEach { order ->
                DropdownMenuItem(
                    text = { Text(order.longLabel(), style = MaterialTheme.typography.bodyLarge) },
                    onClick = { onChange(order); expanded = false },
                    modifier = Modifier.heightIn(min = AccessibilityDefaults.MinTouchTarget)
                )
            }
        }
    }
}

/**
 * Los productos de la tienda como ticket: igual que en la pestaña Lista. Pulsar el nombre marca o
 * desmarca; el lápiz edita; arrastrar o la acción de accesibilidad elimina.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProductsTicket(
    products: List<Product>,
    showCounts: Boolean,
    sortOrder: ProductSortOrder,
    onSortOrderChange: (ProductSortOrder) -> Unit,
    onToggle: (Product) -> Unit,
    onEdit: (Product) -> Unit,
    onDelete: (Product) -> Unit
) {
    val total = products.sumOf { (it.price ?: 0.0) * it.quantity }
    Surface(
        shape = TicketEdgeShape(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Productos", style = MaterialTheme.typography.headlineSmall)
                if (showCounts) {
                    Text(
                        text = "${products.size} en total",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            SortSelector(sortOrder, onSortOrderChange)

            products.forEach { product ->
                DashedDivider()
                ProductRow(product, { onToggle(product) }, { onEdit(product) }, { onDelete(product) })
            }

            if (total > 0) {
                DashedDivider()
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = formatEuros(total),
                        style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = TabularNumbers)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: Product, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val dim = MaterialTheme.colorScheme.onSurfaceVariant
    val details = buildString {
        append(quantityLabel(product.quantity, product.unit))
        product.price?.let { append(" · ").append(formatEuros(it * product.quantity)) }
    }
    SwipeToDelete(
        onDelete = onDelete,
        deleteLabel = "Eliminar ${product.name}",
        shape = RoundedCornerShape(12.dp)
    ) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = AccessibilityDefaults.MinTouchTarget)
                        .toggleable(value = product.isPurchased, role = Role.Checkbox, onValueChange = { onToggle() })
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                ) {
                    CheckCircle(checked = product.isPurchased)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null,
                            color = if (product.isPurchased) dim else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = TabularNumbers),
                            color = dim
                        )
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar ${product.name}", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
