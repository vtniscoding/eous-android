package com.eous.mentor.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.Message
import com.eous.mentor.domain.usecase.chat.GetMessagesUseCase
import com.eous.mentor.domain.usecase.chat.SendMessageUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    initialQuestion: String = "",
    private val sendMessageUseCase: SendMessageUseCase = SendMessageUseCase(RepositoryProvider.chatRepository),
    private val getMessagesUseCase: GetMessagesUseCase = GetMessagesUseCase(RepositoryProvider.chatRepository)
) : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        if (initialQuestion.isNotEmpty()) {
            _state.update { it.copy(inputText = initialQuestion) }
        }
    }

    fun onInputTextChanged(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val currentInput = _state.value.inputText.trim()
        if (currentInput.isEmpty() || _state.value.isSending) return

        val userMessage = Message(role = "user", content = currentInput)
        val thinkingMessage = Message(role = "assistant", content = "Thinking... 🤖")

        val updatedMessages = _state.value.messages + userMessage + thinkingMessage

        _state.update {
            it.copy(
                inputText = "",
                messages = updatedMessages,
                isSending = true
            )
        }

        viewModelScope.launch {
            // Call UseCase (which routes to repository)
            sendMessageUseCase(currentInput)
            
            delay(1500)
            _state.update { state ->
                val finalMessages = state.messages.toMutableList()
                if (finalMessages.isNotEmpty()) {
                    finalMessages[finalMessages.lastIndex] = Message(
                        role = "assistant",
                        content = "I've analyzed your question: '$currentInput'. Let's break this down into simple, easy-to-understand concepts!"
                    )
                }
                state.copy(
                    messages = finalMessages,
                    isSending = false
                )
            }
        }
    }
}
