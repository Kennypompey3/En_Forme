package com.example.enforme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.ktx.userProfileChangeRequest

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState

    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign up failed")
                    }
                }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        _authState.value = AuthState.Error(task.exception?.message ?: "Sign in failed")
                    }
                }
        }
    }

    suspend fun updateProfile(displayName: String): Boolean {
        val user = auth.currentUser
        return if (user != null) {
            try {
                // Create a profile change request using the KTX builder
                val profileUpdates = userProfileChangeRequest {
                    this.displayName = displayName
                }
                // Await the completion of the update task
                user.updateProfile(profileUpdates).await()
                true // Return true if the await() call completes without an exception
            } catch (e: Exception) {
                // Log the exception or handle the error as needed
                // For example: Log.e("AuthViewModel", "Failed to update profile", e)
                false // Return false if an exception occurs
            }
        } else {
            false // Return false if there is no logged-in user
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}
