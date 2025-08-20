package com.inhouse.flutter_application

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import co.tryinhouse.android.TrackingSDK
import android.util.Log
import android.app.Activity
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import android.os.Handler
import android.os.Looper

class TrackingSDKPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private var activity: Activity? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "tracking_sdk")
        channel.setMethodCallHandler(this)
        Log.d("TrackingSDKPlugin", "onAttachedToEngine called")
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        Log.d("TrackingSDKPlugin", "onMethodCall: ${call.method}")
        when (call.method) {
            "initialize" -> {
                val projectToken = call.argument<String>("projectToken")
                val shortLinkDomain = call.argument<String>("shortLinkDomain")
                val serverUrl = call.argument<String>("serverUrl")
                val enableDebugLogging = call.argument<Boolean>("enableDebugLogging") ?: false
                val tokenId = call.argument<String>("tokenId")

                Log.d("TrackingSDKPlugin", "initialize called with projectToken=$projectToken, tokenId=$tokenId, shortLinkDomain=$shortLinkDomain, serverUrl=$serverUrl, enableDebugLogging=$enableDebugLogging")

                if (projectToken != null && shortLinkDomain != null && tokenId != null) {
                    TrackingSDK.getInstance().initialize(
                        context = context,
                        projectToken = projectToken,
                        tokenId = tokenId,
                        shortLinkDomain = shortLinkDomain,
                        serverUrl = serverUrl ?: "https://your-api-server.com",
                        enableDebugLogging = enableDebugLogging
                    ) { callbackType, jsonData ->
                        Log.d("TrackingSDKPlugin", "SDK callback: callbackType=$callbackType, data=$jsonData")
                        val args = mapOf("callbackType" to callbackType, "data" to jsonData)
                        Handler(Looper.getMainLooper()).post {
                            channel.invokeMethod("onSdkCallback", args)
                        }
                    }
                    result.success("initialized")
                } else {
                    Log.e("TrackingSDKPlugin", "Missing required arguments for initialize")
                    result.error("INVALID_ARGUMENTS", "Missing required arguments", null)
                }
            }
            "onAppResume" -> {
                TrackingSDK.getInstance().onAppResume()
                result.success(null)
            }
            "trackAppOpen" -> {
                val shortLink = call.argument<String>("shortLink")
                TrackingSDK.getInstance().trackAppOpen(shortLink) { responseJson ->
                    result.success(responseJson)
                }
            }
            "trackSessionStart" -> {
                val shortLink = call.argument<String>("shortLink")
                TrackingSDK.getInstance().trackSessionStart(shortLink) { responseJson ->
                    result.success(responseJson)
                }
            }
            "trackShortLinkClick" -> {
                val shortLink = call.argument<String>("shortLink")
                val deepLink = call.argument<String>("deepLink")
                if (shortLink != null) {
                    TrackingSDK.getInstance().trackShortLinkClick(shortLink, deepLink) { responseJson ->
                        result.success(responseJson)
                    }
                } else {
                    result.success(null)
                }
            }
            "getInstallReferrer" -> {
                val referrer = TrackingSDK.getInstance().getInstallReferrer()
                result.success(referrer)
            }
            "fetchInstallReferrer" -> {
                TrackingSDK.getInstance().fetchInstallReferrer { referrer ->
                    Handler(Looper.getMainLooper()).post {
                        result.success(referrer)
                    }
                }
            }
            "resetFirstInstall" -> {
                TrackingSDK.getInstance().resetFirstInstall()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        TrackingSDK.getInstance().setCurrentActivity(activity)
        Log.d("TrackingSDKPlugin", "onAttachedToActivity: activity set")
    }

    override fun onDetachedFromActivity() {
        activity = null
        TrackingSDK.getInstance().setCurrentActivity(null)
        Log.d("TrackingSDKPlugin", "onDetachedFromActivity: activity cleared")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        TrackingSDK.getInstance().setCurrentActivity(activity)
        Log.d("TrackingSDKPlugin", "onReattachedToActivityForConfigChanges: activity set")
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
        TrackingSDK.getInstance().setCurrentActivity(null)
        Log.d("TrackingSDKPlugin", "onDetachedFromActivityForConfigChanges: activity cleared")
    }
}