#!/bin/bash

# Build Distribution Script for TryInhouse Android SDK
# This script builds the client SDK and packages it for distribution

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
PROJECT_ROOT="$SCRIPT_DIR"
CLIENT_DIR="$PROJECT_ROOT/client"
DIST_DIR="$PROJECT_ROOT/client-sdk-distribution"

print_step "Starting TryInhouse Android SDK build and distribution packaging..."
echo "Project Root: $PROJECT_ROOT"
echo "Client Directory: $CLIENT_DIR"
echo "Distribution Directory: $DIST_DIR"

# Check if we're in the right directory
if [ ! -f "$CLIENT_DIR/build.gradle.kts" ]; then
    print_error "Client build.gradle.kts not found. Please run this script from the InhouseSDK root directory."
    exit 1
fi

# Clean previous builds
print_step "Cleaning previous builds..."
cd "$PROJECT_ROOT"
./gradlew clean

# Build the project
print_step "Building release version of the SDK..."
./gradlew :client:assembleRelease

# Generate documentation
print_step "Generating documentation..."
./gradlew :client:dokkaHtml

# Create javadoc JAR
print_step "Creating Javadoc JAR..."
./gradlew :client:javadocJar

# Create sources JAR
print_step "Creating Sources JAR..."
./gradlew :client:sourcesJar

# Create or clean distribution directory
print_step "Preparing distribution directory..."
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# Copy built artifacts to distribution directory
print_step "Copying artifacts to distribution directory..."

# Copy AAR file
AAR_FILE=$(find "$CLIENT_DIR/build/outputs/aar" -name "*.aar" | head -1)
if [ -f "$AAR_FILE" ]; then
    cp "$AAR_FILE" "$DIST_DIR/client-release.aar"
    print_success "Copied AAR: $(basename "$AAR_FILE")"
else
    print_error "AAR file not found!"
    exit 1
fi

# Copy Javadoc JAR
JAVADOC_JAR=$(find "$CLIENT_DIR/build/libs" -name "*javadoc*.jar" | head -1)
if [ -f "$JAVADOC_JAR" ]; then
    cp "$JAVADOC_JAR" "$DIST_DIR/client-javadoc.jar"
    print_success "Copied Javadoc JAR: $(basename "$JAVADOC_JAR")"
else
    print_error "Javadoc JAR not found!"
    exit 1
fi

# Copy Sources JAR
SOURCES_JAR=$(find "$CLIENT_DIR/build/libs" -name "*sources*.jar" | head -1)
if [ -f "$SOURCES_JAR" ]; then
    cp "$SOURCES_JAR" "$DIST_DIR/client-sources.jar"
    print_success "Copied Sources JAR: $(basename "$SOURCES_JAR")"
else
    print_error "Sources JAR not found!"
    exit 1
fi

# Copy ProGuard rules
print_step "Copying ProGuard configuration files..."
cp "$CLIENT_DIR/proguard-rules.pro" "$DIST_DIR/"
cp "$CLIENT_DIR/consumer-rules.pro" "$DIST_DIR/"
print_success "Copied ProGuard configuration files"

# Copy documentation
print_step "Copying documentation..."
if [ -d "$CLIENT_DIR/build/dokka" ]; then
    cp -r "$CLIENT_DIR/build/dokka" "$DIST_DIR/"
    print_success "Copied Dokka documentation"
else
    print_warning "Dokka documentation not found, skipping..."
fi

# Generate or update README for distribution
print_step "Generating distribution README..."
cat > "$DIST_DIR/README.md" << 'EOF'
# TryInhouse Android SDK Distribution

This directory contains the distributable artifacts for the TryInhouse Android SDK.

## Files Included

- `client-release.aar` - The main SDK library (Android Archive)
- `client-javadoc.jar` - API documentation in Javadoc format
- `client-sources.jar` - Source code for debugging and reference
- `proguard-rules.pro` - ProGuard rules for the SDK
- `consumer-rules.pro` - Consumer ProGuard rules for apps using the SDK
- `dokka/` - Detailed API documentation generated with Dokka (if available)

## Integration Guide

### Gradle Integration

1. Copy the `client-release.aar` file to your app's `libs` directory
2. Add the following to your app's `build.gradle` file:

```gradle
android {
    // ... your existing configuration
}

dependencies {
    // Add the TryInhouse SDK
    implementation files('libs/client-release.aar')
    
    // Required dependencies for the SDK
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.android.installreferrer:installreferrer:2.2'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'com.github.thumbmarkjs:thumbmark-android:1.0.+'
    
    // Standard Android dependencies
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
}

repositories {
    // Add JitPack for thumbmark dependency
    maven { url 'https://jitpack.io' }
}
```

3. Copy the ProGuard rules to your app if you're using ProGuard/R8:
   - Add contents of `proguard-rules.pro` to your app's ProGuard configuration
   - The `consumer-rules.pro` will be automatically applied when you include the AAR

### Basic Usage

