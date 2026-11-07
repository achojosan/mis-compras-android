package app.miscompras.com.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.miscompras.com.domain.model.Product
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.toDoubleOrNullLocalized

val CommonProductUnits = listOf("unidades", "kg", "g", "L", "ml", "docena", "pack")

/**
 * Formulario de producto presentado como hoja inferior. Comparte estructura y
 * estilo con el resto de formularios de la app ([FormSheetScaffold]).
 */
@Composable
fun ProductFormSheet(
    title: String,
    subtitle: String?,
    name: String,
    onNameChange: (String) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    nameError: String?,
    quantityError: String?,
    priceError: String?,
    suggestions: List<String>,
    saveLabel: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    FormSheetScaffold(
        title = title,
        subtitle = subtitle,
        onDismiss = onDismiss
    ) {
        FormTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Nombre del producto",
            isError = nameError != null,
            errorMessage = nameError
        )

        val filteredSuggestions = if (name.isBlank()) {
            emptyList()
        } else {
            suggestions
                .filter { it.contains(name, ignoreCase = true) && !it.equals(name, ignoreCase = true) }
                .take(4)
        }
        if (filteredSuggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column {
                    filteredSuggestions.forEachIndexed { index, suggestion ->
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNameChange(suggestion) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                        if (index < filteredSuggestions.lastIndex) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
            verticalAlignment = Alignment.Top
        ) {
            FormTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                label = "Cantidad",
                keyboardType = KeyboardType.Decimal,
                isError = quantityError != null,
                errorMessage = quantityError,
                modifier = Modifier.weight(1f)
            )
            UnitDropdownField(
                unit = unit,
                onUnitChange = onUnitChange,
                units = CommonProductUnits,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        FormTextField(
            value = price,
            onValueChange = onPriceChange,
            label = "Precio (€) (opcional)",
            keyboardType = KeyboardType.Decimal,
            isError = priceError != null,
            errorMessage = priceError
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing + 8.dp))

        PrimarySheetButton(text = saveLabel, onClick = onSave)
    }
}

/**
 * Hoja de alta de producto. Valida los datos y devuelve los valores ya convertidos.
 */
@Composable
fun AddProductSheet(
    supermarketName: String,
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, quantity: Double, unit: String, price: Double?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf(CommonProductUnits.first()) }
    var price by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    ProductFormSheet(
        title = "Nuevo producto",
        subtitle = "Se añadirá a $supermarketName",
        name = name,
        onNameChange = {
            name = it
            nameError = null
        },
        quantity = quantity,
        onQuantityChange = {
            quantity = it
            quantityError = null
        },
        unit = unit,
        onUnitChange = { unit = it },
        price = price,
        onPriceChange = {
            price = it
            priceError = null
        },
        nameError = nameError,
        quantityError = quantityError,
        priceError = priceError,
        suggestions = suggestions,
        saveLabel = "Añadir producto",
        onSave = {
            val parsedQuantity = quantity.toDoubleOrNullLocalized()?.takeIf { it > 0 }
            val parsedPrice = price.toDoubleOrNullLocalized()?.takeIf { it >= 0 }

            nameError = if (name.isBlank()) "El nombre es obligatorio" else null
            quantityError = if (parsedQuantity == null) "Introduce una cantidad válida" else null
            priceError = if (price.isNotBlank() && parsedPrice == null) {
                "Introduce un precio válido"
            } else {
                null
            }

            if (name.isNotBlank() && parsedQuantity != null && priceError == null) {
                onSave(
                    name.trim(),
                    parsedQuantity,
                    unit,
                    if (price.isBlank()) null else parsedPrice
                )
            }
        },
        onDismiss = onDismiss
    )
}

/**
 * Hoja de edición de producto, precargada con los datos actuales.
 */
@Composable
fun EditProductSheet(
    product: Product,
    onSave: (Product) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember {
        mutableStateOf(product.quantity.toBigDecimal().stripTrailingZeros().toPlainString())
    }
    var unit by remember { mutableStateOf(product.unit) }
    var price by remember {
        mutableStateOf(
            product.price?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: ""
        )
    }
    var nameError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    ProductFormSheet(
        title = "Editar producto",
        subtitle = null,
        name = name,
        onNameChange = {
            name = it
            nameError = null
        },
        quantity = quantity,
        onQuantityChange = {
            quantity = it
            quantityError = null
        },
        unit = unit,
        onUnitChange = { unit = it },
        price = price,
        onPriceChange = {
            price = it
            priceError = null
        },
        nameError = nameError,
        quantityError = quantityError,
        priceError = priceError,
        suggestions = emptyList(),
        saveLabel = "Guardar cambios",
        onSave = {
            val parsedQuantity = quantity.toDoubleOrNullLocalized()?.takeIf { it > 0 }
            val parsedPrice = price.toDoubleOrNullLocalized()?.takeIf { it >= 0 }

            nameError = if (name.isBlank()) "El nombre es obligatorio" else null
            quantityError = if (parsedQuantity == null) "Introduce una cantidad válida" else null
            priceError = if (price.isNotBlank() && parsedPrice == null) {
                "Introduce un precio válido"
            } else {
                null
            }

            if (name.isNotBlank() && parsedQuantity != null && priceError == null) {
                onSave(
                    product.copy(
                        name = name.trim(),
                        quantity = parsedQuantity,
                        unit = unit.ifBlank { product.unit },
                        price = if (price.isBlank()) null else parsedPrice
                    )
                )
            }
        },
        onDismiss = onDismiss
    )
}
