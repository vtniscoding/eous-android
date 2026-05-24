package com.eous.mentor.features.auth.presentation

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.di.supabase
import com.eous.mentor.core.theme.*
import com.eous.mentor.core.util.friendlyAuthError
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch

@Composable
fun LoginFormScreen(navController: NavController, isTablet: Boolean) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val buttonScale = remember { Animatable(1f) }

    Box(modifier = Modifier.fillMaxSize()) {
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
