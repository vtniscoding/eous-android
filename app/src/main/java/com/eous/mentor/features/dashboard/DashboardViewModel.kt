package com.eous.mentor.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.RepositoryProvider
import com.eous.mentor.domain.usecase.auth.LogoutUseCase
import com.eous.mentor.domain.usecase.dashboard.GetDashboardStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userId: String,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase = GetDashboardStatsUseCase(RepositoryProvider.userRepository, RepositoryProvider.chatRepository),
    private val logoutUseCase: LogoutUseCase = LogoutUseCase(RepositoryProvider.authRepository)
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        // Set initial display name from cached local session to prevent flickering
        val cachedEmail = RepositoryProvider.sessionRepository.getCurrentUserEmail()
        if (!cachedEmail.isNullOrEmpty()) {
            val initialName = cachedEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            _state.update {
                it.copy(stats = it.stats.copy(displayName = initialName))
            }
        }
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        if (userId.isEmpty()) {
            _state.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getDashboardStatsUseCase(userId)
                .onSuccess { fetchedStats ->
                    _state.update { it.copy(stats = fetchedStats, isLoading = false) }
                }
                .onFailure { e ->
                    e.printStackTrace()
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun logout(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
                .onSuccess {
                    _state.update { it.copy(isLoggedOut = true) }
                    onSuccess()
                }
                .onFailure { e ->
                    e.printStackTrace()
                    onError()
                }
        }
    }
}
