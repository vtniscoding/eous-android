package com.eous.mentor.features.chat

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.eous.mentor.core.ui.theme.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.ChatSession
import com.eous.mentor.features.notebook.NotebookViewModel
import com.eous.mentor.domain.model.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun Chat(
        userId: String,
        onMenuClick: () -> Unit = {},
        initialQuestion: String = "",
        viewModel: ChatViewModel,
        onNavigateToSearch: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Collapsible sessions panel
    var showSessionsPanel by remember { mutableStateOf(false) }
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }
    var sessionToRename by remember { mutableStateOf<ChatSession?>(null) }
    var renameText by remember { mutableStateOf("") }

    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredMessages = remember(state.messages, searchQuery) {
        if (searchQuery.isEmpty()) {
            state.messages
        } else {
            state.messages.filter { it.content.contains(searchQuery, ignoreCase = true) }
        }
    }

    LaunchedEffect(sessionToRename) {
        sessionToRename?.let { renameText = it.title }
    }

    // Image picker launcher
    val imagePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri
                ->
                uri?.let { viewModel.onImagePicked(it, context) }
            }

    // Auto-scroll to bottom when new messages arrive or session is switched
    var lastActiveSessionId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(state.activeSession?.id, state.messages.size) {
        val currentSessionId = state.activeSession?.id
        if (currentSessionId != lastActiveSessionId) {
            // Session switched: snap to the bottom immediately (no animation)
            if (state.messages.isNotEmpty()) {
                listState.scrollToItem(state.messages.size - 1)
            }
            lastActiveSessionId = currentSessionId
        } else {
            // Same session, new message added: animate smoothly to bottom
            if (state.messages.isNotEmpty()) {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }

    // Auto-scroll to bottom when the keyboard opens
    val isKeyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(isKeyboardOpen) {
        if (isKeyboardOpen && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    // Show error toast
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    var messageToBookmark by remember { mutableStateOf<ChatMessage?>(null) }
    val userRepository = remember { RepositoryProvider.userRepository }
    var subjects by remember { mutableStateOf(NotebookViewModel.DEFAULT_TOPICS) }

    LaunchedEffect(userId, messageToBookmark) {
        if (messageToBookmark != null) {
            userRepository.getProfile(userId).onSuccess { profile ->
                profile?.subjects?.let { dbSubjects ->
                    subjects = (NotebookViewModel.DEFAULT_TOPICS + dbSubjects).distinct()
                }
            }
        }
    }

    if (messageToBookmark != null) {
        var showNewSubjectField by remember { mutableStateOf(false) }
        var newSubjectText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { messageToBookmark = null },
            title = { Text("Select Notebook Topic", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            containerColor = Color(0xFF1E1E24),
            textContentColor = Color.White,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Choose a topic to organize this AI response in your Notebook:",
                        color = MutedText,
                        fontSize = 13.sp
                    )

                    subjects.forEach { subject ->
                        Card(
                            onClick = {
                                viewModel.toggleBookmark(messageToBookmark!!, folder = subject)
                                messageToBookmark = null
                            },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2E38)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(subject, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }

                    if (!showNewSubjectField) {
                        TextButton(
                            onClick = { showNewSubjectField = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = EousPurple)
                        ) {
                            Text("+ Create New Topic", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedTextField(
                            value = newSubjectText,
                            onValueChange = { newSubjectText = it },
                            placeholder = { Text("Topic name...", color = MutedText) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EousPurple,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            TextButton(onClick = { showNewSubjectField = false }) {
                                Text("Cancel", color = MutedText)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val trimmed = newSubjectText.trim()
                                    if (trimmed.isNotEmpty()) {
                                        val currentCustom = subjects.filterNot { NotebookViewModel.DEFAULT_TOPICS.contains(it) }
                                        if (!currentCustom.contains(trimmed)) {
                                            val updatedCustom = currentCustom + trimmed
                                            coroutineScope.launch {
                                                userRepository.updateSubjects(userId, updatedCustom)
                                            }
                                            subjects = NotebookViewModel.DEFAULT_TOPICS + updatedCustom
                                        }
                                        viewModel.toggleBookmark(messageToBookmark!!, folder = trimmed)
                                        messageToBookmark = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EousPurple)
                            ) {
                                Text("Save", color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Rename session dialog
    if (sessionToRename != null) {
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("Rename Chat", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    placeholder = { Text("Chat name...", color = MutedText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EousPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = renameText.trim()
                        if (trimmed.isNotEmpty()) {
                            sessionToRename?.id?.let { id -> viewModel.renameSession(id, trimmed) }
                            sessionToRename = null
                        }
                    }
                ) { Text("Save", color = EousPurple, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Color(0xFF1E1F22),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f)
        )
    }

    // Delete session dialog
    if (sessionToDelete != null) {
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Chat", color = Color.White) },
            text = { Text("Are you sure you want to delete this chat session?", color = Color.White.copy(alpha = 0.7f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        sessionToDelete?.id?.let { id -> viewModel.deleteSession(id) }
                        sessionToDelete = null
                    }
                ) { Text("Delete", color = EousRed) }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Color(0xFF1E1F22),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.7f)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Background decorative glow
        Box(
                modifier =
                        Modifier.align(Alignment.TopEnd)
                                .offset(x = 100.dp, y = (-100).dp)
                                .size(350.dp)
                                .blur(80.dp)
                                .background(
                                        Brush.radialGradient(
                                                listOf(
                                                        EousPurple.copy(alpha = 0.15f),
                                                        Color.Transparent
                                                )
                                        )
                                )
        )

        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .imePadding()
        ) {
            // ---- Top App Bar ----
            ChatTopBar(
                    sessionTitle = state.activeSession?.title ?: "AI Chat",
                    onNewChat = {
                        viewModel.createNewSession()
                        showSessionsPanel = false
                        isSearchMode = false
                        searchQuery = ""
                    },
                    onToggleSessions = { showSessionsPanel = !showSessionsPanel },
                    showSessionsPanel = showSessionsPanel,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = { searchQuery = it },
                    isSearchMode = isSearchMode,
                    onSearchModeChanged = { active ->
                        isSearchMode = active
                        if (!active) {
                            searchQuery = ""
                        }
                    }
            )

            // ---- Sessions Panel (collapsible) ----
            AnimatedVisibility(
                visible = showSessionsPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ChatSessionsPanel(
                    sessions = state.sessions,
                    activeSessionId = state.activeSession?.id,
                    isLoading = state.isLoadingSessions,
                    onSelectSession = { session ->
                        viewModel.selectSession(session)
                        showSessionsPanel = false
                        isSearchMode = false
                        searchQuery = ""
                    },
                    onDeleteSession = { session -> sessionToDelete = session },
                    onRenameSession = { session -> sessionToRename = session }
                )
            }

            // ---- Content Area below dropdown ----
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ---- Main Content ----
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            state.isLoadingSessions || state.isLoadingMessages -> {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                            color = EousPurple,
                                            strokeWidth = 3.dp,
                                            modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            state.messages.isEmpty() -> {
                                // Empty state
                                EmptyState()
                            }
                            else -> {
                                // Message list
                                LazyColumn(
                                        state = listState,
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                                ) {
                                    items(filteredMessages, key = { it.id ?: it.hashCode() }) { msg ->
                                        MessageBubble(
                                                message = msg,
                                                onToggleBookmark = {
                                                    if (msg.is_bookmarked) {
                                                        viewModel.toggleBookmark(msg)
                                                    } else {
                                                        messageToBookmark = msg
                                                    }
                                                }
                                        )
                                    }
                                    // Thinking indicator
                                    if (state.isAiResponding) {
                                        item { ThinkingIndicator() }
                                    }
                                }
                            }
                        }
                    }

                    // ---- Input Bar ----
                    ChatInputBar(
                            inputText = state.inputText,
                            onInputChanged = { viewModel.onInputTextChanged(it) },
                            onSend = { viewModel.sendMessage() },
                            onStop = { viewModel.stopResponding() },
                            onPickImage = { imagePickerLauncher.launch("image/*") },
                            isAiResponding = state.isAiResponding,
                            isSending = state.isSending,
                            hasImage = state.pendingImageUri != null,
                            isImageUploading = state.pendingImageUri != null && state.pendingImageUrl == null,
                            pendingImageUri = state.pendingImageUri,
                            onClearImage = { viewModel.clearPendingImage() }
                    )
                }

                // Overlay when sessions panel is open to auto-close when clicking outside
                if (showSessionsPanel) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                showSessionsPanel = false
                            }
                    )
                }
            }
        }
    }
}

// ---- Sub-components ----

@Composable
private fun ChatTopBar(
        sessionTitle: String,
        onNewChat: () -> Unit,
        onToggleSessions: () -> Unit,
        showSessionsPanel: Boolean,
        searchQuery: String,
        onSearchQueryChanged: (String) -> Unit,
        isSearchMode: Boolean,
        onSearchModeChanged: (Boolean) -> Unit
) {
    AnimatedContent(
        targetState = isSearchMode,
        transitionSpec = {
            if (targetState) {
                // Fade in and slide/shift slightly from the right (smooth translation)
                (slideInHorizontally(
                    animationSpec = tween(250),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.08f).toInt() }
                ) + fadeIn(animationSpec = tween(250))).togetherWith(
                    fadeOut(animationSpec = tween(150))
                )
            } else {
                // Fade in and slide/shift slightly from the left
                (slideInHorizontally(
                    animationSpec = tween(250),
                    initialOffsetX = { fullWidth -> -(fullWidth * 0.08f).toInt() }
                ) + fadeIn(animationSpec = tween(250))).togetherWith(
                    fadeOut(animationSpec = tween(150))
                )
            }
        },
        label = "TopBarSearchTransition"
    ) { searchActive ->
        if (searchActive) {
            // Expanded Search Bar (matching Pic 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onSearchQueryChanged("")
                            onSearchModeChanged(false)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        cursorBrush = SolidColor(EousPurple),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "Search message contents...",
                                        color = MutedText.copy(alpha = 0.5f),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChanged("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Normal Top Bar (matching Pic 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title row (tappable to toggle sessions list)
                val displayTitle = if (sessionTitle.length > 14) sessionTitle.take(14) + "..." else sessionTitle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onToggleSessions
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayTitle,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = if (showSessionsPanel) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Combined capsule pill (like Pic 1)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search button
                    IconButton(
                        onClick = { onSearchModeChanged(true) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // New Chat circle button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EousPurple)
                            .clickable { onNewChat() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatSessionsPanel(
    sessions: List<ChatSession>,
    activeSessionId: String?,
    isLoading: Boolean,
    onSelectSession: (ChatSession) -> Unit,
    onDeleteSession: (ChatSession) -> Unit,
    onRenameSession: (ChatSession) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0E0E14))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "RECENT CHATS",
            color = MutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = EousPurple,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else if (sessions.isEmpty()) {
            Text(
                "No recent chats yet.",
                color = MutedText.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        } else {
            // Show max 5 sessions
            sessions.take(5).forEach { session ->
                val isActive = session.id == activeSessionId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) EousPurple.copy(alpha = 0.1f) else Color(0xFF1A1A22))
                        .border(
                            1.dp,
                            if (isActive) EousPurple.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.04f),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectSession(session) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (isActive) EousPurple else MutedText,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = session.title,
                            color = if (isActive) EousPurple else Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onRenameSession(session) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = MutedText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteSession(session) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = EousPurple,
                    modifier = Modifier.size(48.dp)
            )
            Text(
                    text = "Start a conversation",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
            )
            Text(
                    text = "Ask your AI mentor anything about\nyour studies!",
                    color = MutedText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, onToggleBookmark: () -> Unit) {
    val isUser = message.role == "user"

    Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // AI label
        if (!isUser) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = EousPurple,
                        modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                        text = "Eous AI Mentor",
                        color = EousPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                )
            }
        }

        // Message content
        Box(
                modifier =
                        Modifier.clip(
                                        RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                )
                                .background(
                                        if (isUser) Color(0xFF6856E6)
                                        else CardBackground.copy(alpha = 0.5f)
                                )
                                .border(
                                        1.dp,
                                        if (isUser) Color.Transparent
                                        else BorderColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 4.dp,
                                                bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .widthIn(max = 300.dp)
        ) {
            Column {
                // Image attachment
                if (!message.image.isNullOrEmpty()) {
                    AsyncImage(
                            model = message.image,
                            contentDescription = "Attached image",
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.FillWidth
                    )
                    if (message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (message.content.isNotEmpty()) {
                    Text(
                            text = parseMarkdownAndMath(message.content),
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                    )
                }
            }
        }

        // Bookmark action for AI messages
        if (!isUser && message.id != null) {
            Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(24.dp)) {
                    Icon(
                            imageVector =
                                    if (message.is_bookmarked) Icons.Outlined.Bookmark
                                    else Icons.Outlined.BookmarkBorder,
                            contentDescription =
                                    if (message.is_bookmarked) "Remove Bookmark" else "Bookmark",
                            tint = if (message.is_bookmarked) EousPurple else MutedText,
                            modifier = Modifier.size(16.dp)
                    )
                }
                if (message.is_bookmarked) {
                    Text(
                            text = "Saved",
                            color = EousPurple,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = EousPurple,
                    modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                    text = "Eous AI Mentor",
                    color = EousPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
            )
        }

        Box(
                modifier =
                        Modifier.clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                                .background(CardBackground.copy(alpha = 0.5f))
                                .border(
                                        1.dp,
                                        BorderColor.copy(alpha = 0.4f),
                                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Thinking", color = MutedText, fontSize = 14.sp)
                CircularProgressIndicator(
                        color = EousPurple,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun PendingImagePreviewCompact(
        imageUri: String,
        isUploading: Boolean,
        onClear: () -> Unit
) {
    Box(
            modifier = Modifier
                    .padding(start = 10.dp, top = 8.dp, bottom = 4.dp)
                    .size(56.dp)
    ) {
        AsyncImage(
                model = imageUri,
                contentDescription = "Attached image preview",
                modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
        )

        if (isUploading) {
            Box(
                    modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                        color = EousPurple,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                )
            }
        }

        // Close button overlay
        Box(
                modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(18.dp)
                        .background(Color.Black, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onClear() },
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove image",
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
        inputText: String,
        onInputChanged: (String) -> Unit,
        onSend: () -> Unit,
        onStop: () -> Unit,
        onPickImage: () -> Unit,
        isAiResponding: Boolean,
        isSending: Boolean,
        hasImage: Boolean,
        isImageUploading: Boolean,
        pendingImageUri: String?,
        onClearImage: () -> Unit
) {
    Surface(
            color = Color.Black,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Pill container for text input and image picker
            Column(
                    modifier =
                            Modifier.width(0.dp)
                                    .weight(1f)
                                    .background(Color(0xFF18191B), RoundedCornerShape(24.dp))
                                    .border(
                                            1.dp,
                                            BorderColor.copy(alpha = 0.2f),
                                            RoundedCornerShape(24.dp)
                                    )
            ) {
                if (hasImage && pendingImageUri != null) {
                    PendingImagePreviewCompact(
                            imageUri = pendingImageUri,
                            isUploading = isImageUploading,
                            onClear = onClearImage
                    )
                }

                Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image picker button
                    IconButton(
                            onClick = onPickImage,
                            modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Attach Image",
                                tint = if (hasImage) EousPurple else MutedText.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Multiline input field
                    Box(
                            modifier =
                                    Modifier.width(0.dp)
                                            .weight(1f)
                                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        if (inputText.isEmpty()) {
                            Text(
                                    text = "Ask your mentor anything...",
                                    color = MutedText.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                            )
                        }

                        BasicTextField(
                                value = inputText,
                                onValueChange = onInputChanged,
                                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                cursorBrush = SolidColor(EousPurple),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                        )
                    }
                }
            }

            // Send/Stop button outside the pill
            val canSend = (inputText.isNotBlank() || (hasImage && !isImageUploading)) && !isSending
            Box(
                    modifier =
                            Modifier.padding(bottom = 4.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                            if (isAiResponding || canSend) EousPurple
                                            else Color(0xFF18191B)
                                    )
                                    .clickable(
                                            enabled = isAiResponding || canSend,
                                            onClick = { if (isAiResponding) onStop() else onSend() }
                                    ),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector =
                                if (isAiResponding) Icons.Default.Stop
                                else Icons.Default.ArrowUpward,
                        contentDescription = if (isAiResponding) "Stop" else "Send",
                        tint =
                                if (isAiResponding || canSend) Color.White
                                else MutedText.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
