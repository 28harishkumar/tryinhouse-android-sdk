package co.tryinhouse.android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseTrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle deep links
        handleDeepLink()
    }

    private fun handleDeepLink() {
        val trackingSDK = TrackingSDK.getInstance()
        val config = trackingSDK.config

        if (config != null) {
            val deepLinkHandler = DeepLinkHandler(trackingSDK, config)
            deepLinkHandler.handleDeepLink(this, intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setIntent(intent)
            handleDeepLink()
        }
    }
}