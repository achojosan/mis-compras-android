package app.miscompras.com.ui.screens.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.miscompras.com.nearby.NearbyChain
import app.miscompras.com.nearby.NearbyChains
import app.miscompras.com.nearby.OpeningHours
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.components.ButtonStyle
import app.miscompras.com.ui.components.SupermarketLogoPlate
import app.miscompras.com.ui.theme.AccessibilityDefaults
import java.time.LocalDateTime

/** Paso del onboarding: busca los supermercados cercanos con la ubicación y deja elegir cuáles añadir. */
@Composable
fun NearbyStep(viewModel: OnboardingViewModel) {
    val state by viewModel.nearby.collectAsState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        // En Android 12+ el usuario puede conceder solo la ubicación aproximada: basta para esto.
        if (result.values.any { it }) viewModel.searchNearby() else viewModel.onLocationDenied()
    }
    val ask = {
        launcher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
    }

    when (val s = state) {
        NearbyUi.Intro -> Message(
            title = "Tiendas cerca de ti",
            text = "Si me dejas usar tu ubicación, busco los supermercados que tienes cerca y los añado por ti. " +
                "La ubicación solo se usa ahora, para esta búsqueda.",
            primary = "Buscar tiendas cercanas" to ask,
            secondary = "Ahora no" to viewModel::continueToDone,
            loading = false
        )

        NearbyUi.Loading -> Message(
            title = "Buscando tiendas…",
            text = "Un momento, estoy mirando qué supermercados hay a tu alrededor.",
            loading = true
        )

        NearbyUi.Denied -> Message(
            title = "Sin ubicación",
            text = "No pasa nada: podrás añadir tus supermercados a mano desde la pantalla principal.",
            primary = "Continuar" to viewModel::continueToDone,
            secondary = "Probar de nuevo" to ask,
            loading = false
        )

        NearbyUi.Empty -> Message(
            title = "No he encontrado tiendas",
            text = "No hay ninguna cadena conocida en 10 km a tu alrededor. Puedes añadirlas a mano cuando quieras.",
            primary = "Continuar" to viewModel::continueToDone,
            loading = false
        )

        is NearbyUi.Error -> Message(
            title = "No ha funcionado",
            text = s.message,
            primary = "Reintentar" to viewModel::searchNearby,
            secondary = "Continuar sin tiendas" to viewModel::continueToDone,
            loading = false
        )

        is NearbyUi.Results -> Results(
            chains = s.chains,
            selected = s.selected,
            onToggle = viewModel::toggleChain,
            onAdd = viewModel::addSelectedAndContinue,
            onSkip = viewModel::continueToDone
        )
    }
}

@Composable
private fun Message(
    title: String,
    text: String,
    primary: Pair<String, () -> Unit>? = null,
    secondary: Pair<String, () -> Unit>? = null,
    loading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(48.dp))
            else Icon(
                Icons.Default.LocationOn, null, Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(AccessibilityDefaults.SectionSpacing))
        Text(
            title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(AccessibilityDefaults.ItemSpacing))
        Text(
            text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(AccessibilityDefaults.SectionSpacing))
        primary?.let { AccessibleButton(text = it.first, onClick = it.second) }
        secondary?.let { TextButton(onClick = it.second) { Text(it.first) } }
    }
}

@Composable
private fun Results(
    chains: List<NearbyChain>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onAdd: () -> Unit,
    onSkip: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "He encontrado estas tiendas", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Desmarca las que no quieras. Se muestra la sucursal más cercana de cada cadena.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = AccessibilityDefaults.ItemSpacing)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
        ) {
            items(chains, key = { it.chainName }) { chain ->
                val isSelected = chain.chainName in selected
                val status = OpeningHours.parse(chain.openingHours)?.let {
                    if (it.isOpen(LocalDateTime.now())) "Abierto ahora" else "Cerrado ahora"
                }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onToggle(chain.chainName) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(AccessibilityDefaults.CardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing)
                    ) {
                        SupermarketLogoPlate(
                            supermarketName = chain.chainName, logoPath = null, height = 44.dp,
                            cornerRadius = 14.dp, contentPadding = 6.dp, shadowElevation = 1.dp, showBorder = false
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(chain.chainName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                listOfNotNull("A ${NearbyChains.formatDistance(chain.distanceMeters)}", status).joinToString(" · "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Checkbox(checked = isSelected, onCheckedChange = { onToggle(chain.chainName) })
                    }
                }
            }
        }
        Spacer(Modifier.height(AccessibilityDefaults.ItemSpacing))
        AccessibleButton(
            text = if (selected.isEmpty()) "Continuar sin añadir" else "Añadir ${selected.size} tienda${if (selected.size == 1) "" else "s"}",
            onClick = if (selected.isEmpty()) onSkip else onAdd
        )
        TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Ahora no") }
    }
}
