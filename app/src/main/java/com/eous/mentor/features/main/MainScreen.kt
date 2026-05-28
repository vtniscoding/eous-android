package com.eous.mentor.features.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.compose.animation.animateColorAsState
import com.eous.mentor.core.ui.theme.*
import com.eous.mentor.features.chat.Chat
import com.eous.mentor.features.chat.ChatViewModel
import com.eous.mentor.features.dashboard.Dashboard
import com.eous.mentor.features.dashboard.DashboardViewModel
import com.eous.mentor.features.pro.Pro
import com.eous.mentor.features.quizzes.Tools
import com.eous.mentor.features.search.Search
import com.eous.mentor.features.notebook.Notebook
import com.eous.mentor.features.notebook.NotebookViewModel

// Data class for bottom nav items
data class MainNavItem(
        val route: String,
        val label: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
)

val MainNavItems =
        listOf(
                MainNavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
                MainNavItem("tools", "Tools", Icons.Filled.Widgets, Icons.Outlined.Widgets),
                MainNavItem("chat", "Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble),
                MainNavItem("notebook", "Notebook", Icons.Filled.Book, Icons.Outlined.Book),
                MainNavItem("personal", "Personal", Icons.Filled.Person, Icons.Outlined.Person)
        )

private fun getScreenIndex(route: String): Int {
        return when (route) {
                "dashboard" -> 0
                "tools" -> 1
                "chat" -> 2
                "search" -> 2
                "notebook" -> 3
                "personal" -> 4
                "pro" -> 5
                else -> 0
        }
}

@Composable
fun MainScreen(
        navController: NavController,
        userId: String,
        viewModel: MainScreenViewModel = remember { MainScreenViewModel() },
        dashboardViewModel: DashboardViewModel = remember(userId) { DashboardViewModel(userId) }
) {
        val state by viewModel.state.collectAsState()

        val chatViewModel = remember(userId) { ChatViewModel(userId = userId) }
        val notebookViewModel = remember(userId) { NotebookViewModel(userId = userId) }

        // Show Pro upgrade dialog once on launch
        var showProDialog by remember { mutableStateOf(true) }



        // Back handler: go back to dashboard from any non-dashboard tab
        if (state.currentScreen != "dashboard") {
                BackHandler { viewModel.navigateTo("dashboard") }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                // --- MAIN CONTENT ---
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(bottom = 92.dp) // reserve space for bottom nav
                ) {
                        AnimatedContent(
                                targetState = state.currentScreen,
                                transitionSpec = {
                                        val initialIndex = getScreenIndex(initialState)
                                        val targetIndex = getScreenIndex(targetState)
                                        if (targetIndex > initialIndex) {
                                                // Slide left (new content enters from right, old
                                                // exits to left)
                                                slideInHorizontally(animationSpec = tween(300)) {
                                                        it
                                                } + fadeIn(animationSpec = tween(300)) togetherWith
                                                        slideOutHorizontally(
                                                                animationSpec = tween(300)
                                                        ) { -it } +
                                                                fadeOut(animationSpec = tween(300))
                                        } else {
                                                // Slide right (new content enters from left, old
                                                // exits to right)
                                                slideInHorizontally(animationSpec = tween(300)) {
                                                        -it
                                                } + fadeIn(animationSpec = tween(300)) togetherWith
                                                        slideOutHorizontally(
                                                                animationSpec = tween(300)
                                                        ) { it } +
                                                                fadeOut(animationSpec = tween(300))
                                        }
                                },
                                modifier = Modifier.fillMaxSize(),
                                label = "tab_transition"
                        ) { screen ->
                                when (screen) {
                                        "dashboard" -> {
                                                Dashboard(
                                                        navController = navController,
                                                        userId = userId,
                                                        onMenuClick = {},
                                                        viewModel = dashboardViewModel
                                                )
                                        }
                                        "chat" -> {
                                                Chat(
                                                        userId = userId,
                                                        onMenuClick = {},
                                                        initialQuestion = state.chatInitialQuestion,
                                                        viewModel = chatViewModel,
                                                        onNavigateToSearch = {
                                                                viewModel.navigateTo("search")
                                                        }
                                                )
                                        }
                                        "tools" -> {
                                                Tools(onMenuClick = {})
                                        }
                                        "notebook" -> {
                                                Notebook(
                                                        onMenuClick = {},
                                                        userId = userId,
                                                        viewModel = notebookViewModel
                                                )
                                        }
                                        "pro" -> {
                                                Pro(
                                                        onMenuClick = {
                                                                viewModel.navigateTo("dashboard")
                                                        }
                                                )
                                        }
                                        "search" -> {
                                                Search(
                                                        onMenuClick = {
                                                                viewModel.navigateTo("chat")
                                                        }
                                                )
                                        }
                                        "personal" -> {
                                                Box(
                                                        modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color.Black),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        Text(
                                                                text = "Personal Profile Coming Soon",
                                                                color = Color.White,
                                                                fontSize = 18.sp,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }
                                        }
                                }
                        }
                }

                // --- BOTTOM NAVIGATION BAR ---
                MainNavigationBar(
                        currentScreen = state.currentScreen,
                        onNavigate = { route -> viewModel.navigateTo(route) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                )

                // --- PRO UPGRADE DIALOG (shown on app open) ---
                if (showProDialog) {
                        MainScreenDialog(
                                onDismiss = { showProDialog = false },
                                onUpgrade = {
                                        showProDialog = false
                                        viewModel.navigateTo("pro")
                                }
                        )
                }
        }
}

@Composable
fun MainNavigationBar(
        currentScreen: String,
        onNavigate: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        Box(
                modifier = modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
        ) {
                Row(
                        modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF14141E))
                                .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        MainNavItems.forEach { item ->
                                val isSelected =
                                        currentScreen == item.route ||
                                                (item.route == "chat" &&
                                                        currentScreen == "search")
                                MainNavItemView(
                                        item = item,
                                        isSelected = isSelected,
                                        onClick = { onNavigate(item.route) }
                                )
                        }
                }
        }
}

