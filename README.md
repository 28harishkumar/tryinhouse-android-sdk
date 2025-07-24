# TryInhouse Android SDK

A comprehensive Android SDK for tracking app installs, app opens, and user interactions with shortlinks.

## 📦 Installation

### From Maven Central (Recommended)

```kotlin
// build.gradle.kts
dependencies {
    implementation("co.tryinhouse.android:sdk:1.0.0")
}
```

### From Local Maven Repository (Development)

```kotlin
// build.gradle.kts
dependencies {
    implementation("co.tryinhouse.android:sdk:1.0.0")
}

repositories {
    mavenLocal()
}
```

## 🚀 Quick Start

### 1. Initialize the SDK

```kotlin
import co.tryinhouse.android.TrackingSDK
import co.tryinhouse.android.models.SDKConfig

val config = SDKConfig(
    projectId = "your_project_id",
    projectToken = "your_project_token",
    shortLinkDomain = "yourdomain.com",
    serverUrl = "https://your-server.com",
    enableDebugLogging = true
)

TrackingSDK.initialize(context, config) { response ->
    // Handle initialization response
    Log.d("TrackingSDK", "Initialization response: $response")
}
```

### 2. Track Events

```kotlin
// Track app install
TrackingSDK.trackAppInstall { response ->
    Log.d("TrackingSDK", "App install tracked: $response")
}

// Track app open
TrackingSDK.trackAppOpen { response ->
    Log.d("TrackingSDK", "App open tracked: $response")
}

// Track custom event
TrackingSDK.trackCustomEvent("user_action", mapOf("action" to "button_click")) { response ->
    Log.d("TrackingSDK", "Custom event tracked: $response")
}
```

### 3. Handle Shortlinks

The SDK automatically detects and tracks shortlink interactions. For manual handling:

```kotlin
// Check if a URL is a shortlink
val isShortLink = ShortLinkDetector("yourdomain.com").isShortLink(url)

// Extract shortlink from referrer
val shortLink = ShortLinkDetector("yourdomain.com").extractShortLink(referrer)
```

## 🔧 Configuration

### SDKConfig Properties

| Property             | Type    | Required | Description                           |
| -------------------- | ------- | -------- | ------------------------------------- |
| `projectId`          | String  | Yes      | Your project identifier               |
| `projectToken`       | String  | Yes      | Your project authentication token     |
| `shortLinkDomain`    | String  | Yes      | Domain for shortlink detection        |
| `serverUrl`          | String  | Yes      | Your tracking server URL              |
| `enableDebugLogging` | Boolean | No       | Enable debug logging (default: false) |

### AndroidManifest.xml Setup

```xml
<application>
    <!-- For install referrer tracking -->
    <receiver android:name="co.tryinhouse.android.InstallReferrerReceiver"
        android:exported="true">
        <intent-filter>
            <action android:name="com.android.vending.INSTALL_REFERRER" />
        </intent-filter>
    </receiver>

    <!-- For deep link handling -->
    <activity android:name="co.tryinhouse.android.DeepLinkActivity"
        android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />
            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <data android:scheme="https" />
        </intent-filter>
    </activity>
</application>
```

## 📊 Event Tracking

### Automatic Events

- **App Install**: Tracked automatically on first app launch
- **App Open**: Tracked when app comes to foreground
- **Shortlink Click**: Tracked when user opens app via shortlink

### Manual Events

```kotlin
// Track custom events with additional data
TrackingSDK.trackCustomEvent(
    eventType = "purchase",
    extraData = mapOf(
        "amount" to 99.99,
        "currency" to "USD",
        "product_id" to "premium_subscription"
    )
) { response ->
    Log.d("TrackingSDK", "Purchase tracked: $response")
}
```

## 🔍 Debugging

### Enable Debug Logging

```kotlin
val config = SDKConfig(
    // ... other config
    enableDebugLogging = true
)
```

### Debug First Install State

```kotlin
// Check if this is the first install
val isFirstInstall = TrackingSDK.isFirstInstall()

// Reset first install state (for testing)
TrackingSDK.resetFirstInstall()

// Debug first install state
TrackingSDK.debugFirstInstallState()
```

## 🏗️ Development

### Building Locally

```bash
# Build the library
./gradlew :client:assembleRelease

# Generate documentation
./gradlew :client:dokkaHtml

# Publish to local Maven repository
./gradlew :client:publishReleasePublicationToMavenLocal
```

### Publishing to Maven Central

1. **Setup Environment Variables**

Create a `.env` file in the project root:

```bash
# OSSRH (Maven Central) credentials
OSSRH_USERNAME=your_username
OSSRH_PASSWORD=your_password

# GPG signing configuration
SIGNING_KEY_ID=your_key_id
SIGNING_PASSWORD=your_gpg_passphrase
SIGNING_SECRET_KEY_RING_FILE=/path/to/secret/key
```

2. **Publish to Maven Central**

```bash
# Use the provided script
./publish-to-maven-central.sh

# Or manually
./gradlew :client:publishReleasePublicationToOSSRHRepository -PenableSigning=true
```

### GPG Key Setup

1. **Generate GPG Key**

```bash
gpg --full-generate-key
```

2. **Export Public Key**

```bash
gpg --export --armor your_email@example.com > public-key.asc
```

3. **Upload to OSSRH**

- Go to https://s01.oss.sonatype.org/
- Upload your public key

4. **Get Key ID**

```bash
gpg --list-secret-keys --keyid-format LONG
```

## 📁 Project Structure

```
client/src/main/java/co/tryinhouse/android/
├── TrackingSDK.kt              # Main SDK class
├── EventTracker.kt             # Event tracking logic
├── NetworkClient.kt            # HTTP client for API calls
├── StorageManager.kt           # Local data storage
├── ShortLinkDetector.kt        # Shortlink detection
├── InstallReferrerManager.kt   # Install referrer handling
├── DeepLinkHandler.kt          # Deep link processing
├── BaseTrackingActivity.kt     # Base activity for tracking
├── DeepLinkActivity.kt         # Deep link activity
├── InstallReferrerReceiver.kt  # Install referrer receiver
└── models/
    ├── Event.kt                # Event data model
    ├── SDKConfig.kt            # SDK configuration
    └── InstallData.kt          # Install data model
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Email: ck@tryinhouse.co
- GitHub Issues: [Create an issue](https://github.com/28harishkumar/tryinhouse-android-sdk/issues)

## 📈 Version History

- **1.0.0** - Initial release with basic tracking functionality
  - App install tracking
  - App open tracking
  - Shortlink detection and tracking
  - Install referrer handling
  - Deep link support
  - Comprehensive device information collection
