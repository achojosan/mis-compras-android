package app.miscompras.com

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
 * Barra de navegación flotante, solo con iconos, al estilo de iOS: una cápsula
 * con sombra y borde sutil donde el destino activo se resalta con un fondo
 * tintado del color del tema.
 */
@Composable
private fun FloatingBottomBar(
    items: List<Screen>,
    isSelected: (Screen) -> Boolean,
    onSelect: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = isSelected(screen)
                    val background by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Transparent
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "bottomItemBackground"
                    )
                    val tint by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "bottomItemTint"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onSelect(screen) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(background),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
