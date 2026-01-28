package com.example.enforme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val displayName: String = "",
    val email: String = "",
    val bankName: String = "",
    val bankCode: String = "",
    val bankAccountNumber: String = "",
    val phoneNumber: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)

class ProfileViewModel(private val authViewModel: AuthViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val db = Firebase.firestore

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val user = Firebase.auth.currentUser ?: return

        _uiState.update {
            it.copy(
                displayName = user.displayName.orEmpty(),
                email = user.email.orEmpty(),
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(user.uid).get().await()

                val bank = doc.getString("bankAccountNumber").orEmpty()
                val phone = doc.getString("phoneNumber").orEmpty()
                val bankName = doc.getString("bankName").orEmpty()
                val bankCode = doc.getString("bankCode").orEmpty()

                _uiState.update {
                    it.copy(
                        bankAccountNumber = bank,
                        phoneNumber = phone,
                        bankName = bankName,
                        bankCode = bankCode,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Could not load profile from cloud."
                    )
                }
            }
        }
    }

    fun onDisplayNameChange(newName: String) {
        _uiState.update { it.copy(displayName = newName) }
    }

    fun onBankAccountChange(newNumber: String) {
        if (newNumber.all { it.isDigit() } && newNumber.length <= 10) {
            _uiState.update { it.copy(bankAccountNumber = newNumber) }
        }
    }

    fun onPhoneNumberChange(newNumber: String) {
        if (newNumber.all { it.isDigit() } && newNumber.length <= 11) {
            _uiState.update { it.copy(phoneNumber = newNumber) }
        }
    }

    fun onBankSelected(name: String, code: String) {
        _uiState.update { it.copy(bankName = name, bankCode = code) }
    }

    fun onToggleEditMode() {
        if (_uiState.value.isEditing) {
            saveUserProfile()
        } else {
            _uiState.update { it.copy(isEditing = true) }
        }
    }

    private fun saveUserProfile() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(userMessage = "Not signed in.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Update Firebase Auth display name using your existing AuthViewModel method
                val ok = authViewModel.updateProfile(displayName = _uiState.value.displayName)
                if (!ok) {
                    _uiState.update {
                        it.copy(isLoading = false, userMessage = "Failed to save profile name.")
                    }
                    return@launch
                }

                // Save to Firestore
                val data = mapOf(
                    "displayName" to _uiState.value.displayName,
                    "email" to (user.email ?: ""),
                    "bankAccountNumber" to _uiState.value.bankAccountNumber,
                    "phoneNumber" to _uiState.value.phoneNumber,
                    "bankName" to _uiState.value.bankName,
                    "bankCode" to _uiState.value.bankCode,
                    "updatedAt" to Timestamp.now()
                )

                db.collection("users")
                    .document(user.uid)
                    .set(data, SetOptions.merge())
                    .await()

                _uiState.update {
                    it.copy(
                        isEditing = false,
                        isLoading = false,
                        userMessage = "Profile saved!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Failed to save to cloud."
                    )
                }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
