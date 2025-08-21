package co.tryinhouse.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import co.tryinhouse.android.models.SDKConfig
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
     * Expose application context for SDK internals that require it
     */
    fun getApplicationContext(): Context? = context

    /**
     * Initialize the SDK with project token and shortlink domain
     * @param callback: (callbackType: String, jsonData: String) -> Unit
     */
    fun initialize(
        context: Context,
        projectToken: String,
        tokenId: String,
        shortLinkDomain: String,
        serverUrl: String = "https://api.tryinhouse.co",
        enableDebugLogging: Boolean = false,
        callback: ((callbackType: String, jsonData: String) -> Unit)? = null
    ) {
        Log.d("TrackingSDK", "initialize called with projectToken=$projectToken, tokenId=$tokenId, shortLinkDomain=$shortLinkDomain, serverUrl=$serverUrl, enableDebugLogging=$enableDebugLogging")
        this.context = context.applicationContext
        this.config = SDKConfig(
            projectToken = projectToken,
            tokenId = tokenId,
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
        } else {
            Log.d("TrackingSDK", "Not first install, skipping first install logic")
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
            // Send referrer directly to server without extracting shortlink
            Log.d("TrackingSDK", "Sending install referrer directly to server without shortlink extraction")
            trackAppInstallFromReferrer(installReferrer) { responseJson ->
                Log.d("TrackingSDK", "App install from referrer callback triggered: $responseJson")
                sdkCallback?.invoke("app_install_from_shortlink", responseJson)
            }
            storage.setFirstInstallComplete()
        } else {
            // Referrer not yet available, fetch it asynchronously
            fetchInstallReferrer { referrer ->
                if (referrer != null) {
                    Log.d("TrackingSDK", "Install referrer fetched async: $referrer")
                    // Send referrer directly to server without extracting shortlink
                    Log.d("TrackingSDK", "Sending install referrer directly to server without shortlink extraction (async)")
                    trackAppInstallFromReferrer(referrer) { responseJson ->
                        Log.d("TrackingSDK", "App install from referrer callback triggered (async): $responseJson")
                        sdkCallback?.invoke("app_install_from_shortlink", responseJson)
                    }
                } else {
                    Log.d("TrackingSDK", "No install referrer available after async fetch")
                }
                storage.setFirstInstallComplete()
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

    fun trackAppInstallFromShortLink(shortLink: String, referrer: String? = null, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppInstallFromShortLink called with shortLink=$shortLink, referrer=$referrer")
        eventTracker?.trackAppInstall(shortLink, referrer) { responseJson ->
            Log.d("TrackingSDK", "trackAppInstallFromShortLink callback: $responseJson")
            callback?.invoke(responseJson)
        }
    }

    fun trackAppInstallFromReferrer(referrer: String, callback: ((String) -> Unit)? = null) {
        Log.d("TrackingSDK", "trackAppInstallFromReferrer called with referrer=$referrer")
        eventTracker?.trackAppInstallReferrerOnly(referrer) { responseJson ->
            Log.d("TrackingSDK", "trackAppInstallFromReferrer callback: $responseJson")
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

    /**
     * Call this method when the app comes to the foreground (onResume)
     * This will check if the app was opened from a shortlink
     */
    fun onAppResume() {
        Log.d("TrackingSDK", "onAppResume called")
        checkForShortLinkOpen(isAppResume = true)
    }

    fun onNewIntent(intent: Intent?) {
        Log.d("TrackingSDK", "onNewIntent called with intent: $intent")
        // Check for shortlink in the new intent
        checkForShortLinkOpen(intent = intent, isAppResume = true)
    }

    /**
     * Check for shortlinks in the current activity's intent
     * This is called both during initialization and when app comes to foreground
     */
    private fun checkForShortLinkOpen(intent: Intent? = null, isAppResume: Boolean = false) {
        Log.d("TrackingSDK", "checkForShortLinkOpen called with isAppResume=$isAppResume")
        val activity = getCurrentActivity() ?: run {
            Log.e("TrackingSDK", "Current activity is null in checkForShortLinkOpen")
            return
        }
        val useIntent = intent ?: activity.intent

        Log.d("TrackingSDK", "Intent action: ${useIntent?.action}")
        Log.d("TrackingSDK", "New Intent data: ${useIntent?.data}")
        
        // Check for shortlink data regardless of action
        val data = useIntent?.data
        if (data != null && shortLinkDetector?.isShortLink(data.toString()) == true) {
            val shortLink = data.toString()
            Log.d("TrackingSDK", "App opened from shortlink: $shortLink")
            
            if (isAppResume) {
                // When app comes to foreground, only track app open
                trackAppOpenFromShortLink(shortLink) { responseJson ->
                    Log.d("TrackingSDK", "App open from shortlink callback triggered (resume): $responseJson")
                    sdkCallback?.invoke("shortlink_click", responseJson)
                }
            } else {
                // When app is first opened, track all events
                trackShortLinkClick(shortLink, data.toString()) { responseJson ->
                    Log.d("TrackingSDK", "Shortlink click callback triggered: $responseJson")
                    sdkCallback?.invoke("shortlink_click", responseJson)
                }
                // trackAppOpenFromShortLink(shortLink) { responseJson ->
                //     Log.d("TrackingSDK", "App open from shortlink callback triggered: $responseJson")
                //     sdkCallback?.invoke("app_open_from_shortlink", responseJson)
                // }
                trackSessionStartFromShortLink(shortLink) { responseJson ->
                    Log.d("TrackingSDK", "Session start from shortlink callback triggered: $responseJson")
                    sdkCallback?.invoke("session_start_from_shortlink", responseJson)
                }
            }
        } else {
            Log.d("TrackingSDK", "No shortlink data found in intent")
        }
    }

    private fun getCurrentActivity(): Activity? {
        return currentActivity
    }

    fun getSessionId(): String = _sessionId

    fun getDeviceId(): String = storageManager?.getDeviceId() ?: ""

    /**
    * Returns the stored install referrer string, or null if not available.
    */
    fun getInstallReferrer(): String? {
        return storageManager?.getInstallReferrer()
    }

    /**
    * Fetches the install referrer from the Play Store API (async).
    * Calls the callback with the referrer string or null.
    */
    fun fetchInstallReferrer(callback: (String?) -> Unit) {
        val ctx = context ?: run {
            Log.e("TrackingSDK", "Context is null in fetchInstallReferrer")
            callback(null)
            return
        }
        val sm = storageManager ?: run {
            Log.e("TrackingSDK", "StorageManager is null in fetchInstallReferrer")
            callback(null)
            return
        }
        val irm = InstallReferrerManager(ctx, sm)
        irm.getInstallReferrer(callback)
    }

    // For testing purposes - reset first install flag
    fun resetFirstInstall() {
        Log.d("TrackingSDK", "resetFirstInstall called")
        val storage = storageManager ?: run {
            Log.e("TrackingSDK", "StorageManager is null in resetFirstInstall")
            return
        }
        storage.resetFirstInstall()
    }

    // For debugging - check current first install state
    fun debugFirstInstallState() {
        Log.d("TrackingSDK", "debugFirstInstallState called")
        val storage = storageManager ?: run {
            Log.e("TrackingSDK", "StorageManager is null in debugFirstInstallState")
            return
        }
        storage.debugFirstInstallState()
    }
}