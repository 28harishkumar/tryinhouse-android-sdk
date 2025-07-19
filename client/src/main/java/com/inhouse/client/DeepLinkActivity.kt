package com.inhouse.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log

class DeepLinkActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val data = intent.data

        if (data != null) {
            val trackingSDK = TrackingSDK.getInstance()
            val shortLinkDetector = ShortLinkDetector("yourdomain.com") // Use your domain

            if (shortLinkDetector.isShortLink(data.toString())) {
                val shortLink = data.toString()

                // Track shortlink click
                trackingSDK.trackShortLinkClick(shortLink, data.toString())

                Log.d("DeepLinkActivity", "Deep link handled: $shortLink")
            }
        }

        // Redirect to main activity
        val mainIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (mainIntent != null) {
            startActivity(mainIntent)
        }
        finish()
    }
}