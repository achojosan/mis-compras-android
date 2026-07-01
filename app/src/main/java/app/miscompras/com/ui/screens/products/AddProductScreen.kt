package app.miscompras.com.ui.screens.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir producto") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(AccessibilityDefaults.IconSize)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveProduct(onNavigateBack) },
                        modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar",
                            modifier = Modifier.size(AccessibilityDefaults.IconSize)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(AccessibilityDefaults.ItemSpacing),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing),
            contentPadding = PaddingValues(AccessibilityDefaults.ItemSpacing)
        ) {
            item {
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
                                    .padding(top = AccessibilityDefaults.TextFieldMinHeight + 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                LazyColumn {
                                    items(filtered) { suggestion ->
                                        DropdownMenuItem(
                                            text = { Text(suggestion) },
                                            onClick = {
                                                viewModel.onNameChange(suggestion)
                                                showSuggestions = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    Text(
                        text = "Cantidad",
                        style = MaterialTheme.typography.titleSmall
                    )
                    AccessibleTextField(
                        value = uiState.quantity,
                        onValueChange = viewModel::onQuantityChange,
                        label = "Cantidad",
                        keyboardType = KeyboardType.Decimal,
                        isError = uiState.quantityError != null,
                        errorMessage = uiState.quantityError
                    )
                }
            }

            item {
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
                                }
                            )
                        }
                    }
                }
            }

            item {
                AccessibleTextField(
                    value = uiState.price,
                    onValueChange = viewModel::onPriceChange,
                    label = "Precio (€)",
                    keyboardType = KeyboardType.Decimal,
                    isError = uiState.priceError != null,
                    errorMessage = uiState.priceError
                )
            }

            item {
                AccessibleButton(
                    text = "Guardar producto",
                    onClick = { viewModel.saveProduct(onNavigateBack) },
                    modifier = Modifier.padding(top = AccessibilityDefaults.SectionSpacing)
                )
            }
        }
    }
}
