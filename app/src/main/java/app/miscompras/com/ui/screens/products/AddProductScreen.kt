package app.miscompras.com.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.AccessibleTextField
import app.miscompras.com.ui.theme.AccessibilityDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val commonUnits by viewModel.commonUnits.collectAsState()

    var showSuggestions by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AccessibilityDefaults.ItemSpacing),
        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing),
        contentPadding = PaddingValues(
            top = AccessibilityDefaults.SectionSpacing,
            bottom = AccessibilityDefaults.SectionSpacing
        )
    ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AccessibilityDefaults.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                    ) {
                        Text(
                            text = "Información del producto",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Box {
                            AccessibleTextField(
                                value = uiState.name,
                                onValueChange = {
                                    viewModel.onNameChange(it)
                                    showSuggestions = it.isNotBlank()
                                },
                                label = "Nombre del producto",
                                isError = uiState.nameError != null,
                                errorMessage = uiState.nameError
                            )

                            if (showSuggestions && uiState.name.isNotBlank()) {
                                val filtered = suggestions.filter {
                                    it.contains(uiState.name, ignoreCase = true) && it != uiState.name
                                }.take(5)

                                if (filtered.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 72.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column {
                                            filtered.forEachIndexed { index, suggestion ->
                                                Text(
                                                    text = suggestion,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.onNameChange(suggestion)
                                                            showSuggestions = false
                                                        }
                                                        .padding(AccessibilityDefaults.CardPadding)
                                                )
                                                if (index < filtered.lastIndex) {
                                                    HorizontalDivider(
                                                        thickness = 0.5.dp,
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AccessibilityDefaults.CardPadding),
                        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                    ) {
                        Text(
                            text = "Cantidad y precio",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
                            verticalAlignment = Alignment.Top
                        ) {
                            AccessibleTextField(
                                value = uiState.quantity,
                                onValueChange = viewModel::onQuantityChange,
                                label = "Cantidad",
                                keyboardType = KeyboardType.Decimal,
                                isError = uiState.quantityError != null,
                                errorMessage = uiState.quantityError,
                                modifier = Modifier.weight(1f)
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                ExposedDropdownMenuBox(
                                    expanded = showUnitMenu,
                                    onExpandedChange = { showUnitMenu = it }
                                ) {
                                    OutlinedTextField(
                                        value = uiState.unit,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Unidad") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitMenu) },
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                            .fillMaxWidth()
                                    )

                                    ExposedDropdownMenu(
                                        expanded = showUnitMenu,
                                        onDismissRequest = { showUnitMenu = false }
                                    ) {
                                        commonUnits.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit) },
                                                onClick = {
                                                    viewModel.onUnitChange(unit)
                                                    showUnitMenu = false
                                                },
                                                trailingIcon = if (uiState.unit == unit) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AccessibleTextField(
                            value = uiState.price,
                            onValueChange = viewModel::onPriceChange,
                            label = "Precio (€) (opcional)",
                            keyboardType = KeyboardType.Decimal,
                            isError = uiState.priceError != null,
                            errorMessage = uiState.priceError
                        )
                    }
                }
            }

            item {
                AccessibleButton(
                    text = "Guardar producto",
                    onClick = { viewModel.saveProduct(onNavigateBack) },
                    modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing)
                )
            }
        }
}
