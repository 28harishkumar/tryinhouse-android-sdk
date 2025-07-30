#!/bin/bash

# Script to publish TryInhouse Android SDK to Maven Central
# This script handles GPG signing and publishing using the modern Central Portal workflow

set -e

echo "🚀 Publishing TryInhouse Android SDK to Maven Central..."

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ Error: .env file not found. Please create it with your credentials."
    exit 1
fi

# Load environment variables
source .env

# Check required environment variables
if [ -z "$OSSRH_USERNAME" ] || [ -z "$OSSRH_PASSWORD" ]; then
    echo "❌ Error: OSSRH credentials not found in .env file"
    exit 1
fi

if [ -z "$SIGNING_KEY_ID" ] || [ -z "$SIGNING_PASSWORD" ]; then
    echo "❌ Error: GPG signing credentials not found in .env file"
    exit 1
fi

echo "✅ Environment variables loaded successfully"

# Setup GPG signing
echo "🔐 Setting up GPG signing..."

# Export environment variables for Gradle
export SIGNING_KEY_ID
export SIGNING_PASSWORD
export SIGNING_SECRET_KEY_RING_FILE

# Debug: Check if keyring file exists
if [ -n "$SIGNING_SECRET_KEY_RING_FILE" ]; then
    if [ -f "$SIGNING_SECRET_KEY_RING_FILE" ]; then
        echo "✅ Keyring file exists: $SIGNING_SECRET_KEY_RING_FILE"
    else
        echo "⚠️  Warning: Keyring file not found: $SIGNING_SECRET_KEY_RING_FILE"
    fi
else
    echo "ℹ️  No keyring file specified, will use in-memory signing"
fi

echo "✅ GPG signing configured"

# Publish to Maven Central using modern Central Portal workflow
echo "📦 Publishing to Maven Central..."
./gradlew :client:publishReleasePublicationToSonatypeRepository -PenableSigning=true

echo "✅ Publishing completed successfully!"
echo "🎉 TryInhouse Android SDK has been published to Maven Central"
echo "📋 Users can now include it in their projects with:"
echo "   implementation('co.tryinhouse.android:sdk:1.0.0')" 