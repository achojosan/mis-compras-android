package app.miscompras.com.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.miscompras.com.ui.components.AccessibleButton
import app.miscompras.com.ui.theme.AccessibilityDefaults

/** Una pantalla del tour explicativo. */
private data class OnboardingTourPage(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val onboardingTourPages = listOf(
    OnboardingTourPage(
        icon = Icons.Default.Store,
        title = "Tus supermercados",
        description = "Añade tus tiendas favoritas y tenlas siempre a mano."
    ),
    OnboardingTourPage(
        icon = Icons.Default.ShoppingCart,
        title = "Tu lista de la compra",
        description = "Prepara la lista y marca los productos a medida que los compras."
    ),
    OnboardingTourPage(
        icon = Icons.Default.CalendarMonth,
        title = "Tus gastos",
        description = "Registra cada compra y consulta cuánto gastas cada mes."
    ),
    OnboardingTourPage(
        icon = Icons.Default.OpenInNew,
        title = "Tarjeta y cupones",
        description = "Guarda tu tarjeta de cliente para mostrarla en caja, o abre la app " +
            "oficial de la tienda para aplicar tus cupones."
    )
)

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var tourPage by rememberSaveable { mutableStateOf(0) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = AccessibilityDefaults.SectionSpacing,
                    end = AccessibilityDefaults.SectionSpacing,
                    top = AccessibilityDefaults.SectionSpacing,
                    bottom = AccessibilityDefaults.SectionSpacing
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = uiState.step to tourPage,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally { it / 4 }) togetherWith
                        (fadeOut(tween(200)) + slideOutHorizontally { -it / 4 })
                },
                label = "onboardingStep",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { (step, page) ->
                when (step) {
                    OnboardingStep.Welcome -> WelcomeStep(
                        onStart = {
                            tourPage = 0
                            viewModel.onGetStarted()
                        },
                        onSkip = viewModel::onSkipTour
                    )

                    OnboardingStep.Tour -> TourStep(
                        page = page,
                        onNext = {
                            if (page < onboardingTourPages.lastIndex) {
                                tourPage = page + 1
                            } else {
                                viewModel.onTourCompleted()
                            }
                        },
                        onBack = { if (page > 0) tourPage = page - 1 },
                        onSkip = viewModel::onSkipTour
                    )

                    OnboardingStep.Nearby -> NearbyStep(viewModel = viewModel)

                    OnboardingStep.Done -> DoneStep(
                        onFinish = viewModel::completeOnboarding
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        Text(
            text = "Mis Compras",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        Text(
            text = "Organiza tus compras del súper en un solo lugar.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        AccessibleButton(
            text = "Descubre la app",
            onClick = onStart,
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = onSkip) {
            Text("Saltar")
        }
    }
}

@Composable
private fun TourStep(
    page: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    val current = onboardingTourPages[page.coerceIn(0, onboardingTourPages.lastIndex)]
    val isLast = page == onboardingTourPages.lastIndex

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = current.icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        Text(
            text = current.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        Text(
            text = current.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            onboardingTourPages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == page) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= page) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        AccessibleButton(
            text = if (isLast) "Empezar a comprar" else "Siguiente",
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page > 0) {
                TextButton(onClick = onBack) {
                    Text("Atrás")
                }
            } else {
                Box(modifier = Modifier)
            }

            TextButton(onClick = onSkip) {
                Text("Saltar")
            }
        }
    }
}

@Composable
private fun DoneStep(onFinish: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        Text(
            text = "¡Todo listo!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.ItemSpacing))

        Text(
            text = "Puedes añadir tus supermercados cuando quieras desde la pantalla principal.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(AccessibilityDefaults.SectionSpacing))

        AccessibleButton(
            text = "Empezar a comprar",
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
