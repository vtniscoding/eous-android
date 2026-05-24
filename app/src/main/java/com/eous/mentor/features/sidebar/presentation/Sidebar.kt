package com.eous.mentor.features.sidebar.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.R
import com.eous.mentor.core.theme.*
import com.eous.mentor.features.chat.presentation.Chat
import com.eous.mentor.features.dashboard.presentation.Dashboard
import com.eous.mentor.features.pro.presentation.Pro
import com.eous.mentor.features.search.presentation.Search
import com.eous.mentor.features.tools.presentation.Tools
import com.eous.mentor.features.userlibrary.presentation.Library

@Composable
fun Sidebar(
    navController: NavController,
    userId: String
) {
    var isSidebarOpen by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("dashboard") }
    var chatInitialQuestion by remember { mutableStateOf("") }

    // Sidebar items lists
    val recentChats = remember {
        mutableStateListOf(
            "how i can draw squre...",
            "Explain Photosynthesis in ..."
        )
    }

    val sidebarWidth = 280.dp

    // Animation States
    val sidebarOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) 0.dp else -sidebarWidth,
        animationSpec = tween(durationMillis = 300),
        label = "sidebar_offset"
    )

    val contentOffset by animateDpAsState(
        targetValue = if (isSidebarOpen) sidebarWidth else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "content_offset"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isSidebarOpen) 0.6f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "overlay_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- 1. MAIN CONTENT WRAPPER (Pushed to the right) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = contentOffset)
        ) {
            when (currentScreen) {
                "dashboard" -> {
                    Dashboard(
                        navController = navController,
                        userId = userId,
                        onMenuClick = { isSidebarOpen = true }
                    )
                }
                "chat" -> {
                    Chat(
                        userId = userId,
                        onMenuClick = { isSidebarOpen = true },
                        initialQuestion = chatInitialQuestion
                    )
                }
                "tools" -> {
                    Tools(
                        onMenuClick = { isSidebarOpen = true }
                    )
                }
                "library" -> {
                    Library(
                        onMenuClick = { isSidebarOpen = true }
                    )
                }
                "pro" -> {
                    Pro(
                        onMenuClick = { isSidebarOpen = true }
                    )
                }
                "search" -> {
                    Search(
                        onMenuClick = { isSidebarOpen = true }
                    )
                }
            }

            // --- 2. DARK OVERLAY FOR SIDESCRIM ---
            if (isSidebarOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isSidebarOpen = false
                        }
                )
            }
        }

        // --- 3. SIDEBAR DRAWER PANEL (Slides in on top of background, pushes content) ---
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(sidebarWidth)
                .offset(x = sidebarOffset)
                .background(Color(0xFF131416))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(0.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(vertical = 16.dp, horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Header title & logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Eous",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // + New Chat Button
                    Button(
                        onClick = {
                            chatInitialQuestion = ""
                            currentScreen = "chat"
                            isSidebarOpen = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EousIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "New Chat",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Recent Chats Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "RECENT CHATS",
                            color = MutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        recentChats.forEachIndexed { index, chat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1F1F23))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        chatInitialQuestion = chat
                                        currentScreen = "chat"
                                        isSidebarOpen = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chat,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MutedText,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable {
                                            recentChats.removeAt(index)
                                        }
                                )
                            }
                        }
                    }

                    // Search View box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1F1F23))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .clickable {
                                currentScreen = "search"
                                isSidebarOpen = false
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Search in this view...",
                                color = MutedText,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Library Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "YOUR LIBRARY",
                            color = MutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "No folders yet.",
                            color = MutedText.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Tools Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "TOOLS",
                            color = MutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Dashboard item (Green accent style)
                            val isDashboardSelected = currentScreen == "dashboard"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isDashboardSelected) Color(0xFF092920) else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isDashboardSelected) Color(0xFF0D5E49) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable {
                                        currentScreen = "dashboard"
                                        isSidebarOpen = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridView,
                                        contentDescription = null,
                                        tint = if (isDashboardSelected) Color(0xFF05B68A) else MutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Dashboard",
                                        color = if (isDashboardSelected) Color(0xFF05B68A) else MutedText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Quizzes item (Purple accent style)
                            val isQuizzesSelected = currentScreen == "tools"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        if (isQuizzesSelected) Color(0xFF22173B) else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isQuizzesSelected) Color(0xFF452C70) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable {
                                        currentScreen = "tools"
                                        isSidebarOpen = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isQuizzesSelected) Color(0xFF9E7CFA) else MutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Quizzes",
                                        color = if (isQuizzesSelected) Color(0xFF9E7CFA) else MutedText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Section (Unlock Eous Pro promo card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1A1A22))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Unlock Eous Pro",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(EousPurple, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        "PRO",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Text(
                                "Get unlimited chats, custom flashcards, and textbook scan features.",
                                color = MutedText,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )

                            Button(
                                onClick = {
                                    currentScreen = "pro"
                                    isSidebarOpen = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EousPurple),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    "Upgrade Now ✨",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
