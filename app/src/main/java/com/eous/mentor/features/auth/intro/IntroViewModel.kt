package com.eous.mentor.features.auth.intro

import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.core.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IntroViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        IntroState(
            activeSlideIndex = 0,
            slides = listOf(
                Slide(
                    title = "Your Personal AI Mentor",
                    description = "Works 24/7 with customized, step-by-step explanations tailored just for you.",
                    themeColor = Brush.horizontalGradient(listOf(EousPurple, EousIndigo))
                ),
                Slide(
                    title = "Tailored to Your Level",
                    description = "Whether you are in Middle School, High School, or University, Eous adapts to you.",
                    themeColor = Brush.horizontalGradient(listOf(EousBlue, EousIndigo))
                ),
                Slide(
                    title = "Active Recall Quizzes",
                    description = "Test your skills with interactive flashcards and gamified subject tracking.",
                    themeColor = Brush.horizontalGradient(listOf(EousPink, EousRed))
                )
            )
        )
    )
    val state: StateFlow<IntroState> = _state.asStateFlow()

    private var autoAdvanceJob: Job? = null

    init {
        startAutoAdvance()
    }

    private fun startAutoAdvance() {
        autoAdvanceJob = viewModelScope.launch {
            while (true) {
                delay(4000)
                _state.update { current ->
                    val slides = current.slides
                    if (slides.isNotEmpty()) {
                        val nextIndex = (current.activeSlideIndex + 1) % slides.size
                        current.copy(activeSlideIndex = nextIndex)
                    } else {
                        current
                    }
                }
            }
        }
    }
}
