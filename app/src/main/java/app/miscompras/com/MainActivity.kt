package app.miscompras.com

import androidx.compose.ui.unit.sp
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import app.miscompras.com.ui.theme.LARGE_TEXT_THRESHOLD
import app.miscompras.com.ui.theme.LocalAccessibilityConfig
import app.miscompras.com.ui.theme.isLargeText
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.miscompras.com.data.local.datastore.ThemeMode
import app.miscompras.com.data.local.datastore.UserPreferencesDataStore
import app.miscompras.com.ui.navigation.NavGraph
import app.miscompras.com.update.UpdateHost
import app.miscompras.com.ui.navigation.Screen
import app.miscompras.com.ui.screens.onboarding.OnboardingScreen
import app.miscompras.com.ui.theme.AccessibilityConfig
import app.miscompras.com.ui.theme.MisComprasTheme
import app.miscompras.com.ui.theme.ProvideAccessibilityConfig
import app.miscompras.com.ui.theme.ThemeColor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var dataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val fontScale by dataStore.fontScale.collectAsState(initial = 1.0f)
            val spacingScale by dataStore.spacingScale.collectAsState(initial = 1.0f)
            val highContrast by dataStore.highContrast.collectAsState(initial = false)
            val themeMode by dataStore.themeMode.collectAsState(initial = ThemeMode.System)
            val themeColor by dataStore.themeColor.collectAsState(initial = ThemeColor.Green)
            val onboardingCompleted by dataStore.onboardingCompleted.collectAsState(initial = null)

            val config = AccessibilityConfig(
                fontScale = fontScale,
                spacingScale = spacingScale,
                highContrast = highContrast
            )

            val isDarkTheme = when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MisComprasTheme(
                darkTheme = isDarkTheme,
                highContrast = highContrast,
                themeColor = themeColor
            ) {
                ProvideAccessibilityConfig(config = config) {
                    MainScreen(
                        themeColor = themeColor,
                        onboardingCompleted = onboardingCompleted
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    themeColor: ThemeColor = ThemeColor.Green,
    onboardingCompleted: Boolean? = true
) {
    var showBrandedSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(600)
        showBrandedSplash = false
    }

    val showContent = !showBrandedSplash && onboardingCompleted != null

    Box(modifier = Modifier.fillMaxSize()) {
        if (showContent) {
            if (onboardingCompleted == false) {
                OnboardingScreen()
            } else {
                MainContentRoot(themeColor = themeColor)
            }
        }

        AnimatedVisibility(
            visible = !showContent,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            BrandedSplashScreen()
        }
    }
}

@Composable
fun MainContentRoot(themeColor: ThemeColor = ThemeColor.Green) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    MainContent(
        navController = navController,
        currentDestination = currentDestination,
        themeColor = themeColor
    )
    UpdateHost()
}

@Composable
fun BrandedSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Mis Compras",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun MainContent(
    navController: androidx.navigation.NavHostController,
    currentDestination: androidx.navigation.NavDestination?,
    themeColor: ThemeColor = ThemeColor.Green
) {
    val isBottomNavScreen = Screen.bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isBottomNavScreen) {
                FloatingBottomBar(
                    items = Screen.bottomNavItems,
                    isSelected = { screen ->
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    },
                    onSelect = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Barra de navegación de Material 3 con etiquetas siempre visibles (quien tiene baja
 * visión o dificultades cognitivas no puede depender solo de un icono). Con texto grande
 * la etiqueta se limita a 1,3× para que quepa en una línea y se usa el título corto;
 * la altura de la barra crece con el contenido.
 */
@Composable
private fun FloatingBottomBar(
    items: List<Screen>,
    isSelected: (Screen) -> Boolean,
    onSelect: (Screen) -> Unit
) {
    val largeText = LocalAccessibilityConfig.current.isLargeText
    val density = LocalDensity.current
    val labelDensity = Density(
        density = density.density,
        fontScale = density.fontScale.coerceAtMost(LARGE_TEXT_THRESHOLD)
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                selected = isSelected(screen),
                onClick = { onSelect(screen) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    CompositionLocalProvider(LocalDensity provides labelDensity) {
                        Text(
                            text = if (largeText) screen.shortTitle else screen.title,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 13.sp, letterSpacing = 0.sp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
