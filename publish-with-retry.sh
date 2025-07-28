#!/bin/bash

# Script to publish TryInhouse Android SDK to Maven Central with retry logic
# This handles temporary server issues with exponential backoff

set -e

echo "🚀 Publishing TryInhouse Android SDK to Maven Central with retry logic..."

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

echo "✅ GPG signing configured"

# Retry logic
MAX_RETRIES=5
RETRY_DELAY=30

for attempt in $(seq 1 $MAX_RETRIES); do
    echo "📦 Publishing attempt $attempt of $MAX_RETRIES..."
    
    if ./gradlew :client:publishReleasePublicationToOSSRHRepository -PenableSigning=true; then
        echo "✅ Publishing completed successfully on attempt $attempt!"
        echo "🎉 TryInhouse Android SDK has been published to Maven Central"
        echo "📋 Users can now include it in their projects with:"
        echo "   implementation('co.tryinhouse.android:sdk:1.0.0')"
        exit 0
    else
        echo "❌ Publishing failed on attempt $attempt"
        
        if [ $attempt -lt $MAX_RETRIES ]; then
            echo "⏳ Waiting $RETRY_DELAY seconds before retry..."
            sleep $RETRY_DELAY
            RETRY_DELAY=$((RETRY_DELAY * 2))  # Exponential backoff
        else
            echo "❌ All $MAX_RETRIES attempts failed."
            echo "💡 This appears to be a persistent server issue with Sonatype OSSRH."
            echo "📧 Please contact Sonatype support: ossrh-support@sonatype.org"
            echo "📋 Include your error messages and groupId: co.tryinhouse.android"
            exit 1
        fi
    fi
done 