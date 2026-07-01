package app.miscompras.com.ui.screens.supermarkets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.AccessibleTextField
import app.miscompras.com.ui.components.SupermarketLogo
import app.miscompras.com.ui.theme.AccessibilityDefaults
import app.miscompras.com.util.DefaultSupermarket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupermarketScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSupermarketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val defaultSupermarkets by viewModel.defaultSupermarkets.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onLogoUriChange(uri?.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Editar supermercado" else "Añadir supermercado") },
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
                        onClick = { viewModel.saveSupermarket(onNavigateBack) },
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
                AccessibleTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nombre del supermercado",
                    isError = uiState.nameError != null,
                    errorMessage = uiState.nameError
                )
            }

            item {
                Text(
                    text = "Logo personalizado",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.logoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uiState.logoUri)
                                .build(),
                            contentDescription = "Logo personalizado",
                            modifier = Modifier.size(AccessibilityDefaults.LargeIconSize * 2)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(AccessibilityDefaults.LargeIconSize * 2),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    AccessibleButton(
                        text = if (uiState.logoUri != null) "Cambiar logo" else "Seleccionar logo",
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "O selecciona un supermercado predeterminado",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = AccessibilityDefaults.SectionSpacing)
                )
            }

            items(defaultSupermarkets) { defaultSupermarket ->
                DefaultSupermarketItem(
                    defaultSupermarket = defaultSupermarket,
                    isSelected = uiState.name == defaultSupermarket.name && uiState.logoUri == null,
                    onClick = { viewModel.selectDefaultSupermarket(defaultSupermarket.name) }
                )
            }
        }
    }
}

@Composable
fun DefaultSupermarketItem(
    defaultSupermarket: DefaultSupermarket,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccessibilityDefaults.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            SupermarketLogo(
                supermarketName = defaultSupermarket.name,
                customLogoPath = null,
                size = AccessibilityDefaults.LargeIconSize
            )

            Text(
                text = defaultSupermarket.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
