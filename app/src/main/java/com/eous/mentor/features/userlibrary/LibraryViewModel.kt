package com.eous.mentor.features.userlibrary

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LibraryViewModel : ViewModel() {
    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    fun createFolder(name: String) {
        _state.update {
            val updatedFolders = it.folders + name
            it.copy(
                folders = updatedFolders,
                isEmpty = updatedFolders.isEmpty()
            )
        }
    }
}
