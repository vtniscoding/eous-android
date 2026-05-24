package com.eous.mentor.features.chat.presentation

import com.eous.mentor.data.model.Message

data class ChatState(
    val inputText: String = "",
    val messages: List<Message> = listOf(
        Message(role = "assistant", content = "Hello! I am Eous, your AI Study Mentor. How can I help you master your subjects today? 🚀")
    ),
    val isSending: Boolean = false
)
