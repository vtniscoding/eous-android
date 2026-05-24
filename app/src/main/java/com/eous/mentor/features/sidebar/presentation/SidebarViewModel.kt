package com.eous.mentor.features.sidebar.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SidebarViewModel : ViewModel() {
    private val _state = MutableStateFlow(SidebarState())
    val state: StateFlow<SidebarState> = _state.asStateFlow()

    fun setSidebarOpen(open: Boolean) {
        _state.update { it.copy(isSidebarOpen = open) }
    }

    fun navigateTo(screen: String, initialQuestion: String = "") {
        _state.update {
            it.copy(
                currentScreen = screen,
                chatInitialQuestion = initialQuestion,
                isSidebarOpen = false
            )
        }
    }

    fun deleteRecentChat(index: Int) {
        _state.update {
            val newList = it.recentChats.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            it.copy(recentChats = newList)
        }
    }
}
