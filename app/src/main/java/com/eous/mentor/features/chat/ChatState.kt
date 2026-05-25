package com.eous.mentor.features.chat

import com.eous.mentor.domain.model.Message

data class ChatState(
    val inputText: String = "",
    val messages: List<Message> = listOf(
        Message(role = "assistant", content = "Hello! I am Eous, your AI Study Mentor. How can I help you master your subjects today? 🚀")
    ),
    val isSending: Boolean = false
)
