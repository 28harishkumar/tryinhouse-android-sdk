package co.tryinhouse.android

import android.util.Log
import co.tryinhouse.android.models.Event
import co.tryinhouse.android.models.InstallData
import co.tryinhouse.android.models.SDKConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.thumbmarkjs.thumbmark_android.Thumbmark

class EventTracker(
    private val networkClient: NetworkClient,
    private val storageManager: StorageManager,
    private val config: SDKConfig
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun trackEvent(eventType: String, shortLink: String? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackEvent called with eventType=$eventType, shortLink=$shortLink")
        coroutineScope.launch {
            val event = createEvent(eventType, shortLink)
            Log.d("TrackingSDK", "Event created: $event")
            sendEvent(event, shortLink) { responseJson ->
                Log.d("TrackingSDK", "trackEvent callback: $responseJson")
                callback?.invoke(responseJson)
            }
        }
    }

    fun trackShortLinkClick(shortLink: String, deepLink: String?, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackShortLinkClick called with shortLink=$shortLink, deepLink=$deepLink")
        coroutineScope.launch {
            val event = createEvent("short_link_click", shortLink, deepLink)
            Log.d("TrackingSDK", "Event created: $event")
            sendEvent(event, shortLink) { responseJson ->
                Log.d("TrackingSDK", "trackShortLinkClick callback: $responseJson")
                callback?.invoke(responseJson)
            }
        }
    }

    fun trackAppInstall(shortLink: String, referrer: String? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppInstall called with shortLink=$shortLink, referrer=$referrer")
        coroutineScope.launch {
            try {
                // First, get key-value pairs from server
                val installData = networkClient.getInstallData(shortLink)
                Log.d("TrackingSDK", "Install data received: $installData")

                // Store the key-value pairs
                storageManager.storeInstallData(InstallData(shortLink, installData))

                // Track install event
                val event = createEvent("app_install", shortLink, additionalData = installData, referrer = referrer)
                Log.d("TrackingSDK", "Event created: $event")
                sendEvent(event, shortLink) { responseJson ->
                    Log.d("TrackingSDK", "trackAppInstall callback: $responseJson")
                    callback?.invoke(responseJson)
                }

                if (config.enableDebugLogging) {
                    Log.d("TrackingSDK", "App install tracked with data: $installData")
                }
            } catch (e: Exception) {
                Log.e("TrackingSDK", "Error tracking app install", e)
            }
        }
    }

    fun trackAppInstallReferrerOnly(referrer: String, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppInstallReferrerOnly called with referrer=$referrer")
        coroutineScope.launch {
            try {
                // Track install event with referrer only, no shortlink extraction
                val event = createEvent("app_install", shortLink = null, additionalData = null, referrer = referrer)
                Log.d("TrackingSDK", "Event created: $event")
                sendEvent(event, null) { responseJson ->
                    Log.d("TrackingSDK", "trackAppInstallReferrerOnly callback: $responseJson")
                    callback?.invoke(responseJson)
                }

                if (config.enableDebugLogging) {
                    Log.d("TrackingSDK", "App install tracked with referrer only: $referrer")
                }
            } catch (e: Exception) {
                Log.e("TrackingSDK", "Error tracking app install with referrer", e)
            }
        }
    }

    fun trackCustomEvent(eventType: String, shortLink: String?, additionalData: Map<String, String>?, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackCustomEvent called with eventType=$eventType, shortLink=$shortLink, additionalData=$additionalData")
        coroutineScope.launch {
            val event = createEvent(eventType, shortLink, additionalData = additionalData)
            Log.d("TrackingSDK", "Event created: $event")
            sendEvent(event, shortLink) { responseJson ->
                Log.d("TrackingSDK", "trackCustomEvent callback: $responseJson")
                callback?.invoke(responseJson)
            }
        }
    }

    private fun createEvent(
        eventType: String,
        shortLink: String? = null,
        deepLink: String? = null,
        additionalData: Map<String, String>? = null,
        referrer: String? = null
    ): Event {
        Log.d("TrackingSDK", "createEvent called with eventType=$eventType, shortLink=$shortLink, deepLink=$deepLink, additionalData=$additionalData, referrer=$referrer")
        val extra = mutableMapOf<String, Any>()

        // Device info
        extra["device"] = android.os.Build.DEVICE ?: ""
        extra["device_model"] = android.os.Build.MODEL ?: ""
        extra["device_vendor"] = android.os.Build.MANUFACTURER ?: ""
        extra["os"] = "Android"
        extra["os_version"] = android.os.Build.VERSION.RELEASE ?: ""
        extra["cpu_architecture"] = android.os.Build.SUPPORTED_ABIS?.firstOrNull() ?: ""
        extra["platform"] = "Android"
        extra["vendor"] = android.os.Build.BRAND ?: ""
        extra["hardware_concurrency"] = Runtime.getRuntime().availableProcessors()
        extra["screen_width"] = getScreenWidth()
        extra["screen_height"] = getScreenHeight()
        extra["language"] = java.util.Locale.getDefault().language
        extra["timezone"] = java.util.TimeZone.getDefault().id

        // User agent
        extra["ua"] = getUserAgent()

        // Location (if available, else empty)
        extra["country"] = ""
        extra["city"] = ""
        extra["region"] = ""
        extra["latitude"] = ""
        extra["longitude"] = ""
        extra["continent"] = ""

        // Browser info (if available, else empty)
        extra["browser"] = ""
        extra["browser_version"] = ""
        extra["engine"] = ""
        extra["engine_version"] = ""

        // Other fields
        extra["bot"] = false
        extra["referrer"] = ""
        extra["referrer_url"] = ""
        extra["identity_hash"] = ""
        extra["ip"] = getIPAddress() ?: ""
        extra["qr"] = false
        extra["max_touch_points"] = 0
        extra["cookie_enabled"] = false
        extra["do_not_track"] = ""
        extra["path"] = ""

        // Thumbmark fingerprinting
        try {
            val ctx = TrackingSDK.getInstance().getApplicationContext()
            if (ctx != null) {
                // Default SHA-256 id
                val id = Thumbmark.id(ctx)
                if (!id.isNullOrEmpty()) {
                    extra["thumbmark_id_sha256"] = id
                }
                // Full fingerprint
                val fingerprint = Thumbmark.fingerprint(ctx)
                fingerprint?.let {
                    val gson = com.google.gson.Gson()
                    val json = gson.toJson(it)
                    extra["thumbmark_fingerprint_json"] = json
                }
            }
        } catch (e: Throwable) {
            Log.w("TrackingSDK", "Thumbmark integration failed: ${e.message}")
        }

        // Merge in any additionalData
        additionalData?.let { extra.putAll(it) }

        val event = Event(
            eventType = eventType,
            shortLink = shortLink,
            deepLink = deepLink,
            deviceId = getDeviceId(),
            sessionId = TrackingSDK.getInstance().getSessionId(),
            extra = extra,
            userAgent = getUserAgent(),
            ipAddress = getIPAddress(),
            referrer = referrer
        )
        Log.d("TrackingSDK", "Event created: $event")
        return event
    }

    private suspend fun sendEvent(event: Event, shortLink: String?, callback: ((String) -> Unit)? = null) {
        try {
            Log.d("TrackingSDK", "sendEvent called with event: $event")
            val responseJson = networkClient.sendEvent(event, shortLink)
            if (config.enableDebugLogging) {
                Log.d("TrackingSDK", "Event sent: ${event.eventType}")
            }
            Log.d("TrackingSDK", "sendEvent response: $responseJson")
            callback?.invoke(responseJson)
        } catch (e: Exception) {
            Log.e("TrackingSDK", "Error sending event", e)
            storageManager.storeFailedEvent(event)
            callback?.invoke("{\"status\":\"error\",\"message\":\"${e.message}\"}")
        }
    }

    private fun getDeviceId(): String {
        return storageManager.getDeviceId()
    }

    private fun getUserAgent(): String {
        return System.getProperty("http.agent") ?: "Unknown"
    }

    private fun getIPAddress(): String? {
        // Implementation to get IP address
        return null
    }

    private fun getScreenWidth(): Int {
        // Use Resources if available, else return 0
        return try {
            val resources = TrackingSDK.getInstance().javaClass.classLoader?.loadClass("android.content.res.Resources")
            val context = TrackingSDK.getInstance().javaClass.getDeclaredField("context").apply { isAccessible = true }.get(TrackingSDK.getInstance()) as? android.content.Context
            context?.resources?.displayMetrics?.widthPixels ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun getScreenHeight(): Int {
        // Use Resources if available, else return 0
        return try {
            val resources = TrackingSDK.getInstance().javaClass.classLoader?.loadClass("android.content.res.Resources")
            val context = TrackingSDK.getInstance().javaClass.getDeclaredField("context").apply { isAccessible = true }.get(TrackingSDK.getInstance()) as? android.content.Context
            context?.resources?.displayMetrics?.heightPixels ?: 0
        } catch (e: Exception) {
            0
        }
    }
}