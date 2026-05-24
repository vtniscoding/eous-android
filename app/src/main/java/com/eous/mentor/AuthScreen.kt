package com.eous.mentor

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eous.mentor.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Friendly Auth Error Parser ---
private fun friendlyAuthError(e: Throwable): String {
    val msg = (e.localizedMessage ?: e.message ?: "").lowercase()
    return when {
        "invalid_credentials" in msg || "invalid login" in msg ->
            "Incorrect email or password. Please try again."
        "email not confirmed" in msg ->
            "Please verify your email before logging in."
        "user already registered" in msg || "already been registered" in msg ->
            "An account with this email already exists."
        "rate limit" in msg || "too many requests" in msg ->
            "Too many attempts. Please wait a moment and try again."
        "network" in msg || "unable to resolve" in msg || "timeout" in msg ->
            "Network error. Please check your connection."
        "weak password" in msg ->
            "Password is too weak. Use at least 8 characters with mixed case and digits."
        "invalid email" in msg ->
            "Please enter a valid email address."
        else ->
            "Something went wrong. Please try again later."
    }
}

// --- Slide Data Model ---
data class Slide(
    val title: String,
    val description: String,
    val themeColor: Brush
)

// --- Navigation Host ---
@Composable
fun EousAppNavHost() {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val isTablet = screenWidth >= 600

    val isLoggedIn = remember {
        try {
            supabase.auth.currentSessionOrNull() != null
        } catch (e: Throwable) {
            false
        }
    }

    val startDest = remember {
        if (isLoggedIn) "dashboard" else if (isTablet) "login" else "intro"
    }

    NavHost(
        navController = navController,
        startDestination = startDest,
        enterTransition = { fadeIn(animationSpec = tween(250)) },
        exitTransition = { fadeOut(animationSpec = tween(250)) },
        popEnterTransition = { fadeIn(animationSpec = tween(250)) },
        popExitTransition = { fadeOut(animationSpec = tween(250)) }
    ) {
        composable("intro") {
            AuthIntroScreen(navController = navController)
        }
        composable("login") {
            LoginFormScreen(navController = navController, isTablet = isTablet)
        }
        composable("register") {
            RegisterFormScreen(navController = navController, isTablet = isTablet)
        }
        composable("dashboard") {
            val user = remember {
                try {
                    supabase.auth.currentSessionOrNull()?.user
                } catch (e: Throwable) {
                    null
                }
            }
            DashboardScreen(navController, user?.id ?: "")
        }
    }
}

// --- Glow Background Helper ---
@Composable
fun GlowBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A14))
            .drawBehind {
                // Top-left purple glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
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
                    brush = Brush.radialGradient(
                        colors = listOf(
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
    ) {
        content()
    }
}

// --- Floating Mascot Composable ---
@Composable
fun FloatingMascot(
    modifier: Modifier = Modifier,
    size: Int = 80
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_float")
    val translateY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_app_logo),
        contentDescription = "Eous Mascot",
        modifier = modifier
            .size(size.dp)
            .graphicsLayer {
                translationY = translateY.dp.toPx()
            }
    )
}

