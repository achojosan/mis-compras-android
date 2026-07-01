package app.miscompras.com.ui.screens.nearby

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.domain.model.NearbyPlace
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.EmptyState
import app.miscompras.com.ui.theme.AccessibilityDefaults
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    onAddSupermarketClick: (String, Double?, Double?, String?) -> Unit = { _, _, _, _ -> },
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadNearbyPlaces()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comercios Cercanos") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is NearbyUiState.Idle -> {
                    EmptyState(
                        icon = Icons.Default.LocationOn,
                        title = "Comercios cercanos",
                        description = "Descubre supermercados cerca de tu ubicación",
                        action = {
                            AccessibleButton(
                                text = "Buscar comercios",
                                onClick = { viewModel.checkPermissionAndLoad() }
                            )
                        }
                    )
                }
                is NearbyUiState.PermissionRequired -> {
                    EmptyState(
                        icon = Icons.Default.LocationOn,
                        title = "Permiso de ubicación",
                        description = "Necesitamos permiso de ubicación para encontrar comercios cercanos",
                        action = {
                            AccessibleButton(
                                text = "Conceder permiso",
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            )
                        }
                    )
                }
                is NearbyUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NearbyUiState.Empty -> {
                    EmptyState(
                        icon = Icons.Default.LocationOn,
                        title = "No hay comercios cercanos",
                        description = "No se encontraron supermercados en un radio de 5km"
                    )
                }
                is NearbyUiState.Error -> {
                    EmptyState(
                        icon = Icons.Default.LocationOn,
                        title = "Error",
                        description = state.message,
                        action = {
                            AccessibleButton(
                                text = "Reintentar",
                                onClick = { viewModel.loadNearbyPlaces() }
                            )
                        }
                    )
                }
                is NearbyUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(AccessibilityDefaults.ItemSpacing),
                        verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        items(state.places, key = { it.id }) { place ->
                            NearbyPlaceItem(
                                place = place,
                                onAdd = {
                                    onAddSupermarketClick(
                                        place.name,
                                        place.latitude,
                                        place.longitude,
                                        place.openingHours
                                    )
                                }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { viewModel.loadNearbyPlaces() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AccessibilityDefaults.SectionSpacing)
                    .size(AccessibilityDefaults.MinTouchTarget),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Actualizar ubicación",
                    modifier = Modifier.size(AccessibilityDefaults.IconSize)
                )
            }
        }
    }
}

@Composable
fun NearbyPlaceItem(
    place: NearbyPlace,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceFormat = NumberFormat.getNumberInstance(Locale("es", "ES"))
    val distanceText = if (place.distanceMeters < 1000) {
        "${distanceFormat.format(place.distanceMeters)} m"
    } else {
        "${distanceFormat.format(place.distanceMeters / 1000)} km"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AccessibilityDefaults.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                when (place.isOpenNow) {
                    true -> Text(
                        text = "Abierto",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    false -> Text(
                        text = "Cerrado",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    null -> {}
                }
            }

            if (place.openingHours != null) {
                Text(
                    text = "Horario: ${place.openingHours}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing / 2)
                )
            }

            if (place.phone != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
                    modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(AccessibilityDefaults.SmallIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = place.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (place.website != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing / 2),
                    modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing / 2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(AccessibilityDefaults.SmallIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = place.website,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AccessibleButton(
                text = "Añadir a mis supermercados",
                onClick = onAdd,
                modifier = Modifier.padding(top = AccessibilityDefaults.ItemSpacing)
            )
        }
    }
}
