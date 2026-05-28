package com.eous.mentor.features.auth.intro

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.eous.mentor.R
import com.eous.mentor.core.ui.theme.*

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
            .padding(top = 22.dp, start = 12.dp, end = 12.dp, bottom = 22.dp)
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
                painter = painterResource(id = R.drawable.ic_eous),
                contentDescription = "Logo",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Eous AI Mentor",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
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
                painter = painterResource(id = R.drawable.ic_eous),
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
                painter = painterResource(id = R.drawable.ic_eous),
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
