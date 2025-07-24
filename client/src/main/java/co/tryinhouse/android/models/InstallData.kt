package co.tryinhouse.android.models

data class InstallData(
    val shortLink: String,
    val keyValuePairs: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)
