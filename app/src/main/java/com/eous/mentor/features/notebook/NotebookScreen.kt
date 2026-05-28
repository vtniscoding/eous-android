package com.eous.mentor.features.notebook

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eous.mentor.core.ui.theme.*

@Composable
fun Notebook(
    onMenuClick: () -> Unit,
    userId: String,
    viewModel: NotebookViewModel = remember { NotebookViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Load data when entering the screen
    LaunchedEffect(userId) {
        viewModel.loadNotebookData(userId)
    }

    // Show error toast
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    var showAddTopicDialog by remember { mutableStateOf(false) }
    var newTopicName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Decorative glows
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(300.dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(EousPink.copy(alpha = 0.08f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(350.dp)
                .blur(90.dp)
                .background(Brush.radialGradient(listOf(EousPurple.copy(alpha = 0.08f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
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
                    Text(
                        text = "Notebook",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scrollable Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // extra padding so list content isn't covered by FAB
            ) {
                // Intro text
                item {
                    Text(
                        text = "Bookmark important AI responses during chat, select a topic, and organize them here for review.",
                        color = MutedText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Topics Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOPICS",
                            color = MutedText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showAddTopicDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Custom Topic",
                                tint = EousPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "All" filter item
                        val isAllSelected = state.selectedSubject == null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAllSelected) Color(0xFF1E1E24) else CardBackground)
                                .border(
                                    1.dp,
                                    if (isAllSelected) EousPurple.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.selectSubject(null) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = if (isAllSelected) EousPurple else MutedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "All Topics",
                                color = if (isAllSelected) Color.White else MutedText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            val allCount = state.bookmarkedMessages.size
                            Text(
                                text = allCount.toString(),
                                color = MutedText.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }

                        // Individual subject folder items
                        state.subjects.forEach { subject ->
                            val isSelected = state.selectedSubject == subject
                            val isDefault = NotebookViewModel.DEFAULT_TOPICS.contains(subject)
                            val count = state.bookmarkedMessages.count { it.bookmark_folder == subject }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF1E1E24) else CardBackground)
                                    .border(
                                        1.dp,
                                        if (isSelected) EousPurple.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectSubject(subject) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) EousPurple else MutedText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = subject,
                                    color = if (isSelected) Color.White else MutedText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    text = count.toString(),
                                    color = MutedText.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = if (!isDefault) 8.dp else 0.dp)
                                )

                                // Only custom topics can be deleted
                                if (!isDefault) {
                                    IconButton(
                                        onClick = { viewModel.removeSubject(userId, subject) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete Topic",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bookmarks header
                val filteredMessages = if (state.selectedSubject == null) {
                    state.bookmarkedMessages
                } else {
                    state.bookmarkedMessages.filter { it.bookmark_folder == state.selectedSubject }
                }

                item {
                    Text(
                        text = if (state.selectedSubject == null) "ALL BOOKMARKS" else "${state.selectedSubject?.uppercase()} BOOKMARKS",
                        color = MutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (filteredMessages.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MutedText.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "No bookmarked answers in this topic yet.",
                                    color = MutedText,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMessages, key = { it.id ?: it.hashCode() }) { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Subject Tag & Remove Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Topic tag pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EousPurple.copy(alpha = 0.15f))
                                            .border(1.dp, EousPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = msg.bookmark_folder ?: "General",
                                            color = Color(0xFFC084FC),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Remove Bookmark Button
                                    IconButton(
                                        onClick = { viewModel.removeBookmark(msg, userId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Bookmark",
                                            tint = MutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Message Content
                                Text(
                                    text = msg.content,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

    }

    // Add Custom Topic Dialog
    if (showAddTopicDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddTopicDialog = false
                newTopicName = ""
            },
            title = {
                Text(
                    text = "Add Custom Topic",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            containerColor = Color(0xFF1E1E24),
            textContentColor = Color.White,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Create a custom subject topic to organize your bookmarked AI answers:",
                        color = MutedText,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = newTopicName,
                        onValueChange = { newTopicName = it },
                        placeholder = { Text("Topic name...", color = MutedText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EousPurple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newTopicName.trim()
                        if (name.isNotEmpty()) {
                            viewModel.addSubject(userId, name)
                        }
                        showAddTopicDialog = false
                        newTopicName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EousPurple)
                ) {
                    Text("Add Topic", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddTopicDialog = false
                        newTopicName = ""
                    }
                ) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}
