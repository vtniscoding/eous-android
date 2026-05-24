package com.eous.mentor.features.auth.presentation

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
