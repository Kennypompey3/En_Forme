package com.example.enforme

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Very small API client that calls YOUR backend.
 *
 * Your backend then talks to OnePipe and returns an authorizationUrl.
 *
 * Expected backend endpoint:
 *   POST {baseUrl}/subscriptions/start
 * Body:
 *   { "planCode": "singles" }   // example
 * Response:
 *   { "authorizationUrl": "https://...", "reference": "optional" }
 */
class SubscriptionApi(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    data class StartSubscriptionResult(
        val authorizationUrl: String,
        val reference: String? = null
    )

    fun startSubscription(planCode: String): StartSubscriptionResult {
        val json = JSONObject()
            .put("planCode", planCode)
            .toString()

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/subscriptions/start")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                throw IllegalStateException("Backend error ${response.code}: $raw")
            }

            val obj = JSONObject(raw)
            val authorizationUrl = obj.getString("authorizationUrl")
            val reference = if (obj.has("reference")) obj.optString("reference") else null

            return StartSubscriptionResult(
                authorizationUrl = authorizationUrl,
                reference = reference
            )
        }
    }
}