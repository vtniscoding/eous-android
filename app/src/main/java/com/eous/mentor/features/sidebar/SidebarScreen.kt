package com.eous.mentor.features.sidebar

import androidx.activity.compose.BackHandler
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
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.features.chat.Chat
import com.eous.mentor.features.chat.ChatViewModel
import com.eous.mentor.features.dashboard.Dashboard
import com.eous.mentor.features.dashboard.DashboardViewModel
import com.eous.mentor.features.pro.Pro
import com.eous.mentor.features.search.Search
import com.eous.mentor.features.tools.Tools
import com.eous.mentor.features.userlibrary.Library

@Composable
fun Sidebar(
    navController: NavController,
    userId: String,
    viewModel: SidebarViewModel = remember { SidebarViewModel() },
    dashboardViewModel: DashboardViewModel = remember(userId) { DashboardViewModel(userId) }
) {
    val state by viewModel.state.collectAsState()
    val sidebarWidth = 280.dp

    val chatViewModel = remember(state.chatInitialQuestion) { ChatViewModel(state.chatInitialQuestion) }

    // System back button handling
    if (state.isSidebarOpen) {
        BackHandler {
            viewModel.setSidebarOpen(false)
        }
    } else if (state.currentScreen != "dashboard") {
        BackHandler {
            viewModel.navigateTo("dashboard")
        }
    }

    // Animation States
    val sidebarOffset by animateDpAsState(
        targetValue = if (state.isSidebarOpen) 0.dp else -sidebarWidth,
        animationSpec = tween(durationMillis = 300),
        label = "sidebar_offset"
    )

    val contentOffset by animateDpAsState(
        targetValue = if (state.isSidebarOpen) sidebarWidth else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "content_offset"
    )

    val overlayAlpha by animateFloatAsState(
        targetValue = if (state.isSidebarOpen) 0.6f else 0f,
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
            when (state.currentScreen) {
                "dashboard" -> {
                    Dashboard(
                        navController = navController,
                        userId = userId,
                        onMenuClick = { viewModel.setSidebarOpen(true) },
                        viewModel = dashboardViewModel
                    )
                }
                "chat" -> {
                    Chat(
                        userId = userId,
                        onMenuClick = { viewModel.setSidebarOpen(true) },
                        initialQuestion = state.chatInitialQuestion,
                        viewModel = chatViewModel
                    )
                }
                "tools" -> {
                    Tools(
                        onMenuClick = { viewModel.setSidebarOpen(true) }
                    )
                }
                "library" -> {
                    Library(
                        onMenuClick = { viewModel.setSidebarOpen(true) }
                    )
                }
                "pro" -> {
                    Pro(
                        onMenuClick = { viewModel.setSidebarOpen(true) }
                    )
                }
                "search" -> {
                    Search(
                        onMenuClick = { viewModel.setSidebarOpen(true) }
                    )
                }
            }

            // --- 2. DARK OVERLAY FOR SIDESCRIM ---
            if (state.isSidebarOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayAlpha))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.setSidebarOpen(false)
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Eous",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // + New Chat Button
                    Button(
                        onClick = {
                            viewModel.navigateTo("chat", "")
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

                        state.recentChats.forEachIndexed { index, chat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1F1F23))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        viewModel.navigateTo("chat", chat)
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
                                            viewModel.deleteRecentChat(index)
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
                                viewModel.navigateTo("search")
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
                            val isDashboardSelected = state.currentScreen == "dashboard"
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
                                        viewModel.navigateTo("dashboard")
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
                            val isQuizzesSelected = state.currentScreen == "tools"
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
                                        viewModel.navigateTo("tools")
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
                                    viewModel.navigateTo("pro")
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
