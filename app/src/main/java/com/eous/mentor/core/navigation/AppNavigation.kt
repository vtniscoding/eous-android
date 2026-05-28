package com.eous.mentor.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.repository.SessionState
import com.eous.mentor.features.auth.intro.AuthIntroScreen
import com.eous.mentor.features.auth.login.LoginFormScreen
import com.eous.mentor.features.auth.register.RegisterFormScreen
import com.eous.mentor.features.dashboard.DashboardViewModel
import com.eous.mentor.features.main.MainScreen

// --- Navigation Host ---
@Composable
fun AuthRouter() {
    val sessionRepository = RepositoryProvider.sessionRepository
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600

    var isInitialized by remember { mutableStateOf(false) }
    var startDest by remember { mutableStateOf("intro") }
    var activeUserId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        sessionRepository.observeSessionStatus().collect { status ->
            if (status != SessionState.INITIALIZING) {
                val currentUid = sessionRepository.getCurrentUserId() ?: ""
                activeUserId = currentUid
                startDest =
                        if (status == SessionState.AUTHENTICATED) "dashboard"
                        else if (isTablet) "login" else "intro"
                isInitialized = true
            }
        }
    }

    val dashboardViewModel =
            remember(activeUserId) {
                if (activeUserId.isNotEmpty()) DashboardViewModel(activeUserId) else null
            }

    val dashboardState = dashboardViewModel?.state?.collectAsState()?.value
    val isAppReady =
            if (startDest == "dashboard") {
                isInitialized && dashboardState != null && !dashboardState.isLoading
            } else {
                isInitialized
            }

    if (!isAppReady) {
        Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A14)),
                contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = EousPurple) }
    } else {

        GlowBackground {
            NavHost(
                    navController = navController,
                    startDestination = startDest,
                    enterTransition = {
                        slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(350, easing = FastOutSlowInEasing)
                        )
                    }
            ) {
                composable("intro") { AuthIntroScreen(navController = navController) }
                composable("login") {
                    LoginFormScreen(navController = navController, isTablet = isTablet)
                }
                composable("register") {
                    RegisterFormScreen(navController = navController, isTablet = isTablet)
                }
                composable("dashboard") {
                    MainScreen(
                            navController = navController,
                            userId = activeUserId,
                            dashboardViewModel = dashboardViewModel!!
                    )
                }
            }
        }
    }
}

// --- Glow Background Helper ---
@Composable
fun GlowBackground(content: @Composable () -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxSize().background(Color(0xFF0A0A14)).drawBehind {
                        // Top-left purple glow
                        drawCircle(
                                brush =
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                Color(0x40, 0x1A, 0x80, 0x55),
                                                                Color.Transparent
                                                        ),
                                                center = Offset(0f, 0f),
                                                radius = 120.dp.toPx()
                                        ),
                                center = Offset(0f, 0f),
                                radius = 120.dp.toPx()
                        )
                        // Bottom-right indigo glow
                        drawCircle(
                                brush =
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                Color(0x2A, 0x2A, 0x88, 0x77),
                                                                Color.Transparent
                                                        ),
                                                center = Offset(size.width, size.height),
                                                radius = 120.dp.toPx()
                                        ),
                                center = Offset(size.width, size.height),
                                radius = 120.dp.toPx()
                        )
                    }
    ) { content() }
}

// --- Floating Mascot Composable ---
@Composable
fun FloatingMascot(modifier: Modifier = Modifier, size: Int = 80) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_float")
    val translateY by
            infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(2000, easing = EaseInOutSine),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "mascot_y"
            )

    Image(
            painter = painterResource(id = R.drawable.ic_eous),
            contentDescription = "Eous Mascot",
            modifier = modifier.size(size.dp).graphicsLayer { translationY = translateY.dp.toPx() }
    )
}
