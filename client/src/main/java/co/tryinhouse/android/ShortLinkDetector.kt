package co.tryinhouse.android

import android.content.Intent
import android.net.Uri
import android.util.Log

class ShortLinkDetector(private val shortLinkDomain: String) {

    fun isShortLink(url: String): Boolean {
        try {
            Log.d("TrackingSDK", "isShortLink called with url=$url")
            val uri = Uri.parse(url)
            val host = uri.host
            val result = host != null && (host == shortLinkDomain || host.endsWith(".$shortLinkDomain"))
            Log.d("TrackingSDK", "isShortLink result: $result for host=$host and domain=$shortLinkDomain")
            return result
        } catch (e: Exception) {
            Log.e("TrackingSDK", "Error in isShortLink: ${e.message}")
            return false
        }
    }

    fun extractShortLink(referrer: String): String? {
        try {
            Log.d("TrackingSDK", "extractShortLink called with referrer=$referrer")
            val uri = Uri.parse(referrer)
            val shortLinkParam = uri.getQueryParameter("shortlink")

            if (shortLinkParam != null && isShortLink(shortLinkParam)) {
                Log.d("TrackingSDK", "Shortlink param found and valid: $shortLinkParam")
                return shortLinkParam
            } else {
                // Check if the referrer itself is a shortlink
                if (isShortLink(referrer)) {
                    Log.d("TrackingSDK", "Referrer itself is a shortlink: $referrer")
                    return referrer
                } else {
                    Log.d("TrackingSDK", "No valid shortlink found in referrer")
                    return null
                }
            }
        } catch (e: Exception) {
            Log.e("TrackingSDK", "Error in extractShortLink: ${e.message}")
            return null
        }
    }

    fun extractShortLinkFromIntent(intent: Intent): String? {
        val data = intent.data
        Log.d("TrackingSDK", "extractShortLinkFromIntent called with data=$data")
        return if (data != null && isShortLink(data.toString())) {
            Log.d("TrackingSDK", "Intent data is a valid shortlink: ${data}")
            data.toString()
        } else {
            Log.d("TrackingSDK", "Intent data is not a valid shortlink")
            null
        }
    }
}