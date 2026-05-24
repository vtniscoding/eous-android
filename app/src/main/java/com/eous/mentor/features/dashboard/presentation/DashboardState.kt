package com.eous.mentor.features.dashboard.presentation

import com.eous.mentor.data.model.DashboardStats

data class DashboardState(
    val stats: DashboardStats = DashboardStats(
        displayName = "Student",
        totalQueries = 6,
        libraryItems = 0,
        streak = 0,
        studyTime = "0.9",
        level = 1,
        xp = 60,
        mathPct = 0,
        itPct = 0,
        sciencePct = 100,
        quizzes = emptyList()
    ),
    val isLoading: Boolean = true,
    val isLoggedOut: Boolean = false
)