// ==========================================
// 1. MOBILE ONBOARDING / INTRO SCREEN
// ==========================================
@Composable
fun AuthIntroScreen(navController: NavController) {
    val slides = remember {
        listOf(
            Slide(
                "Your Personal AI Mentor",
                "Works 24/7 with customized, step-by-step explanations tailored just for you.",
                Brush.horizontalGradient(listOf(EousPurple, EousIndigo))
            ),
            Slide(
                "Tailored to Your Level",
                "Whether you are in Middle School, High School, or University, Eous adapts to you.",
                Brush.horizontalGradient(listOf(EousBlue, EousIndigo))
            ),
            Slide(
                "Active Recall Quizzes",
                "Test your skills with interactive flashcards and gamified subject tracking.",
                Brush.horizontalGradient(listOf(EousPink, EousRed))
            )
        )
    }

    var activeSlide by remember { mutableStateOf(0) }

    // Auto-advance slides every 4 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            activeSlide = (activeSlide + 1) % slides.size
        }
    }

    GlowBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Phone Frame Mockup Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PhoneFrameShowcase(activeSlide = activeSlide)
            }

            // 2. Slide Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                slides.forEachIndexed { index, _ ->
                    val width by animateDpAsState(
                        targetValue = if (activeSlide == index) 20.dp else 6.dp,
                        animationSpec = tween(300),
                        label = "dot_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (activeSlide == index) EousPurple else Color.White.copy(alpha = 0.2f),
                        animationSpec = tween(300),
                        label = "dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // 3. Branding & Interactive Copy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Eous",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_app_logo),
                        contentDescription = "Eous logo",
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Dynamic Caption
                Crossfade(
                    targetState = slides[activeSlide],
                    animationSpec = tween(300),
                    label = "slide_caption",
                    modifier = Modifier.height(75.dp)
                ) { slide ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = slide.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = slide.description,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // 4. Action Buttons Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sign Up capsule button
                Button(
                    onClick = { navController.navigate("register") },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Create an Account",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Log In Flat text button
                TextButton(
                    onClick = { navController.navigate("login") }
                ) {
                    Text(
                        "Log In",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- Phone Frame Showcase and Screens ---
@Composable
fun PhoneFrameShowcase(activeSlide: Int) {
    var isHovered by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isHovered) 0f else -4f,
        animationSpec = tween(500, easing = EaseOutQuad),
        label = "rotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1.0f,
        animationSpec = tween(500, easing = EaseOutQuad),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(370.dp)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isHovered = !isHovered
            }
            .clip(RoundedCornerShape(36.dp))
            .border(5.dp, Color(0xFF2C2D30), RoundedCornerShape(36.dp))
            .background(Color(0xFF0C0D0E))
    ) {
        // Speaker Notch
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(76.dp)
                .height(15.dp)
                .background(Color(0xFF2C2D30), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.5.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            )
        }

        // Screens based on activeSlide with nice Slide animations
        Crossfade(
            targetState = activeSlide,
            animationSpec = tween(700),
            label = "phone_screen"
        ) { slideIndex ->
            when (slideIndex) {
                0 -> ChatScreenMock()
                1 -> DashboardScreenMock()
                2 -> QuizScreenMock()
            }
        }
    }
}

@Composable
fun ChatScreenMock() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 22.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        // Fake App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    "Eous AI Mentor",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "ACTIVE",
                    color = EousPurple,
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(EousPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat flow
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // User Message
            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(Color(0xFF6856E6), RoundedCornerShape(12.dp, 12.dp, 4.dp, 12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .widthIn(max = 140.dp)
            ) {
                Text(
                    "Explain photosynthesis simply 🌿",
                    color = Color.White,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }

            // AI Message
            Column(
                modifier = Modifier
                    .align(Alignment.Start)
                    .background(CardBackground, RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp, 12.dp, 12.dp, 4.dp))
                    .padding(8.dp)
                    .widthIn(max = 160.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = EousPurple,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Eous AI Mentor",
                        color = EousPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "It's how plants eat light! ☀️\n\n1. Drink water 💧\n2. Catch sunlight ☀️\n3. Make sugar for energy! ⚡",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    lineHeight = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Fake Input Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .background(Color(0xFF18191B), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ask your mentor anything...",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 8.sp
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(EousIndigo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("➔", color = Color.White, fontSize = 8.sp)
                }
            }
        }
    }
}

@Composable
fun DashboardScreenMock() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 22.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        // Fake App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "My Dashboard",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            EousBlue.copy(alpha = 0.3f),
                            EousIndigo.copy(alpha = 0.3f)
                        )
                    ),
                    RoundedCornerShape(12.dp)
                )
                .border(0.5.dp, EousBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ACTIVE STUDY LEVEL",
                color = Color(0xFF93C5FD),
                fontSize = 7.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "University Student",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .fillMaxHeight()
                        .background(EousBlue, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subject Grid
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SubjectCard("📐", "Calculus", modifier = Modifier.weight(1f))
                SubjectCard("🧬", "Biology", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SubjectCard("⚛️", "Physics", modifier = Modifier.weight(1f))
                SubjectCard("🧠", "Philosophy", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SubjectCard(emoji: String, name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(52.dp)
            .background(Color(0xFF1C1D20), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(emoji, fontSize = 16.sp)
        Text(name, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuizScreenMock() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 22.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
    ) {
        // Fake App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Flashcard Quiz",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quiz Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp)
                .background(CardBackground, RoundedCornerShape(12.dp))
                .border(1.5.dp, EousPink.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "CHEMISTRY QUIZ",
                color = Color(0xFFF472B6),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "What is the chemical formula for water?",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Multiple Choices
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            QuizOption("CO2 (Carbon Dioxide)", isSelected = false, isCorrect = false)
            QuizOption("H2O (Dihydrogen Monoxide)", isSelected = true, isCorrect = true)
            QuizOption("NaCl (Sodium Chloride)", isSelected = false, isCorrect = false)
        }
    }
}

@Composable
fun QuizOption(text: String, isSelected: Boolean, isCorrect: Boolean) {
    val bgGradient = if (isCorrect) {
        Brush.horizontalGradient(listOf(Color(0x1F, 0x22, 0xC5, 0x5E), Color(0x0A, 0x22, 0xC5, 0x5E)))
    } else {
        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.05f)))
    }
    val borderColor = if (isCorrect) {
        Color(0xFF22C55E).copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }
    val textColor = if (isCorrect) {
        Color(0xFF4ADE80)
    } else {
        Color.White.copy(alpha = 0.6f)
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .background(bgGradient, RoundedCornerShape(6.dp))
            .border(0.5.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text,
            color = textColor,
            fontSize = 7.5.sp,
            fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Medium
        )
        if (isCorrect) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF22C55E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.Black, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            )
        }
    }
}

