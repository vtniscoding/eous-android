package com.eous.mentor

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.eous.mentor.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DashboardStats(
    val displayName: String,
    val totalQueries: Int,
    val libraryItems: Int,
    val streak: Int,
    val studyTime: String,
    val level: Int,
    val xp: Int,
    val mathPct: Int,
    val itPct: Int,
    val sciencePct: Int,
    val quizzes: List<Quiz> = emptyList()
)

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    val display_name: String? = null,
    val onboarding_completed: Boolean = false
)

@Serializable
data class Message(
    val id: String? = null,
    val role: String,
    val subject: String? = null,
    val created_at: String? = null
)

@Serializable
data class Bookmark(
    val id: String? = null,
    val folder: String? = null
)

@Serializable
data class Quiz(
    val id: String,
    val topic: String,
    val score: Int,
    val total_questions: Int,
    val created_at: String
)

@Composable
fun DashboardScreen(navController: NavController, userId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Dashboard Statistics State
    var stats by remember {
        mutableStateOf(
            DashboardStats(
                displayName = "Student",
                totalQueries = 6,
                libraryItems = 0,
                streak = 0,
                studyTime = "0.9",
                level = 1,
                xp = 60,
                mathPct = 0,
                itPct = 0,
                sciencePct = 100,
                quizzes = emptyList()
            )
        )
    }

    var isLoading by remember { mutableStateOf(true) }

    // Fetch stats on load
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                try {
                    val fetched = fetchDashboardStatsFromSupabase(userId)
                    stats = fetched
                } catch (e: Exception) {
                    e.printStackTrace()
                    // If backend is empty or table doesn't exist, we fall back to mockup stats
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

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
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Menu Icon",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
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
                    val initial = if (stats.displayName.isNotEmpty()) stats.displayName.take(1).uppercase() else "T"
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
                            text = "Welcome Back,\n${stats.displayName}! 👋",
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
                                    text = "Level ${stats.level}",
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
                                        text = "${stats.xp}",
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
                                    .fillMaxWidth(fraction = (stats.xp / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                        }

                        Text(
                            text = "🔥 ${100 - stats.xp} XP to reach Level ${stats.level + 1}! Keep asking questions to level up.",
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
                        value = "${stats.streak} Days",
                        icon = Icons.Outlined.Star,
                        iconTint = EousPurple
                    )

                    // Total Queries Card
                    StatItemCard(
                        title = "Total Queries",
                        value = "${stats.totalQueries}",
                        icon = Icons.Outlined.Face,
                        iconTint = EousIndigo
                    )

                    // Library Items Card
                    StatItemCard(
                        title = "Library Items",
                        value = "${stats.libraryItems}",
                        icon = Icons.Outlined.Bookmark,
                        iconTint = EousPink
                    )

                    // Est. Study Time Card
                    StatItemCard(
                        title = "Est. Study Time",
                        value = "${stats.studyTime} hrs",
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
                            pct = stats.mathPct,
                            trackColor = EousPurple
                        )

                        // IT / Programming Progress
                        SubjectFocusProgress(
                            subjectName = "IT / Programming",
                            pct = stats.itPct,
                            trackColor = EousIndigo
                        )

                        // Science Progress
                        SubjectFocusProgress(
                            subjectName = "Science",
                            pct = stats.sciencePct,
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
                                emoji = if (stats.streak >= 5) "🏅" else "🔥",
                                label = "Streak",
                                active = stats.streak >= 5,
                                ringColor = EousOrange
                            )

                            // Badge 2: Library
                            BadgeItem(
                                emoji = if (stats.libraryItems >= 5) "🎖️" else "📁",
                                label = "Library",
                                active = stats.libraryItems >= 5,
                                ringColor = EousGreen
                            )

                            // Badge 3: Total Queries
                            BadgeItem(
                                emoji = if (stats.totalQueries >= 50) "🥇" else "🎯",
                                label = "Questions",
                                active = stats.totalQueries >= 50,
                                ringColor = EousBlue
                            )

                            // Badge 4: Quiz Master
                            BadgeItem(
                                emoji = if (stats.quizzes.size >= 5) "🏆" else "🎓",
                                label = "Quiz Master",
                                active = stats.quizzes.size >= 5,
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

                        if (stats.quizzes.isEmpty()) {
                            // Mock item from design screenshot
                            RecentQuizItem(
                                topic = "Square",
                                date = "5/21/2026",
                                score = 2,
                                total = 3
                            )
                        } else {
                            stats.quizzes.take(5).forEach { quiz ->
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
                        scope.launch {
                            try {
                                supabase.auth.signOut()
                                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error logging out", Toast.LENGTH_SHORT).show()
                            }
                        }
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

@Composable
fun StatItemCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground.copy(alpha = 0.5f))
            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = MutedText,
                    fontSize = 13.sp
                )
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun SubjectFocusProgress(subjectName: String, pct: Int, trackColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = subjectName,
                color = Color.White,
                fontSize = 14.sp
            )
            Text(
                text = "$pct%",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (pct / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(trackColor, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
fun BadgeItem(emoji: String, label: String, active: Boolean, ringColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    2.dp,
                    if (active) ringColor else Color.Gray.copy(alpha = 0.3f),
                    CircleShape
                )
                .padding(4.dp)
                .background(
                    if (active) ringColor.copy(alpha = 0.1f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Text(
            text = label,
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RecentQuizItem(topic: String, date: String, score: Int, total: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, BorderColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = topic,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "$score/$total",
                    color = EousPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(6.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = if (total > 0) score.toFloat() / total else 0f)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(listOf(EousPurple, EousIndigo)),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

suspend fun fetchDashboardStatsFromSupabase(userId: String): DashboardStats {
    // 1. Fetch profile name
    val profile = try {
        supabase.from("profiles")
            .select {
                filter {
                    eq("id", userId)
                }
            }
            .decodeSingleOrNull<Profile>()
    } catch (e: Exception) {
        null
    }

    // 2. Fetch queries count and subject focus
    val messages = try {
        supabase.from("messages")
            .select {
                filter {
                    eq("role", "user")
                }
            }
            .decodeList<Message>()
    } catch (e: Exception) {
        emptyList()
    }

    // 3. Fetch bookmarks count
    val bookmarks = try {
        supabase.from("bookmarks")
            .select()
            .decodeList<Bookmark>()
    } catch (e: Exception) {
        emptyList()
    }

    // 4. Fetch recent quizzes
    val quizzes = try {
        supabase.from("quizzes")
            .select()
            .decodeList<Quiz>()
    } catch (e: Exception) {
        emptyList()
    }

    // Process stats
    val totalQueries = messages.size
    val libraryItems = bookmarks.size

    // Calculate subject focus
    var mathCount = 0
    var itCount = 0
    var scienceCount = 0
    messages.forEach { msg ->
        val sub = msg.subject?.lowercase()
        if (sub == "math") mathCount++
        else if (sub == "it" || sub == "programming") itCount++
        else if (sub == "science" || sub == "chemistry" || sub == "biology" || sub == "physics") scienceCount++
    }

    // Calculate streak from unique message dates
    val uniqueDates = messages.mapNotNull { msg ->
        msg.created_at?.take(10) // Format: YYYY-MM-DD
    }.distinct()

    var streak = 0
    val today = LocalDate.now().toString()
    val yesterday = LocalDate.now().minusDays(1).toString()

    if (uniqueDates.contains(today) || uniqueDates.contains(yesterday)) {
        var checkDate = if (uniqueDates.contains(today)) LocalDate.now() else LocalDate.now().minusDays(1)
        while (uniqueDates.contains(checkDate.toString())) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
    }

    val totalXp = totalQueries * 10 + libraryItems * 20
    val level = (totalXp / 100) + 1
    val xp = totalXp % 100

    val totalSubjects = mathCount + itCount + scienceCount
    val displayEmail = profile?.email ?: "Student"
    val displayName = profile?.display_name ?: displayEmail.substringBefore("@")

    return DashboardStats(
        displayName = displayName,
        totalQueries = totalQueries,
        libraryItems = libraryItems,
        streak = streak,
        studyTime = String.format(java.util.Locale.US, "%.1f", totalQueries * 0.15),
        level = level,
        xp = xp,
        mathPct = if (totalSubjects > 0) Math.round((mathCount.toFloat() / totalSubjects) * 100) else 0,
        itPct = if (totalSubjects > 0) Math.round((itCount.toFloat() / totalSubjects) * 100) else 0,
        sciencePct = if (totalSubjects > 0) Math.round((scienceCount.toFloat() / totalSubjects) * 100) else 100,
        quizzes = quizzes
    )
}
