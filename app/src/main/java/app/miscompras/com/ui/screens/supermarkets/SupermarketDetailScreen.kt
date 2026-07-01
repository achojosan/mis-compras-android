package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.components.SupermarketLogo
import app.miscompras.com.ui.theme.AccessibilityDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupermarketDetailScreen(
    onNavigateBack: () -> Unit,
    onAddProductClick: (Long) -> Unit,
    viewModel: SupermarketDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is SupermarketDetailUiState.Success -> Text(state.supermarket.name)
                        else -> Text("Supermercado")
                    }
                },
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (val state = uiState) {
                        is SupermarketDetailUiState.Success -> onAddProductClick(state.supermarket.id)
                        else -> {}
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir producto",
                    modifier = Modifier.size(AccessibilityDefaults.IconSize)
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SupermarketDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is SupermarketDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is SupermarketDetailUiState.Success -> {
                SupermarketDetailContent(
                    supermarket = state.supermarket,
                    products = state.products,
                    onTogglePurchased = viewModel::toggleProductPurchased,
                    onDeleteProduct = viewModel::deleteProduct,
                    onAddProduct = { onAddProductClick(state.supermarket.id) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
fun SupermarketDetailContent(
    supermarket: Supermarket,
    products: List<Product>,
    onTogglePurchased: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AccessibilityDefaults.SectionSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SupermarketLogo(
                    supermarketName = supermarket.name,
                    customLogoPath = supermarket.logoPath,
                    size = 72.dp
                )
                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))
                Text(
                    text = supermarket.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing / 2))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${products.size} producto${if (products.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(
                            horizontal = AccessibilityDefaults.ItemSpacing,
                            vertical = AccessibilityDefaults.ItemSpacing / 2
                        )
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ShoppingCart,
                    title = "No hay productos",
                    description = "Añade productos a este supermercado",
                    action = {
                        androidx.compose.material3.Button(onClick = onAddProduct) {
                            Text("Añadir producto")
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = AccessibilityDefaults.ItemSpacing,
                        vertical = AccessibilityDefaults.ItemSpacing
                    ),
                    verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            onTogglePurchased = { onTogglePurchased(product) },
                            onDelete = { onDeleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AccessibilityDefaults.ItemSpacing / 2, vertical = AccessibilityDefaults.ItemSpacing / 4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2)
        ) {
            Checkbox(
                checked = product.isPurchased,
                onCheckedChange = { onTogglePurchased() },
                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (product.isPurchased) TextDecoration.LineThrough else null
                )
                val priceText = product.price?.let { " · $it€" } ?: ""
                Text(
                    text = "${product.quantity} ${product.unit}$priceText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (product.isPurchased) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ${product.name}",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(AccessibilityDefaults.SmallIconSize)
                )
            }
        }
    }
}
