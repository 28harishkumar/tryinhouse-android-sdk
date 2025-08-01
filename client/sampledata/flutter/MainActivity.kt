package com.inhouse.flutter_application

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import com.inhouse.flutter_application.TrackingSDKPlugin  
import android.content.Intent
import co.tryinhouse.android.TrackingSDK

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine
            .plugins
            .add(TrackingSDKPlugin())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        TrackingSDK.getInstance().onNewIntent(intent)
    }
}
