package co.tryinhouse.android

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import co.tryinhouse.android.models.Event
import co.tryinhouse.android.models.SDKConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.TimeUnit

class NetworkClient(private val config: SDKConfig) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun sendEvent(event: Event, shortLink: String? = null): String = withContext(Dispatchers.IO) {
        try {
            Log.d("TrackingSDK", "sendEvent called with eventType=${event.eventType}")

            // Route install event to fingerprint endpoint, others to capture endpoint
            return@withContext if (event.eventType == "app_install") {
                sendInstallEvent(event, shortLink)
            } else {
                sendCaptureEvent(event)
            }
        } catch (e: Exception) {
            Log.e("TrackingSDK", "Network error sending event", e)
            "{\"status\":\"error\",\"message\":\"${e.message}\"}"
        }
    }

    private fun buildUserAgent(): String = "TrackingSDK/1.0"

    private fun buildInstallBody(event: Event): String {
        // Body expected by install fingerprint endpoint: { event_type, extra, referrer }
        val extra: MutableMap<String, Any?> = mutableMapOf()
        event.extra?.forEach { (k, v) -> extra[k] = v }
        if (!extra.containsKey("user_agent") && event.userAgent != null) extra["user_agent"] = event.userAgent
        if (!extra.containsKey("device_id")) extra["device_id"] = event.deviceId
        if (!extra.containsKey("session_id")) extra["session_id"] = event.sessionId
        if (!extra.containsKey("timestamp")) extra["timestamp"] = event.timestamp
        if (!extra.containsKey("shortlink") && event.shortLink != null) extra["shortlink"] = event.shortLink

        val bodyMap = mutableMapOf(
            "event_type" to event.eventType,
            "extra" to extra
        )
        
        // Add referrer field if available
        if (event.referrer != null) {
            bodyMap["referrer"] = event.referrer
        }
        
        return gson.toJson(bodyMap)
    }

    private fun buildCaptureBody(event: Event): String {
        // Body expected by capture endpoint: { batch: [events.Event] }
        val captureEvent: MutableMap<String, Any?> = mutableMapOf(
            "event" to event.eventType,
            "fingerprint" to "",
            "user_id" to "",
            "anonymous_id" to (event.deviceId ?: ""),
            "properties" to mapOf(
                "shortlink" to (event.shortLink ?: ""),
                "deep_link" to (event.deepLink ?: "")
            ),
            "system" to mapOf(
                "ip_address" to (event.ipAddress ?: ""),
                "user_agent" to (event.userAgent ?: buildUserAgent()),
                "collected_at" to (event.timestamp),
                "session_id" to (event.sessionId)
            ),
            "type" to "mobile",
            "version" to "1",
            "writeKey" to ""
        )
        return gson.toJson(mapOf("batch" to listOf(captureEvent)))
    }

    private fun sendInstallEvent(event: Event, shortLink: String?): String {
        val jsonBody = buildInstallBody(event)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val builtUrl = "${config.serverUrl}/v1/api/events/fingerprint/"
            .toHttpUrlOrNull()
            ?.newBuilder()
            ?.addQueryParameter("shortlink", shortLink ?: "")
            ?.build()

        if (builtUrl == null) {
            Log.e("TrackingSDK", "Failed to build URL for fingerprint install event")
            return "{\"status\":\"error\",\"message\":\"Invalid URL\"}"
        }

        Log.d("TrackingSDK", "Sending install event to $builtUrl with body: $jsonBody")
        val request = Request.Builder()
            .url(builtUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", buildUserAgent())
            // New authentication headers expected by Go backend middleware
            .addHeader("x-api-token-id", config.tokenId)
            .addHeader("x-api-token-secret", config.projectToken)
            .addHeader("x-inhouse-app", "mobile")
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        Log.d("TrackingSDK", "Received response: $responseBody with code: ${response.code}")
        return responseBody
    }

    private fun sendCaptureEvent(event: Event): String {
        val jsonBody = buildCaptureBody(event)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val builtUrl = "${config.serverUrl}/v1/api/events/track/"
            .toHttpUrlOrNull()
            ?.newBuilder()
            ?.build()

        if (builtUrl == null) {
            Log.e("TrackingSDK", "Failed to build URL for capture event")
            return "{\"status\":\"error\",\"message\":\"Invalid URL\"}"
        }

        Log.d("TrackingSDK", "Sending capture event to $builtUrl with body: $jsonBody")
        val request = Request.Builder()
            .url(builtUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", buildUserAgent())
            // New authentication headers expected by Go backend middleware
            .addHeader("x-api-token-id", config.tokenId)
            .addHeader("x-api-token-secret", config.projectToken)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: "{}"
        Log.d("TrackingSDK", "Received response: $responseBody with code: ${response.code}")
        return responseBody
    }

    suspend fun getInstallData(shortLink: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            Log.d("TrackingSDK", "getInstallData called with shortLink=$shortLink")
            val url = "${config.serverUrl}/install-data"
            val builtUrl = url.toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("shortlink", shortLink)
                ?.build()
                ?: throw IllegalArgumentException("Invalid URL")

            Log.d("TrackingSDK", "Requesting install data from $builtUrl")
            val request = Request.Builder()
                .url(builtUrl)
                .get()
                .addHeader("User-Agent", buildUserAgent())
                // New authentication headers expected by Go backend middleware
                .addHeader("x-api-token-id", config.tokenId)
                .addHeader("x-api-token-secret", config.projectToken)
                .addHeader("x-inhouse-app", "mobile")
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonResponse = response.body?.string() ?: "{}"
                Log.d("TrackingSDK", "Install data response: $jsonResponse")
                val type = object : TypeToken<Map<String, String>>() {}.type
                gson.fromJson<Map<String, String>>(jsonResponse, type) ?: emptyMap()
            } else {
                Log.e("TrackingSDK", "Failed to get install data: ${response.code}")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("TrackingSDK", "Error getting install data", e)
            emptyMap()
        }
    }
}