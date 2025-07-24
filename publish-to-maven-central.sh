#!/bin/bash

# Script to publish TryInhouse Android SDK to Maven Central
# This script handles GPG signing and publishing to OSSRH

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

# Copy the keyring file to a temporary location
TEMP_KEYRING="/tmp/temp-keyring.gpg"
cp "$SIGNING_SECRET_KEY_RING_FILE" "$TEMP_KEYRING"

# Use the temporary keyring file
export SIGNING_KEY_ID
export SIGNING_PASSWORD
export SIGNING_SECRET_KEY_RING_FILE="$TEMP_KEYRING"

echo "✅ GPG signing configured"

# Publish to Maven Central
echo "📦 Publishing to Maven Central..."
echo "⚠️  Note: Publishing without GPG signing due to key access issues"
./gradlew :client:publishReleasePublicationToOSSRHRepository -PenableSigning=true

# Clean up temporary files
rm -f "$TEMP_KEYRING"

echo "✅ Publishing completed successfully!"
echo "🎉 TryInhouse Android SDK has been published to Maven Central"
echo "📋 Users can now include it in their projects with:"
echo "   implementation('co.tryinhouse.android:sdk:1.0.0')" 