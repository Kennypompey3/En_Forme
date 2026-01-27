package com.example.enforme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()
    data class NeedsAuthorization(val authorizationUrl: String) : PaymentUiState()
    data class Error(val message: String) : PaymentUiState()
}

class PaymentViewModel : ViewModel() {
    private val client = OkHttpClient()
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState

    // This function must accept the accountNumber from the UI
    fun startSubscription(planCode: String, accountNumber: String, phoneNumber: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _uiState.value = PaymentUiState.Error("User not signed in")
                return@launch
            }

            _uiState.value = PaymentUiState.Loading

            try {
                val url = "http://192.168.1.6:8080/subscriptions/start"

                val displayName = user.displayName ?: "EnForme User"
                val names = displayName.split(" ", limit = 2)
                val firstName = names.getOrNull(0) ?: "EnForme"
                val lastName = names.getOrNull(1) ?: "User"

                // Construct the JSON request body with the real phone number
                val json = """
                {
                    "planCode": "$planCode",
                    "userId": "${user.uid}",
                    "userEmail": "${user.email ?: ""}",
                    "userFirstName": "$firstName",
                    "userLastName": "$lastName",
                    "accountNumber": "$accountNumber",
                    "phoneNumber": "$phoneNumber"
                }
            """.trimIndent()

                Log.d("PaymentViewModel", "Sending JSON: $json")

                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                // The rest of the network call logic...
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    val authorizationUrl = jsonObject.getString("authorizationUrl")
                    _uiState.value = PaymentUiState.NeedsAuthorization(authorizationUrl)
                } else {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    _uiState.value = PaymentUiState.Error("Backend error: ${response.code} - $errorBody")
                }
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Network or parsing error")
            }
        }
    }

    fun reset() {
        _uiState.value = PaymentUiState.Idle
    }
}