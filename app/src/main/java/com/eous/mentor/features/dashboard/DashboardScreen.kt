package com.eous.mentor.features.dashboard

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.core.ui.theme.*

@Composable
fun Dashboard(
    navController: NavController,
    userId: String,
    onMenuClick: () -> Unit,
    viewModel: DashboardViewModel = remember(userId) { DashboardViewModel(userId) }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- Glow Background Circles ---
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .size(400.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x3B, 0x1A, 0x6A, 0x2A),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .size(400.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x1F, 0x2A, 0x6A, 0x2A),
                            Color.Transparent
                        )
                    )
                )
        )

        // --- Main Content Column ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Menu Icon",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Dashboard",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    // Notification Bell with badge
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(8.dp)
                                .background(EousRed, CircleShape)
                        )
                    }

                    // Avatar Icon
                    val initial = if (state.stats.displayName.isNotEmpty()) state.stats.displayName.take(1).uppercase() else "T"
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Brush.linearGradient(listOf(EousPurple, EousIndigo)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Welcome Back Section
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Welcome back,\n${state.stats.displayName}! 👋",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 34.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Here is your learning progress for this week.",
                        color = MutedText,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(EousPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, EousPurple.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🚀 Keep it up!",
                            color = Purple80,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Level Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackground.copy(alpha = 0.5f))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT LEVEL",
                                    color = EousPurple,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Level ${state.stats.level}",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TOTAL XP",
                                    color = MutedText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${state.stats.xp}",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = " / 100",
                                        color = MutedText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = (state.stats.xp / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                        }

                        Text(
                            text = "🔥 ${100 - state.stats.xp} XP to reach Level ${state.stats.level + 1}! Keep asking questions to level up.",
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    }
                }

                // Stats Column
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Streak Card
                    StatItemCard(
                        title = "Study Streak",
                        value = "${state.stats.streak} Days",
                        icon = Icons.Outlined.Star,
                        iconTint = EousPurple
                    )

                    // Total Queries Card
                    StatItemCard(
                        title = "Total Queries",
                        value = "${state.stats.totalQueries}",
                        icon = Icons.Outlined.Face,
                        iconTint = EousIndigo
                    )

                    // Library Items Card
                    StatItemCard(
                        title = "Library Items",
                        value = "${state.stats.libraryItems}",
                        icon = Icons.Outlined.Bookmark,
                        iconTint = EousPink
                    )

                    // Est. Study Time Card
                    StatItemCard(
                        title = "Est. Study Time",
                        value = "${state.stats.studyTime} hrs",
                        icon = Icons.Outlined.AccessTime,
                        iconTint = EousBlue
                    )
                }

                // Subject Focus Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackground.copy(alpha = 0.5f))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EousPurple, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subject Focus",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Math Progress
                        SubjectFocusProgress(
                            subjectName = "Math",
                            pct = state.stats.mathPct,
                            trackColor = EousPurple
                        )

                        // IT / Programming Progress
                        SubjectFocusProgress(
                            subjectName = "IT / Programming",
                            pct = state.stats.itPct,
                            trackColor = EousIndigo
                        )

                        // Science Progress
                        SubjectFocusProgress(
                            subjectName = "Science",
                            pct = state.stats.sciencePct,
                            trackColor = EousPink
                        )
                    }
                }

                // Unlocked Badges Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackground.copy(alpha = 0.5f))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EousPurple, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unlocked Badges",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Badge 1: Streak
                            BadgeItem(
                                emoji = if (state.stats.streak >= 5) "🏅" else "🔥",
                                label = "Streak",
                                active = state.stats.streak >= 5,
                                ringColor = EousOrange
                            )

                            // Badge 2: Library
                            BadgeItem(
                                emoji = if (state.stats.libraryItems >= 5) "🎖️" else "📁",
                                label = "Library",
                                active = state.stats.libraryItems >= 5,
                                ringColor = EousGreen
                            )

                            // Badge 3: Total Queries
                            BadgeItem(
                                emoji = if (state.stats.totalQueries >= 50) "🥇" else "🎯",
                                label = "Questions",
                                active = state.stats.totalQueries >= 50,
                                ringColor = EousBlue
                            )

                            // Badge 4: Quiz Master
                            BadgeItem(
                                emoji = if (state.stats.quizzes.size >= 5) "🏆" else "🎓",
                                label = "Quiz Master",
                                active = state.stats.quizzes.size >= 5,
                                ringColor = EousPurple
                            )
                        }
                    }
                }

                // Recent Quiz Performance Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBackground.copy(alpha = 0.5f))
                        .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(EousPurple, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recent Quiz Performance",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.stats.quizzes.isEmpty()) {
                            // Mock item from design screenshot
                            RecentQuizItem(
                                topic = "Square",
                                date = "5/21/2026",
                                score = 2,
                                total = 3
                            )
                        } else {
                            state.stats.quizzes.take(5).forEach { quiz ->
                                RecentQuizItem(
                                    topic = quiz.topic,
                                    date = quiz.created_at.take(10),
                                    score = quiz.score,
                                    total = quiz.total_questions
                                )
                            }
                        }
                    }
                }

                // Sign Out Button
                Button(
                    onClick = {
                        viewModel.logout(
                            onSuccess = {
                                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onError = {
                                Toast.makeText(context, "Error logging out", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EousRed.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, EousRed.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Log Out", color = EousRed, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