// ==========================================
// 2. LOGIN FORM SCREEN
// ==========================================
@Composable
fun LoginFormScreen(navController: NavController, isTablet: Boolean) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val buttonScale = remember { Animatable(1f) }

    GlowBackground {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Large Screen Left Intro Banner
            if (isTablet) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 40.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(EousPurple.copy(alpha = 0.1f), CircleShape)
                            .border(1.dp, EousPurple.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "🚀 Eous — The #1 AI Study Mentor",
                            color = EousPurple,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                "Your AI Mentor",
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                "Ready for You.",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-1).sp
                                ),
                                modifier = Modifier.drawBehind {
                                    // Custom text gradient shader placeholder using compose brushes
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        FloatingMascot(size = 80)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "A personal study assistant everyone's obsessed with — works 24/7, no setup needed. Master any subject with step-by-step explanations.",
                        color = MutedText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(EousGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("No credit card needed", color = MutedText, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(EousGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel anytime", color = MutedText, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Right/Center Form Card
            Card(
                modifier = Modifier
                    .run { if (isTablet) weight(1f) else this }
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterVertically),
                colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    // Back button on mobile
                    if (!isTablet) {
                        IconButton(
                            onClick = { navController.navigate("intro") },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, BorderColor.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Centered app logo on mobile
                        if (!isTablet) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Eous Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(bottom = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(
                            text = "Welcome Back",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log in to your Eous account",
                            color = MutedText,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Error message banner
                        if (errorMsg != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EousRed.copy(alpha = 0.1f))
                                    .border(1.dp, EousRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = errorMsg!!,
                                    color = EousRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Email Label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Email",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMsg = null
                            },
                            placeholder = { Text("you@example.com", color = MutedText.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                focusedBorderColor = EousPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Title + Forgot link row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Password", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Forgot password?",
                                color = EousPurple,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                    Toast.makeText(context, "Password reset link sent!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMsg = null
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = MutedText
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                focusedBorderColor = EousPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Log In Button
                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = {
                                if (isLoading) return@Button
                                if (email.isEmpty() || password.isEmpty()) {
                                    errorMsg = "Email and password are required."
                                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errorMsg = "Please enter a valid email address."
                                } else {
                                    scope.launch {
                                        isLoading = true
                                        errorMsg = null
                                        try {
                                            supabase.auth.signInWith(Email) {
                                                this.email = email
                                                this.password = password
                                            }
                                            Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("dashboard") {
                                                popUpTo("intro") { inclusive = true }
                                            }
                                        } catch (e: Throwable) {
                                            errorMsg = friendlyAuthError(e)
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .graphicsLayer {
                                    scaleX = buttonScale.value
                                    scaleY = buttonScale.value
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Log In",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch to Sign Up
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Don't have an account? ", color = MutedText, fontSize = 13.sp)
                            Text(
                                "Sign up",
                                color = EousPurple,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    navController.navigate("register") {
                                        popUpTo("intro")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. REGISTER FORM SCREEN
// ==========================================
@Composable
fun RegisterFormScreen(navController: NavController, isTablet: Boolean) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    GlowBackground {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Large Screen Left Intro Banner
            if (isTablet) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 40.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Eous",
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        FloatingMascot(size = 70)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your AI Study Mentor", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Master any subject with step-by-step explanations, personalized quizzes, and instant homework help.",
                        color = MutedText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Features checklist
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        FeatureRow(
                            title = "AI-Powered Solutions",
                            desc = "Get instant help with complex equations, diagrams, and text questions."
                        )
                        FeatureRow(
                            title = "Tailored to Your Level",
                            desc = "Explanations adapt to your education level (Middle School, High School, or University)."
                        )
                        FeatureRow(
                            title = "Progress Tracking",
                            desc = "Monitor your learning journey with detailed analytics and gamified rewards."
                        )
                    }
                }
            }

            // Right/Center Form Card
            Card(
                modifier = Modifier
                    .run { if (isTablet) weight(1f) else this }
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterVertically),
                colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, BorderColor.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    // Back button on mobile
                    if (!isTablet) {
                        IconButton(
                            onClick = { navController.navigate("intro") },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                                .border(1.dp, BorderColor.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Centered app logo on mobile
                        if (!isTablet) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "Eous Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(bottom = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(
                            text = "Create an Account",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start your journey with Eous AI Study Mentor",
                            color = MutedText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Error message banner
                        if (errorMsg != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EousRed.copy(alpha = 0.1f))
                                    .border(1.dp, EousRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = errorMsg!!,
                                    color = EousRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Email Label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Email",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Email Input
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMsg = null
                            },
                            placeholder = { Text("you@example.com", color = MutedText.copy(alpha = 0.5f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                focusedBorderColor = EousPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Password",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Password Input
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMsg = null
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = MutedText
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                focusedBorderColor = EousPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        // Password Strength Meter (Always Visible)
                        PasswordStrengthMeter(password = password)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Password Label
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Confirm Password",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Confirm Password Input
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMsg = null
                            },
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = MutedText
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                focusedBorderColor = EousPurple,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                            Button(
                            onClick = {
                                if (isLoading) return@Button
                                val strength = getPasswordStrength(password)
                                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                                    errorMsg = "All fields are required."
                                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    errorMsg = "Please enter a valid email address."
                                } else if (password.length < 8) {
                                    errorMsg = "Password must be at least 8 characters long."
                                } else if (password != confirmPassword) {
                                    errorMsg = "Passwords do not match."
                                } else if (strength < 2) {
                                    errorMsg = "Password is too weak. Please use digits or mix cases."
                                } else {
                                    scope.launch {
                                        isLoading = true
                                        errorMsg = null
                                        try {
                                            supabase.auth.signUpWith(Email) {
                                                this.email = email
                                                this.password = password
                                            }
                                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("dashboard") {
                                                popUpTo("intro") { inclusive = true }
                                            }
                                        } catch (e: Throwable) {
                                            errorMsg = friendlyAuthError(e)
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        "Sign Up",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Switch to Login
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Already have an account? ", color = MutedText, fontSize = 13.sp)
                            Text(
                                "Log in",
                                color = EousPurple,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    navController.navigate("login") {
                                        popUpTo("intro")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Check",
            tint = EousGreen,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                color = MutedText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ==========================================
// 4. PASSWORD STRENGTH METER COMPONENT
// ==========================================
fun getPasswordStrength(password: String): Int {
    if (password.isEmpty()) return -1
    if (password.length < 8) return 0 // Very Weak
    
    var score = 1 // Weak
    val hasDigit = password.any { it.isDigit() }
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    
    if (hasDigit && (hasUpper || hasLower)) {
        score = 2 // Fair
    }
    if (hasDigit && hasUpper && hasLower) {
        score = 3 // Good
    }
    if (hasDigit && hasUpper && hasLower && hasSpecial && password.length >= 10) {
        score = 4 // Strong
    }
    return score
}

@Composable
fun PasswordStrengthMeter(password: String) {
    val score = remember(password) { getPasswordStrength(password) }
    
    val scoreText = when (score) {
        0 -> "Very Weak"
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Strong"
        else -> "Enter password"
    }
    
    val scoreColor = when (score) {
        0, 1 -> EousRed
        2 -> EousOrange
        3 -> EousYellow
        4 -> EousGreen
        else -> Color.Gray.copy(alpha = 0.2f)
    }
    
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Password Strength",
                color = MutedText,
                fontSize = 12.sp
            )
            Text(
                scoreText,
                color = scoreColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0..3) {
                val active = score >= 0 && i <= score
                val color = if (active) scoreColor else Color.Gray.copy(alpha = 0.2f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}
