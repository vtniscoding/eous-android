package com.eous.mentor.features.auth.intro

data class IntroState(
    val activeSlideIndex: Int = 0,
    val slides: List<Slide> = emptyList()
)
