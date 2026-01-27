package com.example.enforme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val bankAccountNumber: String = "",
    val phoneNumber: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class ProfileViewModel(private val authViewModel: AuthViewModel) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val firebaseUser = Firebase.auth.currentUser
        firebaseUser?.let {
            _uiState.update { currentState ->
                currentState.copy(
                    displayName = it.displayName ?: "",
                    email = it.email ?: ""
                    // TODO: Load bank account number from a secure source like Firestore
                )
            }
        }
    }

    fun onDisplayNameChange(newName: String) {
        _uiState.update { it.copy(displayName = newName) }
    }

    fun onBankAccountChange(newNumber: String) {
        // Basic validation: only allow digits and limit length
        if (newNumber.all { it.isDigit() } && newNumber.length <= 10) {
            _uiState.update { it.copy(bankAccountNumber = newNumber) }
        }
    }

    fun onPhoneNumberChange(newNumber: String) {
        // Basic validation: only allow digits and limit length
        if (newNumber.all { it.isDigit() } && newNumber.length <= 11) {
            _uiState.update { it.copy(phoneNumber = newNumber) }
        }
    }

    fun onToggleEditMode() {
        val isCurrentlyEditing = _uiState.value.isEditing
        if (isCurrentlyEditing) {
            // If we were editing, now we save
            saveUserProfile()
        } else {
            // If we were not editing, just enter edit mode
            _uiState.update { it.copy(isEditing = true) }
        }
    }

    private fun saveUserProfile() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val success = authViewModel.updateProfile(
                displayName = _uiState.value.displayName
            )
            if (success) {
                // TODO: Save bank account number to a secure backend/Firestore
                // For now, we just pretend it's saved.
                _uiState.update {
                    it.copy(isEditing = false, isLoading = false, userMessage = "Profile saved!")
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, userMessage = "Failed to save profile.")
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}