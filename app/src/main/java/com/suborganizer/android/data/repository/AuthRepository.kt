package com.suborganizer.android.data.repository

import com.suborganizer.android.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

class AuthRepository {
    private val auth = SupabaseClientProvider.client.auth

    val sessionStatus: StateFlow<SessionStatus> get() = auth.sessionStatus

    val currentUserId: String? get() = auth.currentUserOrNull()?.id
    val currentUserEmail: String? get() = auth.currentUserOrNull()?.email

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun requestPasswordReset(email: String) {
        auth.resetPasswordForEmail(email)
    }
}
