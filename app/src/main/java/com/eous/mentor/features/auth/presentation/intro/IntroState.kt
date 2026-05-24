package com.eous.mentor.features.auth.presentation.intro

data class IntroState(
    val activeSlideIndex: Int = 0,
    val slides: List<Slide> = emptyList()
)
