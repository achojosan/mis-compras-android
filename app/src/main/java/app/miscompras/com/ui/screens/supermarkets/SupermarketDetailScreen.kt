package app.miscompras.com.ui.screens.supermarkets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarkets
import coil.compose.AsyncImage
import coil.request.ImageRequest

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
                }
            )
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
    Box(modifier = modifier.fillMaxSize()) {
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
                contentPadding = PaddingValues(AccessibilityDefaults.ItemSpacing),
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

        FloatingActionButton(
            onClick = onAddProduct,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AccessibilityDefaults.SectionSpacing)
                .size(AccessibilityDefaults.MinTouchTarget),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir producto",
                modifier = Modifier.size(AccessibilityDefaults.IconSize)
            )
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
                val priceText = product.price?.let { " - $it€" } ?: ""
                Text(
                    text = "${product.quantity} ${product.unit}$priceText",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(AccessibilityDefaults.MinTouchTarget)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar ${product.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
