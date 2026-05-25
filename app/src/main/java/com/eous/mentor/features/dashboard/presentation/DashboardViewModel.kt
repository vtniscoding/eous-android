package com.eous.mentor.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.core.di.supabase
import com.eous.mentor.data.model.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DashboardViewModel(private val userId: String) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        if (userId.isEmpty()) {
            _state.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // 1. Fetch profile first to get the name immediately
                val profile = withContext(Dispatchers.IO) {
                    try {
                        supabase.from("profiles")
                            .select {
                                filter {
                                    eq("id", userId)
                                }
                            }
                            .decodeSingleOrNull<Profile>()
                    } catch (e: Exception) {
                        null
                    }
                }

                var currentDisplayName = "Student"
                if (profile != null) {
                    val displayEmail = profile.email ?: "Student"
                    currentDisplayName = profile.display_name ?: displayEmail.substringBefore("@")
                    _state.update {
                        it.copy(stats = it.stats.copy(displayName = currentDisplayName))
                    }
                }

                // 2. Fetch messages, bookmarks, and quizzes in parallel
                val fetchedStats = withContext(Dispatchers.IO) {
                    fetchStatsParallel(userId, currentDisplayName)
                }
                _state.update { it.copy(stats = fetchedStats, isLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _state.update { it.copy(isLoggedOut = true) }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }

    private suspend fun fetchStatsParallel(userId: String, displayName: String): DashboardStats = coroutineScope {
        // Fetch queries (messages), bookmarks, and quizzes concurrently
        val messagesDeferred = async {
            try {
                supabase.from("messages")
                    .select {
                        filter {
                            eq("role", "user")
                        }
                    }
                    .decodeList<Message>()
            } catch (e: Exception) {
                emptyList()
            }
        }

        val bookmarksDeferred = async {
            try {
                supabase.from("bookmarks")
                    .select()
                    .decodeList<Bookmark>()
            } catch (e: Exception) {
                emptyList()
            }
        }

        val quizzesDeferred = async {
            try {
                supabase.from("quizzes")
                    .select()
                    .decodeList<Quiz>()
            } catch (e: Exception) {
                emptyList()
            }
        }

        val messages = messagesDeferred.await()
        val bookmarks = bookmarksDeferred.await()
        val quizzes = quizzesDeferred.await()

        // Process stats
        val totalQueries = messages.size
        val libraryItems = bookmarks.size

        // Calculate subject focus
        var mathCount = 0
        var itCount = 0
        var scienceCount = 0
        messages.forEach { msg ->
            val sub = msg.subject?.lowercase()
            if (sub == "math") mathCount++
            else if (sub == "it" || sub == "programming") itCount++
            else if (sub == "science" || sub == "chemistry" || sub == "biology" || sub == "physics") scienceCount++
        }

        // Calculate streak from unique message dates
        val uniqueDates = messages.mapNotNull { msg ->
            msg.created_at?.take(10) // Format: YYYY-MM-DD
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
            displayName = displayName,
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
