package com.inhouse.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.inhouse.client.models.SDKConfig
import java.util.UUID

class TrackingSDK private constructor() {
    companion object {
        @Volatile
        private var INSTANCE: TrackingSDK? = null

        fun getInstance(): TrackingSDK {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TrackingSDK().also { INSTANCE = it }
            }
        }
    }

    private var context: Context? = null
    public var config: SDKConfig? = null
    private var eventTracker: EventTracker? = null
    private var networkClient: NetworkClient? = null
    private var storageManager: StorageManager? = null
    private var shortLinkDetector: ShortLinkDetector? = null
    private var deepLinkHandler: DeepLinkHandler? = null
    private var sdkCallback: ((String, String) -> Unit)? = null
    private var currentActivity: Activity? = null

    private val _sessionId: String by lazy {
        UUID.randomUUID().toString()
    }

    /**
     * Initialize the SDK with project token and shortlink domain
     * @param callback: (callbackType: String, jsonData: String) -> Unit
     */
    fun initialize(
        context: Context,
        projectId: String,
        projectToken: String,
        shortLinkDomain: String,
        serverUrl: String = "https://your-api-server.com",
        enableDebugLogging: Boolean = false,
        callback: ((callbackType: String, jsonData: String) -> Unit)? = null
    ) {
        Log.d("TrackingSDK", "initialize called with projectId=$projectId, projectToken=$projectToken, shortLinkDomain=$shortLinkDomain, serverUrl=$serverUrl, enableDebugLogging=$enableDebugLogging")
        this.context = context.applicationContext
        this.config = SDKConfig(
            projectId = projectId,
            projectToken = projectToken,
            shortLinkDomain = shortLinkDomain,
            serverUrl = serverUrl,
            enableDebugLogging = enableDebugLogging
        )
        this.sdkCallback = callback

        initializeComponents()
        handleAppLaunch()

        if (enableDebugLogging) {
            Log.d("TrackingSDK", "SDK initialized with domain: $shortLinkDomain")
        }
    }

    private fun initializeComponents() {
        Log.d("TrackingSDK", "initializeComponents called")
        val ctx = context ?: run {
            Log.e("TrackingSDK", "Context is null in initializeComponents")
            return
        }
        val cfg = config ?: run {
            Log.e("TrackingSDK", "Config is null in initializeComponents")
            return
        }

        storageManager = StorageManager(ctx)
        networkClient = NetworkClient(cfg)
        eventTracker = EventTracker(networkClient!!, storageManager!!, cfg)
        shortLinkDetector = ShortLinkDetector(cfg.shortLinkDomain)
        deepLinkHandler = DeepLinkHandler(this, cfg)
        Log.d("TrackingSDK", "Components initialized")
    }

    private fun handleAppLaunch() {
        Log.d("TrackingSDK", "handleAppLaunch called")
        val storage = storageManager ?: run {
            Log.e("TrackingSDK", "StorageManager is null in handleAppLaunch")
            return
        }

        // Check if this is first launch after install
        if (storage.isFirstInstall()) {
            Log.d("TrackingSDK", "First install detected")
            handleFirstInstall()
        }

        // Check if app was opened from a shortlink
        checkForShortLinkOpen()
    }

    private fun handleFirstInstall() {
        Log.d("TrackingSDK", "handleFirstInstall called")
        val storage = storageManager ?: run {
            Log.e("TrackingSDK", "StorageManager is null in handleFirstInstall")
            return
        }
        val installReferrer = storage.getInstallReferrer()

        if (installReferrer != null) {
            Log.d("TrackingSDK", "Install referrer found: $installReferrer")
            val shortLink = shortLinkDetector?.extractShortLink(installReferrer)
            if (shortLink != null) {
                Log.d("TrackingSDK", "Shortlink extracted from install referrer: $shortLink")
                // Track app install from shortlink and callback
                trackAppInstallFromShortLink(shortLink) { responseJson ->
                    Log.d("TrackingSDK", "App install from shortlink callback triggered: $responseJson")
                    sdkCallback?.invoke("app_install_from_shortlink", responseJson)
                }
            } else {
                Log.d("TrackingSDK", "No shortlink found in install referrer")
            }
        }

        storage.setFirstInstallComplete()
    }

    private fun checkForShortLinkOpen() {
        Log.d("TrackingSDK", "checkForShortLinkOpen called")
        val activity = getCurrentActivity() ?: run {
            Log.e("TrackingSDK", "Current activity is null in checkForShortLinkOpen")
            return
        }
        val intent = activity.intent

        if (intent?.action == Intent.ACTION_VIEW) {
            val data = intent.data
            Log.d("TrackingSDK", "Intent.ACTION_VIEW data is: $data")
            if (data != null && shortLinkDetector?.isShortLink(data.toString()) == true) {
                val shortLink = data.toString()
                Log.d("TrackingSDK", "App opened from shortlink: $shortLink")
                // Track shortlink click
                trackShortLinkClick(shortLink, data.toString()) { responseJson ->
                    Log.d("TrackingSDK", "Shortlink click callback triggered: $responseJson")
                    sdkCallback?.invoke("shortlink_click", responseJson)
                }
                // Track app open from shortlink
                trackAppOpenFromShortLink(shortLink) { responseJson ->
                    Log.d("TrackingSDK", "App open from shortlink callback triggered: $responseJson")
                    sdkCallback?.invoke("app_open_from_shortlink", responseJson)
                }
                // Track session start from shortlink
                trackSessionStartFromShortLink(shortLink) { responseJson ->
                    Log.d("TrackingSDK", "Session start from shortlink callback triggered: $responseJson")
                    sdkCallback?.invoke("session_start_from_shortlink", responseJson)
                }
            } else {
                Log.d("TrackingSDK", "Intent data is not a recognized shortlink")
            }
        }
    }

    // Public tracking methods
    fun trackAppOpen(shortLink: String? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppOpen called with shortLink=$shortLink")
        eventTracker?.trackEvent("app_open", shortLink) { responseJson ->
            Log.d("TrackingSDK", "trackAppOpen callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackAppOpenFromShortLink(shortLink: String, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppOpenFromShortLink called with shortLink=$shortLink")
        eventTracker?.trackEvent("app_open_shortlink", shortLink) { responseJson ->
            Log.d("TrackingSDK", "trackAppOpenFromShortLink callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackSessionStart(shortLink: String? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackSessionStart called with shortLink=$shortLink")
        eventTracker?.trackEvent("session_start", shortLink) { responseJson ->
            Log.d("TrackingSDK", "trackSessionStart callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackSessionStartFromShortLink(shortLink: String, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackSessionStartFromShortLink called with shortLink=$shortLink")
        eventTracker?.trackEvent("session_start_shortlink", shortLink) { responseJson ->
            Log.d("TrackingSDK", "trackSessionStartFromShortLink callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackShortLinkClick(shortLink: String, deepLink: String? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackShortLinkClick called with shortLink=$shortLink, deepLink=$deepLink")
        eventTracker?.trackShortLinkClick(shortLink, deepLink) { responseJson ->
            Log.d("TrackingSDK", "trackShortLinkClick callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackAppInstallFromShortLink(shortLink: String, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppInstallFromShortLink called with shortLink=$shortLink")
        eventTracker?.trackAppInstall(shortLink) { responseJson ->
            Log.d("TrackingSDK", "trackAppInstallFromShortLink callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackCustomEvent(eventType: String, shortLink: String? = null, additionalData: Map<String, String>? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackCustomEvent called with eventType=$eventType, shortLink=$shortLink, additionalData=$additionalData")
        eventTracker?.trackCustomEvent(eventType, shortLink, additionalData) { responseJson ->
            Log.d("TrackingSDK", "trackCustomEvent callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun setCurrentActivity(activity: Activity?) {
        Log.d("TrackingSDK", "setCurrentActivity called with activity=$activity")
        this.currentActivity = activity
    }

    private fun getCurrentActivity(): Activity? {
        return currentActivity
    }

    fun getSessionId(): String = _sessionId

    fun getDeviceId(): String = storageManager?.getDeviceId() ?: ""
}