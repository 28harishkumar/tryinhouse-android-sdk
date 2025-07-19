package com.inhouse.client

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener

class InstallReferrerManager(
    private val context: Context,
    private val storageManager: StorageManager
) {

    private var referrerClient: InstallReferrerClient? = null

    fun getInstallReferrer(callback: (String?) -> Unit) {
        referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        try {
                            val response = referrerClient?.installReferrer
                            val referrer = response?.installReferrer

                            if (referrer != null) {
                                storageManager.storeInstallReferrer(referrer)
                                callback(referrer)
                            } else {
                                callback(null)
                            }
                        } catch (e: Exception) {
                            Log.e("TrackingSDK", "Error getting install referrer", e)
                            callback(null)
                        }
                    }
                    else -> {
                        Log.e("TrackingSDK", "Install referrer setup failed: $responseCode")
                        callback(null)
                    }
                }
                referrerClient?.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {
                Log.w("TrackingSDK", "Install referrer service disconnected")
                callback(null)
            }
        })
    }
}