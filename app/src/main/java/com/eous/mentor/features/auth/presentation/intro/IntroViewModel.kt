package com.eous.mentor.features.auth.presentation.intro

import androidx.compose.ui.graphics.Brush
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.core.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IntroViewModel : ViewModel() {
    private val _state = MutableStateFlow(IntroState())
    val state: StateFlow<IntroState> = _state.asStateFlow()

    init {
        val initialSlides = listOf(
            Slide(
                "Your Personal AI Mentor",
                "Works 24/7 with customized, step-by-step explanations tailored just for you.",
                Brush.horizontalGradient(listOf(EousPurple, EousIndigo))
            ),
            Slide(
                "Tailored to Your Level",
                "Whether you are in Middle School, High School, or University, Eous adapts to you.",
                Brush.horizontalGradient(listOf(EousBlue, EousIndigo))
            ),
            Slide(
                "Active Recall Quizzes",
                "Test your skills with interactive flashcards and gamified subject tracking.",
                Brush.horizontalGradient(listOf(EousPink, EousRed))
            )
        )
        _state.update { it.copy(slides = initialSlides) }
        startAutoAdvance()
    }

    private fun startAutoAdvance() {
        viewModelScope.launch {
            while (true) {
                delay(4000)
                _state.update {
                    val nextIndex = if (it.slides.isNotEmpty()) {
                        (it.activeSlideIndex + 1) % it.slides.size
                    } else {
                        0
                    }
                    it.copy(activeSlideIndex = nextIndex)
                }
            }
        }
    }
}
