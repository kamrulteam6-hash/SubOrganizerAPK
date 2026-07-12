package com.suborganizer.android.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suborganizer.android.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val isSignUpMode: Boolean = false,
    val loggedIn: Boolean = false,
)

// No-arg constructor deliberately, not a default-valued parameter: Compose's viewModel()
// instantiates this via reflection requiring a real zero-arg constructor, which a Kotlin
// default parameter value does NOT produce at the bytecode level.
class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun toggleMode() {
        _state.value = _state.value.copy(isSignUpMode = !_state.value.isSignUpMode, error = null, info = null)
    }

    fun submit(email: String, password: String) {
        if (email.isBlank() || password.length < 6) {
            _state.value = _state.value.copy(error = "Enter a valid email and a password of at least 6 characters.")
            return
        }
        _state.value = _state.value.copy(loading = true, error = null, info = null)
        viewModelScope.launch {
            try {
                if (_state.value.isSignUpMode) {
                    authRepository.signUp(email, password)
                    if (authRepository.currentUserId != null) {
                        _state.value = _state.value.copy(loading = false, loggedIn = true)
                    } else {
                        _state.value = _state.value.copy(
                            loading = false,
                            isSignUpMode = false,
                            info = "Account created! If sign-in fails, confirm email verification is disabled in Supabase, then log in.",
                        )
                    }
                } else {
                    authRepository.signIn(email, password)
                    _state.value = _state.value.copy(loading = false, loggedIn = true)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message ?: "Something went wrong.")
            }
        }
    }
}
