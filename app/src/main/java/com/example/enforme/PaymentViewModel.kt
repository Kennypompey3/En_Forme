package com.example.enforme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
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

// -------------------- UI State --------------------
sealed class PaymentUiState {
    object Idle : PaymentUiState()
    object Loading : PaymentUiState()

    data class Initiated(
        val transactionRef: String,
        val provider: String?,
        val status: String?,
        val reference: String,
        val accountReference: String?,
        val subscriptionId: Long?,
        val existingSubscription: Boolean?
    ) : PaymentUiState()

    data class Error(val message: String) : PaymentUiState()
}

// -------------------- ViewModel --------------------
class PaymentViewModel : ViewModel() {

    companion object {
        /**
         * Real phone on same Wi-Fi:
         * Use your PC's LAN IP (the machine running Node), not "localhost".
         * Example: "http://192.168.1.6:8080"
         */
        private const val BACKEND_BASE_URL = "http://192.168.1.6:8080"
    }

    private val client = OkHttpClient()
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private val _uiState = MutableStateFlow<PaymentUiState>(PaymentUiState.Idle)
    val uiState: StateFlow<PaymentUiState> = _uiState

    /**
     * Starts a subscription setup on your Node backend, which calls OnePipe.
     *
     * Required:
     * - planCode
     * - accountNumber
     * - phoneNumber
     * - bankCode (CBN code as string e.g. "011")
     */
    fun startSubscription(
        planCode: String,
        accountNumber: String,
        phoneNumber: String,
        bankCode: String
    ) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _uiState.value = PaymentUiState.Error("User not signed in")
                return@launch
            }

            if (planCode.isBlank()) {
                _uiState.value = PaymentUiState.Error("Missing plan code")
                return@launch
            }

            if (accountNumber.isBlank() || phoneNumber.isBlank() || bankCode.isBlank()) {
                _uiState.value = PaymentUiState.Error(
                    "Please set Bank, Account Number, and Phone Number in Account tab first."
                )
                return@launch
            }

            _uiState.value = PaymentUiState.Loading

            try {
                val url = "$BACKEND_BASE_URL/subscriptions/start"

                val displayName = user.displayName?.trim().orEmpty()
                val parts = displayName.split(" ", limit = 2)
                val firstName = parts.getOrNull(0)?.ifBlank { "EnForme" } ?: "EnForme"
                val lastName = parts.getOrNull(1)?.ifBlank { "User" } ?: "User"

                val json = """
                {
                  "planCode": "$planCode",
                  "userId": "${user.uid}",
                  "userEmail": "${user.email ?: ""}",
                  "userFirstName": "$firstName",
                  "userLastName": "$lastName",
                  "accountNumber": "$accountNumber",
                  "phoneNumber": "$phoneNumber",
                  "bankCode": "$bankCode"
                }
                """.trimIndent()

                Log.d("PaymentViewModel", "POST $url")
                Log.d("PaymentViewModel", "Body: $json")

                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                val body = response.body?.string().orEmpty()
                Log.d("PaymentViewModel", "HTTP ${response.code} $body")

                if (!response.isSuccessful) {
                    _uiState.value = PaymentUiState.Error("Backend error: ${response.code} - $body")
                    return@launch
                }

                val obj = JSONObject(body)

                val ok = obj.optBoolean("ok", false)
                if (!ok) {
                    val err = obj.optString("error", "Subscription could not be initiated")
                    _uiState.value = PaymentUiState.Error(err)
                    return@launch
                }

                val transactionRef = obj.optString("transactionRef", "")
                val provider = obj.optString("provider", null)
                val status = obj.optString("status", null)
                val reference = obj.optString("reference", "")
                val accountReference = obj.optString("accountReference", null)

                val subscriptionId: Long? =
                    if (obj.has("subscriptionId") && !obj.isNull("subscriptionId")) obj.optLong("subscriptionId")
                    else null

                val existingSubscription: Boolean? =
                    if (obj.has("existingSubscription") && !obj.isNull("existingSubscription")) obj.optBoolean("existingSubscription")
                    else null

                if (reference.isBlank()) {
                    _uiState.value = PaymentUiState.Error("No reference returned by backend.")
                    return@launch
                }

                // Save subscription info to Firestore so it follows user across devices
                saveSubscriptionToFirestore(
                    uid = user.uid,
                    planCode = planCode,
                    bankCode = bankCode,
                    accountNumberLast4 = accountNumber.takeLast(4),
                    phoneNumber = phoneNumber,
                    transactionRef = transactionRef,
                    provider = provider,
                    status = status,
                    reference = reference,
                    accountReference = accountReference,
                    subscriptionId = subscriptionId,
                    existingSubscription = existingSubscription
                )

                _uiState.value = PaymentUiState.Initiated(
                    transactionRef = transactionRef,
                    provider = provider,
                    status = status,
                    reference = reference,
                    accountReference = accountReference,
                    subscriptionId = subscriptionId,
                    existingSubscription = existingSubscription
                )
            } catch (e: Exception) {
                _uiState.value = PaymentUiState.Error(e.message ?: "Network/parse error")
            }
        }
    }

    private suspend fun saveSubscriptionToFirestore(
        uid: String,
        planCode: String,
        bankCode: String,
        accountNumberLast4: String,
        phoneNumber: String,
        transactionRef: String,
        provider: String?,
        status: String?,
        reference: String,
        accountReference: String?,
        subscriptionId: Long?,
        existingSubscription: Boolean?
    ) {
        withContext(Dispatchers.IO) {
            val data = hashMapOf(
                "subscription" to hashMapOf(
                    "planCode" to planCode,
                    "bankCode" to bankCode,
                    "accountNumberLast4" to accountNumberLast4,
                    "phoneNumber" to phoneNumber,
                    "transactionRef" to transactionRef,
                    "provider" to provider,
                    "status" to status,
                    "reference" to reference,
                    "accountReference" to accountReference,
                    "subscriptionId" to subscriptionId,
                    "existingSubscription" to existingSubscription,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
            )

            db.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnFailureListener { ex ->
                    Log.e("PaymentViewModel", "Failed saving subscription to Firestore: ${ex.message}")
                }
        }
    }

    fun reset() {
        _uiState.value = PaymentUiState.Idle
    }
}
