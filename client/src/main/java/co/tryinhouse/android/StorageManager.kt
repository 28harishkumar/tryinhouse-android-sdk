package co.tryinhouse.android

import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import co.tryinhouse.android.models.Event
import co.tryinhouse.android.models.InstallData
import java.util.UUID

class StorageManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("tracking_sdk", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_FIRST_INSTALL = "first_install"
        private const val KEY_INSTALL_DATA = "install_data"
        private const val KEY_INSTALL_REFERRER = "install_referrer"
        private const val KEY_FAILED_EVENTS = "failed_events"
    }

    fun getDeviceId(): String {
        var deviceId = sharedPreferences.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }

    fun isFirstInstall(): Boolean {
        val isFirst = sharedPreferences.getBoolean(KEY_FIRST_INSTALL, true)
        val hasKey = sharedPreferences.contains(KEY_FIRST_INSTALL)
        Log.d("StorageManager", "isFirstInstall() called, hasKey=$hasKey, returning: $isFirst")
        return isFirst
    }

    fun setFirstInstallComplete() {
        Log.d("StorageManager", "setFirstInstallComplete() called, setting first_install to false")
        sharedPreferences.edit().putBoolean(KEY_FIRST_INSTALL, false).apply()
    }

    // For testing purposes - reset first install flag
    fun resetFirstInstall() {
        Log.d("StorageManager", "resetFirstInstall() called, setting first_install to true")
        sharedPreferences.edit().putBoolean(KEY_FIRST_INSTALL, true).apply()
    }

    // For debugging - check current state
    fun debugFirstInstallState() {
        val hasKey = sharedPreferences.contains(KEY_FIRST_INSTALL)
        val value = sharedPreferences.getBoolean(KEY_FIRST_INSTALL, true)
        Log.d("StorageManager", "DEBUG: first_install key exists=$hasKey, value=$value")
    }

    fun storeInstallData(installData: InstallData) {
        val json = gson.toJson(installData)
        sharedPreferences.edit().putString(KEY_INSTALL_DATA, json).apply()
    }

    fun getInstallData(): InstallData? {
        val json = sharedPreferences.getString(KEY_INSTALL_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, InstallData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun storeInstallReferrer(referrer: String) {
        sharedPreferences.edit().putString(KEY_INSTALL_REFERRER, referrer).apply()
    }

    fun getInstallReferrer(): String? {
        return sharedPreferences.getString(KEY_INSTALL_REFERRER, null)
    }

    fun storeFailedEvent(event: Event) {
        val existingEvents = getFailedEvents().toMutableList()
        existingEvents.add(event)

        // Keep only last 100 failed events
        if (existingEvents.size > 100) {
            existingEvents.removeAt(0)
        }

        val json = gson.toJson(existingEvents)
        sharedPreferences.edit().putString(KEY_FAILED_EVENTS, json).apply()
    }

    fun getFailedEvents(): List<Event> {
        val json = sharedPreferences.getString(KEY_FAILED_EVENTS, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<Event>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun clearFailedEvents() {
        sharedPreferences.edit().remove(KEY_FAILED_EVENTS).apply()
    }
}