package com.eous.mentor.features.chat.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.eous.mentor.core.theme.*
import com.eous.mentor.data.model.Message

@Composable
fun Chat(
    userId: String,
    onMenuClick: () -> Unit,
    initialQuestion: String = ""
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf(initialQuestion) }
    val messages = remember {
        mutableStateListOf<Message>(
            Message(role = "assistant", content = "Hello! I am Eous, your AI Study Mentor. How can I help you master your subjects today? 🚀")
        )
    }
    var isSending by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background decorative glow
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 100.dp, y = (-100).dp)
                .size(350.dp)
                .blur(80.dp)
                .background(Brush.radialGradient(listOf(EousPurple.copy(alpha = 0.15f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // App Bar
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
                            contentDescription = "Open Sidebar",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "AI Chat",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Brush.linearGradient(listOf(EousPurple, EousIndigo)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("S", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Message History list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
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

                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .background(if (isUser) Color(0xFF6856E6) else CardBackground.copy(alpha = 0.5f))
                                .border(
                                    1.dp,
                                    if (isUser) Color.Transparent else BorderColor.copy(alpha = 0.4f),
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 4.dp,
                                        bottomEnd = if (isUser) 4.dp else 16.dp
                                    )
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.content,
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
            }

            // Input Bar area
            Surface(
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask your mentor anything...", color = MutedText.copy(alpha = 0.5f), fontSize = 14.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (inputText.trim().isNotEmpty()) {
                                    val userMsg = Message(role = "user", content = inputText)
                                    messages.add(userMsg)
                                    val queryText = inputText
                                    inputText = ""
                                    isSending = true
                                    
                                    // Simulated simple mock reply
                                    messages.add(Message(role = "assistant", content = "Thinking... 🤖"))
                                    val replyIndex = messages.lastIndex
                                    
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        messages[replyIndex] = Message(
                                            role = "assistant",
                                            content = "I've analyzed your question: '$queryText'. Let's break this down into simple, easy-to-understand concepts!"
                                        )
                                        isSending = false
                                    }, 1500)
                                }
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .background(EousPurple, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF18191B),
                        unfocusedContainerColor = Color(0xFF18191B),
                        focusedBorderColor = EousPurple,
                        unfocusedBorderColor = BorderColor.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}
