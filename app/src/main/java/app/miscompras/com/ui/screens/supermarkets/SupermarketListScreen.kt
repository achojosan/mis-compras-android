package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.SupermarketLogo
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun SupermarketListScreen(
    onSupermarketClick: (Long) -> Unit,
    onAddSupermarketClick: () -> Unit,
    viewModel: SupermarketListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var supermarketToDelete by remember { mutableStateOf<Supermarket?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSupermarketClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir supermercado",
                    modifier = Modifier.size(AccessibilityDefaults.IconSize)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SupermarketListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SupermarketListUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Default.Store,
                        title = "No hay supermercados",
                        description = "Añade tu primer supermercado para empezar",
                        action = {
                            androidx.compose.material3.Button(onClick = onAddSupermarketClick) {
                                Text("Añadir supermercado")
                            }
                        }
                    )
                }
                is SupermarketListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = AccessibilityDefaults.ItemSpacing,
                            vertical = AccessibilityDefaults.ItemSpacing
                        ),
                        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        items(state.supermarkets, key = { it.id }) { supermarket ->
                            SupermarketItem(
                                supermarket = supermarket,
                                onClick = { onSupermarketClick(supermarket.id) },
                                onDelete = { supermarketToDelete = supermarket }
                            )
                        }
                    }
                }
            }
        }
    }

    supermarketToDelete?.let { supermarket ->
        AlertDialog(
            onDismissRequest = { supermarketToDelete = null },
            title = { Text("Eliminar supermercado") },
            text = { Text("¿Estás seguro de que quieres eliminar ${supermarket.name}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSupermarket(supermarket)
                        supermarketToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { supermarketToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SupermarketItem(
    supermarket: Supermarket,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDelete by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDelete = !showDelete }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccessibilityDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            SupermarketLogo(
                supermarketName = supermarket.name,
                customLogoPath = supermarket.logoPath,
                size = 56.dp
            )

            Text(
                text = supermarket.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            if (showDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar ${supermarket.name}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(AccessibilityDefaults.SmallIconSize)
                    )
                }
            }
        }
    }
}
