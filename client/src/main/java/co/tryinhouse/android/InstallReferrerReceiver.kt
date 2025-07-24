package co.tryinhouse.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class InstallReferrerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TrackingSDK", "onReceive called with intent: $intent, extras: ${intent.extras}")
        val referrer = intent.getStringExtra("referrer")
        if (referrer != null) {
            val storageManager = StorageManager(context)
            storageManager.storeInstallReferrer(referrer)
            Log.d("TrackingSDK", "Install referrer received and stored: $referrer")
        } else {
            Log.w("TrackingSDK", "No referrer found in intent extras: ${intent.extras}")
        }
    }
}