package co.tryinhouse.android.models

data class SDKConfig(
    val projectToken: String,
    val tokenId: String, // Added for new API authentication
    val shortLinkDomain: String,
    val serverUrl: String = "https://your-api-server.com",
    val enableDebugLogging: Boolean = false,
    val sessionTimeoutMinutes: Int = 30,
    val maxRetryAttempts: Int = 3
)
