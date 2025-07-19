package com.inhouse.client.models

data class SDKConfig(
    val projectId: String,
    val projectToken: String,
    val shortLinkDomain: String,
    val serverUrl: String = "https://your-api-server.com",
    val enableDebugLogging: Boolean = false,
    val sessionTimeoutMinutes: Int = 30,
    val maxRetryAttempts: Int = 3
)
