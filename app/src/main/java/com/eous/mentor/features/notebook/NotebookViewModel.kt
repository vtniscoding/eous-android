package com.eous.mentor.features.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.model.ChatMessage
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotebookViewModel(
    private val userId: String = "",
    private val chatRepository: ChatRepository = RepositoryProvider.chatRepository,
    private val userRepository: UserRepository = RepositoryProvider.userRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotebookState())
    val state: StateFlow<NotebookState> = _state.asStateFlow()

    companion object {
        val DEFAULT_TOPICS = listOf("Math", "Science", "Programming", "Foreign Languages")
    }

    init {
        if (userId.isNotEmpty()) loadNotebookData(userId)
    }

    fun loadNotebookData(userId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // 1. Fetch Profile for custom subjects
            val profileResult = userRepository.getProfile(userId)
            val dbCustomSubjects = profileResult.getOrNull()?.subjects ?: emptyList()
            
            // Default topics + custom subjects (excluding defaults to prevent duplication)
            val combinedSubjects = (DEFAULT_TOPICS + dbCustomSubjects).distinct()
            
            // 2. Fetch bookmarked messages
            chatRepository.getBookmarkedMessages(userId)
                .onSuccess { msgs ->
                    _state.update {
                        it.copy(
                            subjects = combinedSubjects,
                            bookmarkedMessages = msgs,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            subjects = combinedSubjects,
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    fun selectSubject(subject: String?) {
        _state.update { it.copy(selectedSubject = subject) }
    }

    fun addSubject(userId: String, name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        
        if (DEFAULT_TOPICS.any { it.equals(trimmedName, ignoreCase = true) }) return
        
        viewModelScope.launch {
            val profileResult = userRepository.getProfile(userId)
            val currentCustom = profileResult.getOrNull()?.subjects ?: emptyList()
            
            if (currentCustom.any { it.equals(trimmedName, ignoreCase = true) }) return@launch
            
            val updated = currentCustom + trimmedName
            userRepository.updateSubjects(userId, updated)
                .onSuccess {
                    _state.update { it.copy(subjects = DEFAULT_TOPICS + updated) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun removeSubject(userId: String, name: String) {
        if (DEFAULT_TOPICS.contains(name)) return
        
        viewModelScope.launch {
            val profileResult = userRepository.getProfile(userId)
            val currentCustom = profileResult.getOrNull()?.subjects ?: emptyList()
            
            val updated = currentCustom.filter { it != name }
            userRepository.updateSubjects(userId, updated)
                .onSuccess {
                    val newSelected = if (_state.value.selectedSubject == name) null else _state.value.selectedSubject
                    _state.update { it.copy(subjects = DEFAULT_TOPICS + updated, selectedSubject = newSelected) }
                }
                .onFailure { error ->
                    _state.update { it.copy(errorMessage = error.message) }
                }
        }
    }

    fun removeBookmark(message: ChatMessage, userId: String) {
        val msgId = message.id ?: return
        viewModelScope.launch {
            chatRepository.toggleBookmark(
                messageId = msgId,
                userId = userId,
                isBookmarked = false
            ).onSuccess {
                _state.update { state ->
                    val updatedMsgs = state.bookmarkedMessages.filter { it.id != msgId }
                    state.copy(bookmarkedMessages = updatedMsgs)
                }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
