package com.eous.mentor.data.model

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(val id: Int, val name: String)

@Serializable
data class DashboardStats(
    val displayName: String,
    val totalQueries: Int,
    val libraryItems: Int,
    val streak: Int,
    val studyTime: String,
    val level: Int,
    val xp: Int,
    val mathPct: Int,
    val itPct: Int,
    val sciencePct: Int,
    val quizzes: List<Quiz> = emptyList()
)

@Serializable
data class Profile(
    val id: String,
    val email: String? = null,
    val display_name: String? = null,
    val onboarding_completed: Boolean = false
)

@Serializable
data class Message(
    val id: String? = null,
    val role: String,
    val content: String = "",
    val subject: String? = null,
    val created_at: String? = null
)

@Serializable
data class Bookmark(
    val id: String? = null,
    val folder: String? = null
)

@Serializable
data class Quiz(
    val id: String,
    val topic: String,
    val score: Int,
    val total_questions: Int,
    val created_at: String
)
