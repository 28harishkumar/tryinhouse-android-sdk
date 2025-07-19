package com.inhouse.client

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class InstallReferrerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer")
        if (referrer != null) {
            val storageManager = StorageManager(context)
            storageManager.storeInstallReferrer(referrer)

            Log.d("InstallReferrerReceiver", "Install referrer received: $referrer")
        }
    }
}