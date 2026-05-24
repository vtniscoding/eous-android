package com.eous.mentor.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.theme.*

@Composable
fun LargeScreenRegisterIntroBanner(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
