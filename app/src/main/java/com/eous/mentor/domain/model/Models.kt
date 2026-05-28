package com.eous.mentor.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(val id: Int, val name: String)

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
    val onboarding_completed: Boolean = false,
    val subjects: List<String> = emptyList()
)

/**
 * A chat session grouping messages together.
 * Maps to the `sessions` table in Supabase.
 */
@Serializable
data class ChatSession(
    val id: String? = null,
    val user_id: String? = null,
    val title: String = "New Chat",
    val created_at: String? = null
)

/**
 * A single chat message (user or AI).
 * Maps to the `messages` table in Supabase.
 */
@Serializable
data class ChatMessage(
    val id: String? = null,
    val user_id: String? = null,
    val session_id: String? = null,
    val role: String,          // "user" or "ai"
    val content: String = "",
    val image: String? = null, // Storage URL for uploaded images
    val is_bookmarked: Boolean = false,
    val bookmark_folder: String? = null,
    val subject: String? = null,
    val review_status: String? = "pending",
    val created_at: String? = null
)

/**
 * Legacy Message used by existing dashboard stats fetching.
 * Kept for backward compatibility with totalQueries count.
 */
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
    val user_id: String? = null,
    val message_id: String? = null,
    val folder: String? = null,
    val created_at: String? = null
)

@Serializable
data class BookmarkWithMessage(
    val id: String? = null,
    val user_id: String? = null,
    val message_id: String? = null,
    val folder: String? = null,
    val created_at: String? = null,
    val message: ChatMessage? = null
)

@Serializable
data class Quiz(
    val id: String,
    val topic: String,
    val score: Int,
    val total_questions: Int,
    val created_at: String
)

/**
 * Request body sent to the ai-chat Edge Function.
 */
@Serializable
data class AiChatRequest(
    val message: String,
    val history: List<AiChatHistoryItem> = emptyList(),
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class AiChatHistoryItem(
    val role: String,  // "user" or "model"
    val content: String
)

/**
 * Response body from the ai-chat Edge Function.
 */
@Serializable
data class AiChatResponse(
    val reply: String,
    val subject: String? = null
)
