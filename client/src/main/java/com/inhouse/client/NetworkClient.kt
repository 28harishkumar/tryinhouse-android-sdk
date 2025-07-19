package com.inhouse.client

import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.inhouse.client.models.Event
import com.inhouse.client.models.SDKConfig
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

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    suspend fun sendEvent(event: Event): String = withContext(Dispatchers.IO) {
        try {
            Log.d("TrackingSDK", "sendEvent called with eventType=${event.eventType}, projectId=${event.projectId}, projectToken=${event.projectToken}")
            val jsonBody = gson.toJson(event)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val urlBuilder = "${config.serverUrl}/api/clicks/register_event"
                .toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("project_id", event.projectId ?: "")
                ?.addQueryParameter("project_token", event.projectToken ?: "")
                ?.addQueryParameter("shortlink", event.shortLink ?: "")
                ?.build()

            if (urlBuilder == null) {
                Log.e("TrackingSDK", "Failed to build URL for event registration")
                return@withContext "{\"status\":\"error\",\"message\":\"Invalid URL\"}"
            }

            Log.d("TrackingSDK", "Sending event to $urlBuilder with body: $jsonBody")
            val request = Request.Builder()
                .url(urlBuilder)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "TrackingSDK/1.0")
                .build()

            Log.d("TrackingSDK", "Sending event to ${config.serverUrl}/api/clicks/register_event with body: $jsonBody")
            val response = httpClient.newCall(request).execute()

            val responseBody = response.body?.string() ?: "{}"
            Log.d("TrackingSDK", "Received response: $responseBody with code: ${response.code}")
            if (response.isSuccessful) {
                if (config.enableDebugLogging) {
                    Log.d("TrackingSDK", "Event sent successfully: ${event.eventType}")
                }
                responseBody
            } else {
                Log.e("TrackingSDK", "Failed to send event: ${response.code}")
                responseBody
            }
        } catch (e: Exception) {
            Log.e("TrackingSDK", "Network error sending event", e)
            "{\"status\":\"error\",\"message\":\"${e.message}\"}"
        }
    }

    suspend fun getInstallData(shortLink: String): Map<String, String> = withContext(Dispatchers.IO) {
        try {
            Log.d("TrackingSDK", "getInstallData called with shortLink=$shortLink")
            val url = "${config.serverUrl}/install-data"
            val builtUrl = url.toHttpUrlOrNull()
                ?.newBuilder()
                ?.addQueryParameter("shortlink", shortLink)
                ?.addQueryParameter("project_id", config.projectId)
                ?.addQueryParameter("project_token", config.projectToken)
                ?.build()
                ?: throw IllegalArgumentException("Invalid URL")

            Log.d("TrackingSDK", "Requesting install data from $builtUrl")
            val request = Request.Builder()
                .url(builtUrl)
                .get()
                .addHeader("User-Agent", "TrackingSDK/1.0")
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