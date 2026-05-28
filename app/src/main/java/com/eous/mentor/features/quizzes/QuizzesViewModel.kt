package com.eous.mentor.features.quizzes

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizzesViewModel : ViewModel() {
    private val _state = MutableStateFlow(QuizzesState())
    val state: StateFlow<QuizzesState> = _state.asStateFlow()
}
