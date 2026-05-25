package com.eous.mentor.features.auth.presentation.intro

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.theme.*

@Composable
fun AuthIntroScreen(
    navController: NavController,
    viewModel: IntroViewModel = remember { IntroViewModel() }
) {
    val state by viewModel.state.collectAsState()

    val signUpInteractionSource = remember { MutableInteractionSource() }
    val isSignUpPressed by signUpInteractionSource.collectIsPressedAsState()
    val isSignUpHovered by signUpInteractionSource.collectIsHoveredAsState()
    val signUpScale by animateFloatAsState(
        targetValue = if (isSignUpHovered || isSignUpPressed) 0.95f else 1.0f,
        label = "signup_scale"
    )

    val loginInteractionSource = remember { MutableInteractionSource() }
    val isLoginPressed by loginInteractionSource.collectIsPressedAsState()
    val isLoginHovered by loginInteractionSource.collectIsHoveredAsState()
    val loginScale by animateFloatAsState(
        targetValue = if (isLoginHovered || isLoginPressed) 0.95f else 1.0f,
        label = "login_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                PhoneFrameShowcase(activeSlide = state.activeSlideIndex)
            }

            // 2. Slide Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                state.slides.forEachIndexed { index, _ ->
                    val width by animateDpAsState(
                        targetValue = if (state.activeSlideIndex == index) 20.dp else 6.dp,
                        animationSpec = tween(300),
                        label = "dot_width"
                    )
                    val color by animateColorAsState(
                        targetValue = if (state.activeSlideIndex == index) EousPurple else Color.White.copy(alpha = 0.2f),
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
                if (state.slides.isNotEmpty()) {
                    Crossfade(
                        targetState = state.slides[state.activeSlideIndex],
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
                    interactionSource = signUpInteractionSource,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp)
                        .graphicsLayer {
                            scaleX = signUpScale
                            scaleY = signUpScale
                        }
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
                    onClick = { navController.navigate("login") },
                    interactionSource = loginInteractionSource,
                    modifier = Modifier.graphicsLayer {
                        scaleX = loginScale
                        scaleY = loginScale
                    }
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
