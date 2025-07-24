#!/bin/bash

# Script to publish TryInhouse Android SDK to GitHub Packages
# This is an alternative to Maven Central while fixing OSSRH issues

set -e

echo "🚀 Publishing TryInhouse Android SDK to GitHub Packages..."

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "❌ Error: .env file not found. Please create it with your credentials."
    exit 1
fi

# Load environment variables
source .env

# Check required environment variables
if [ -z "$GITHUB_USERNAME" ] || [ -z "$GITHUB_TOKEN" ]; then
    echo "❌ Error: GitHub credentials not found in .env file"
    echo "Please add:"
    echo "GITHUB_USERNAME=your_github_username"
    echo "GITHUB_TOKEN=your_github_personal_access_token"
    exit 1
fi

echo "✅ Environment variables loaded successfully"

# Publish to GitHub Packages
echo "📦 Publishing to GitHub Packages..."
./gradlew :client:publishReleasePublicationToGitHubPackagesRepository -PenableSigning=false

echo "✅ Publishing completed successfully!"
echo "🎉 TryInhouse Android SDK has been published to GitHub Packages"
echo "📋 Users can now include it in their projects with:"
echo "   implementation('co.tryinhouse.android:sdk:1.0.0')" 