```kotlin
import co.tryinhouse.android.TrackingSDK

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the SDK
        TrackingSDK.getInstance().initialize(
            context = this,
            projectToken = "your-project-token",
            tokenId = "your-token-id",
            shortLinkDomain = "yourdomain.link",
            serverUrl = "https://api.tryinhouse.co",
            enableDebugLogging = BuildConfig.DEBUG
        ) { callbackType, jsonData ->
            // Handle SDK callbacks
            Log.d("TrackingSDK", "Callback: $callbackType -> $jsonData")
        }
    }
}
```

### Activity Integration

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set current activity for SDK
        TrackingSDK.getInstance().setCurrentActivity(this)
    }
    
    override fun onResume() {
        super.onResume()
        TrackingSDK.getInstance().onAppResume()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        TrackingSDK.getInstance().onNewIntent(intent)
    }
}
```

## API Documentation

- View the complete API documentation in the `dokka/` directory
- Open `dokka/index.html` in a web browser for detailed documentation
- The `client-javadoc.jar` contains standard Javadoc format documentation

## ProGuard Configuration

If you're using ProGuard or R8 for code obfuscation, make sure to include the rules from:
- `proguard-rules.pro` - Add these rules to your app's ProGuard configuration
- `consumer-rules.pro` - These are automatically applied when you include the AAR

## Dependencies

The SDK requires the following dependencies in your app:

- OkHttp 4.12.0+ for networking
- Gson 2.10.1+ for JSON parsing
- Android Install Referrer 2.2+ for attribution
- Kotlin Coroutines 1.7.3+ for async operations
- Thumbmark Android 1.0.+ for device fingerprinting

## Support

For technical support and documentation, visit: https://docs.tryinhouse.co

## Version Information

This distribution was built on: $(date)
SDK Version: 1.0.0
EOF

print_success "Generated distribution README"

# Create a version info file
print_step "Creating version information..."
cat > "$DIST_DIR/version-info.txt" << EOF
TryInhouse Android SDK Distribution
==================================

Build Date: $(date)
Build Host: $(hostname)
Git Commit: $(git rev-parse HEAD 2>/dev/null || echo "unknown")
Git Branch: $(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")

SDK Version: 1.0.0
Minimum Android SDK: 24
Target Android SDK: 36
Compile SDK: 36

Dependencies:
- OkHttp: 4.12.0
- Gson: 2.10.1
- Install Referrer: 2.2
- Kotlin Coroutines: 1.7.3
- Thumbmark Android: 1.0.+

Artifacts:
- client-release.aar ($(du -h "$DIST_DIR/client-release.aar" | cut -f1))
- client-javadoc.jar ($(du -h "$DIST_DIR/client-javadoc.jar" | cut -f1))
- client-sources.jar ($(du -h "$DIST_DIR/client-sources.jar" | cut -f1))
EOF

print_success "Created version information"

# Calculate and display file sizes
print_step "Distribution summary:"
echo "Distribution directory: $DIST_DIR"
echo ""
echo "Files created:"
ls -la "$DIST_DIR" | grep -v "^d" | while read -r line; do
    echo "  $line"
done

# Calculate total size
TOTAL_SIZE=$(du -sh "$DIST_DIR" | cut -f1)
echo ""
print_success "Distribution package created successfully!"
echo "Total size: $TOTAL_SIZE"
echo "Location: $DIST_DIR"

# Verify all expected files are present
print_step "Verifying distribution integrity..."
REQUIRED_FILES=("client-release.aar" "client-javadoc.jar" "client-sources.jar" "proguard-rules.pro" "consumer-rules.pro" "README.md")
MISSING_FILES=()

for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$DIST_DIR/$file" ]; then
        MISSING_FILES+=("$file")
    fi
done

if [ ${#MISSING_FILES[@]} -eq 0 ]; then
    print_success "All required files are present in the distribution"
else
    print_error "Missing files in distribution:"
    for file in "${MISSING_FILES[@]}"; do
        echo "  - $file"
    done
    exit 1
fi

# Create a quick integration test script
print_step "Creating integration test helper..."
cat > "$DIST_DIR/test-integration.gradle" << 'EOF'
// Add this to your app's build.gradle to test the SDK integration

android {
    compileSdk 36
    
    defaultConfig {
        minSdk 24
        targetSdk 36
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // TryInhouse SDK
    implementation files('libs/client-release.aar')
    
    // Required dependencies
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.android.installreferrer:installreferrer:2.2'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'com.github.thumbmarkjs:thumbmark-android:1.0.+'
    
    // Standard Android dependencies
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
}

repositories {
    google()
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
EOF

print_success "Created integration test helper"

echo ""
print_success "🎉 Build and distribution packaging completed successfully!"
echo ""
echo "📦 Your SDK distribution is ready at: $DIST_DIR"
echo ""
echo "Next steps:"
echo "1. Copy the client-release.aar to your app's libs directory"
echo "2. Add the dependencies from README.md to your app's build.gradle"
echo "3. Follow the integration guide in README.md"
echo "4. Use test-integration.gradle as a reference for dependencies"
echo ""
print_success "Happy coding! 🚀" 