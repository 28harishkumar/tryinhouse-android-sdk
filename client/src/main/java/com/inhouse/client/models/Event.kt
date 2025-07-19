package com.inhouse.client.models

data class Event (
    val eventType: String,
    val projectId: String,
    val projectToken: String,
    val shortLink: String? = null,
    val deepLink: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String,
    val sessionId: String,
    val extra: Map<String, Any>? = null,
    val userAgent: String? = null,
    val ipAddress: String? = null
)