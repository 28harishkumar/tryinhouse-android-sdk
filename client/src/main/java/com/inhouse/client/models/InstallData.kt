package com.inhouse.client.models

data class InstallData(
    val shortLink: String,
    val keyValuePairs: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)
