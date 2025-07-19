package com.inhouse.client

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.inhouse.client.models.SDKConfig

class DeepLinkHandler(
    private val trackingSDK: TrackingSDK,
    private val config: SDKConfig
) {

    fun handleDeepLink(activity: Activity, intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val data = intent.data
            if (data != null) {
                val shortLinkDetector = ShortLinkDetector(config.shortLinkDomain)

                if (shortLinkDetector.isShortLink(data.toString())) {
                    val shortLink = data.toString()

                    // Track shortlink click
                    trackingSDK.trackShortLinkClick(shortLink, data.toString())

                    // Track app open from shortlink
                    trackingSDK.trackAppOpenFromShortLink(shortLink)

                    if (config.enableDebugLogging) {
                        Log.d("DeepLinkHandler", "Handled shortlink: $shortLink")
                    }
                }
            }
        }
    }
}