package co.tryinhouse.android.models

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class Event (
    @SerializedName("event_type")
    val eventType: String,
    @SerializedName("project_id")
    val projectId: String,
    @SerializedName("project_token")
    val projectToken: String,
    @SerializedName("shortlink")
    val shortLink: String? = null,
    @SerializedName("deep_link")
    val deepLink: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("session_id")
    val sessionId: String,
    @SerializedName("extra")
    val extra: Map<String, Any>? = null,
    @SerializedName("user_agent")
    val userAgent: String? = null,
    @SerializedName("ip_address")
    val ipAddress: String? = null
)