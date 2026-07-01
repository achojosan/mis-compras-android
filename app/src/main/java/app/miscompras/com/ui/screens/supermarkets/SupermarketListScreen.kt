package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarkets
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketListScreen(
    onSupermarketClick: (Long) -> Unit,
    onAddSupermarketClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: SupermarketListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Supermercados") },
                actions = {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            modifier = Modifier.size(AccessibilityDefaults.IconSize)
                        )
                    }
                }
            )
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AccessibilityDefaults.ItemSpacing),
                        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        items(state.supermarkets, key = { it.id }) { supermarket ->
                            SupermarketItem(
                                supermarket = supermarket,
                                onClick = { onSupermarketClick(supermarket.id) },
                                onDelete = { viewModel.deleteSupermarket(supermarket) }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddSupermarketClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AccessibilityDefaults.SectionSpacing)
                    .size(AccessibilityDefaults.MinTouchTarget),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir supermercado",
                    modifier = Modifier.size(AccessibilityDefaults.IconSize)
                )
            }
        }
    }
}

@Composable
fun SupermarketItem(
    supermarket: Supermarket,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultLogoRes = DefaultSupermarkets.getLogoResId(supermarket.name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccessibilityDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            if (supermarket.logoPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(supermarket.logoPath)
                        .build(),
                    contentDescription = "Logo de ${supermarket.name}",
                    modifier = Modifier.size(AccessibilityDefaults.LargeIconSize)
                )
            } else if (defaultLogoRes != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(defaultLogoRes)
                        .build(),
                    contentDescription = "Logo de ${supermarket.name}",
                    modifier = Modifier.size(AccessibilityDefaults.LargeIconSize)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    modifier = Modifier.size(AccessibilityDefaults.LargeIconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = supermarket.name,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ${supermarket.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
