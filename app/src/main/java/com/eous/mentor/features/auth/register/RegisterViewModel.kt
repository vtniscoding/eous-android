package com.eous.mentor.features.auth.register

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

class RegisterViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, error = null) }
    }

    fun onTogglePasswordVisibility() {
        _state.update { it.copy(isPasswordVisible = !_state.value.isPasswordVisible) }
    }

    fun onToggleConfirmPasswordVisibility() {
        _state.update { it.copy(isConfirmPasswordVisible = !_state.value.isConfirmPasswordVisible) }
    }

    fun onRegister(onSuccess: () -> Unit) {
        val currentState = _state.value
        val strength = getPasswordStrength(currentState.password)

        if (currentState.email.isEmpty() || currentState.password.isEmpty() || currentState.confirmPassword.isEmpty()) {
            _state.update { it.copy(error = "All fields are required.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _state.update { it.copy(error = "Please enter a valid email address.") }
            return
        }
        if (currentState.password.length < 8) {
            _state.update { it.copy(error = "Password must be at least 8 characters long.") }
            return
        }
        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match.") }
            return
        }
        if (strength < 2) {
            _state.update { it.copy(error = "Password is too weak. Please use digits or mix cases.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                supabase.auth.signUpWith(Email) {
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
