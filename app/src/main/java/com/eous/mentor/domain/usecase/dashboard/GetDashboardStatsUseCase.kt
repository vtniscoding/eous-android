package com.eous.mentor.domain.usecase.dashboard

import com.eous.mentor.domain.model.DashboardStats
import com.eous.mentor.domain.repository.ChatRepository
import com.eous.mentor.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class GetDashboardStatsUseCase(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userId: String): Result<DashboardStats> = runCatching {
        if (userId.isEmpty()) throw IllegalArgumentException("User ID is empty")

        // 1. Fetch profile first to check name
        val profileResult = userRepository.getProfile(userId)
        val profile = profileResult.getOrThrow()

        var currentDisplayName = "Student"
        if (profile != null) {
            val displayEmail = profile.email ?: "Student"
            currentDisplayName = profile.display_name ?: displayEmail.substringBefore("@")
        }

        // 2. Fetch remaining data in parallel
        coroutineScope {
            val messagesDeferred = async { chatRepository.getLegacyMessages().getOrDefault(emptyList()) }
            val bookmarksDeferred = async { userRepository.getBookmarks(userId).getOrDefault(emptyList()) }
            val quizzesDeferred = async { userRepository.getQuizzes(userId).getOrDefault(emptyList()) }

            val messages = messagesDeferred.await()
            val bookmarks = bookmarksDeferred.await()
            val quizzes = quizzesDeferred.await()

            val totalQueries = messages.size
            val libraryItems = bookmarks.size

            var mathCount = 0
            var itCount = 0
            var scienceCount = 0
            messages.forEach { msg ->
                val sub = msg.subject?.lowercase()
                if (sub == "math") mathCount++
                else if (sub == "it" || sub == "programming") itCount++
                else if (sub == "science" || sub == "chemistry" || sub == "biology" || sub == "physics") scienceCount++
            }

            val uniqueDates = messages.mapNotNull { msg ->
                msg.created_at?.take(10)
            }.distinct()

            var streak = 0
            val today = LocalDate.now().toString()
            val yesterday = LocalDate.now().minusDays(1).toString()

            if (uniqueDates.contains(today) || uniqueDates.contains(yesterday)) {
                var checkDate = if (uniqueDates.contains(today)) LocalDate.now() else LocalDate.now().minusDays(1)
                while (uniqueDates.contains(checkDate.toString())) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                }
            }

            val totalXp = totalQueries * 10 + libraryItems * 20
            val level = (totalXp / 100) + 1
            val xp = totalXp % 100

            val totalSubjects = mathCount + itCount + scienceCount

            DashboardStats(
                displayName = currentDisplayName,
                totalQueries = totalQueries,
                libraryItems = libraryItems,
                streak = streak,
                studyTime = String.format(java.util.Locale.US, "%.1f", totalQueries * 0.15),
                level = level,
                xp = xp,
                mathPct = if (totalSubjects > 0) Math.round((mathCount.toFloat() / totalSubjects) * 100) else 0,
                itPct = if (totalSubjects > 0) Math.round((itCount.toFloat() / totalSubjects) * 100) else 0,
                sciencePct = if (totalSubjects > 0) Math.round((scienceCount.toFloat() / totalSubjects) * 100) else 100,
                quizzes = quizzes
            )
        }
    }
}