@Composable
private fun RowScope.MainNavItemView(
        item: MainNavItem,
        isSelected: Boolean,
        onClick: () -> Unit
) {
        val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) EousPurple.copy(alpha = 0.2f) else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "nav_item_bg"
        )
        val contentColor by animateColorAsState(
                targetValue = if (isSelected) EousPurple else Color.White.copy(alpha = 0.5f),
                animationSpec = tween(durationMillis = 200),
                label = "nav_item_content"
        )

        Column(
                modifier = Modifier
                        .weight(1f)
                        .clickable(
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                onClick = onClick
                        )
                        .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
        ) {
                // Pill container for the icon
                Box(
                        modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(backgroundColor)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                ) {
                        Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = contentColor,
                                modifier = Modifier.size(25.dp)
                        )
                }
                
                Spacer(modifier = Modifier.height(3.dp))
                
                Text(
                        text = item.label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                )
        }
}

@Composable
fun MainScreenDialog(onDismiss: () -> Unit, onUpgrade: () -> Unit) {
        Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
                Box(
                        modifier =
                                Modifier.fillMaxWidth(0.9f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF14141C))
                                        .border(
                                                1.dp,
                                                Color.White.copy(alpha = 0.08f),
                                                RoundedCornerShape(24.dp)
                                        )
                ) {
                        // Background glow
                        Box(
                                modifier =
                                        Modifier.align(Alignment.TopCenter)
                                                .offset(y = (-40).dp)
                                                .size(220.dp)
                                                .background(
                                                        Brush.radialGradient(
                                                                listOf(
                                                                        EousPurple.copy(
                                                                                alpha = 0.25f
                                                                        ),
                                                                        Color.Transparent
                                                                )
                                                        )
                                                )
                        )

                        Column(
                                modifier = Modifier.padding(28.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                // Badge
                                Box(
                                        modifier =
                                                Modifier.background(
                                                                Brush.horizontalGradient(
                                                                        listOf(
                                                                                EousPurple.copy(
                                                                                        alpha = 0.2f
                                                                                ),
                                                                                EousIndigo.copy(
                                                                                        alpha = 0.2f
                                                                                )
                                                                        )
                                                                ),
                                                                RoundedCornerShape(50.dp)
                                                        )
                                                        .border(
                                                                1.dp,
                                                                EousPurple.copy(alpha = 0.35f),
                                                                RoundedCornerShape(50.dp)
                                                        )
                                                        .padding(
                                                                horizontal = 14.dp,
                                                                vertical = 5.dp
                                                        )
                                ) {
                                        Text(
                                                "⭐ Limited Time Offer",
                                                color = Purple80,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }

                                Text(
                                        text = "Unlock Eous Pro",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                )

                                Text(
                                        text =
                                                "Get unlimited AI chats, custom flashcards, textbook scan, and priority support — all in one plan.",
                                        color = MutedText,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                )

                                // Feature pills
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        listOf(
                                                        "✨ Unlimited AI mentor chats",
                                                        "📚 Custom flashcards & quizzes",
                                                        "📷 Textbook scan & summarize",
                                                        "⚡ Priority responses"
                                                )
                                                .forEach { feature ->
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically,
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .background(
                                                                                        Color(
                                                                                                0xFF1C1D25
                                                                                        ),
                                                                                        RoundedCornerShape(
                                                                                                10.dp
                                                                                        )
                                                                                )
                                                                                .padding(
                                                                                        horizontal =
                                                                                                14.dp,
                                                                                        vertical =
                                                                                                10.dp
                                                                                )
                                                        ) {
                                                                Text(
                                                                        text = feature,
                                                                        color =
                                                                                Color.White.copy(
                                                                                        alpha = 0.9f
                                                                                ),
                                                                        fontSize = 13.sp,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                        }
                                                }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Upgrade CTA
                                Button(
                                        onClick = onUpgrade,
                                        shape = RoundedCornerShape(14.dp),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor = Color.Transparent
                                                ),
                                        contentPadding = PaddingValues(),
                                        modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .background(
                                                                        Brush.horizontalGradient(
                                                                                listOf(
                                                                                        EousPurple,
                                                                                        EousIndigo
                                                                                )
                                                                        ),
                                                                        RoundedCornerShape(14.dp)
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        "Upgrade Now ✨",
                                                        color = Color.White,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                )
                                        }
                                }

                                // Dismiss link
                                TextButton(onClick = onDismiss) {
                                        Text("Maybe later", color = MutedText, fontSize = 13.sp)
                                }
                        }
                }
        }
}
