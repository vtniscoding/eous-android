package com.eous.mentor.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eous.mentor.di.supabase
import com.eous.mentor.core.util.friendlyAuthError
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !_state.value.isPasswordVisible) }
    }

    fun onLogin(onSuccess: () -> Unit) {
        val currentState = _state.value
        if (currentState.email.isEmpty() || currentState.password.isEmpty()) {
            _state.update { it.copy(error = "Email and password are required.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Please enter a valid email address.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.signInWith(Email) {
                    this.email = currentState.email
                    this.password = currentState.password
                }
                onSuccess()
            } catch (e: Throwable) {
                _state.update { it.copy(error = friendlyAuthError(e)) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
