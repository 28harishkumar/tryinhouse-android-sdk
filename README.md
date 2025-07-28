# TryInhouse Android SDK

Android SDK for tracking app installs, app opens, and user interactions with shortlinks.

## Setup for Maven Central Publishing

### 1. Create Central Portal Account

1. Visit [Central Portal](https://central.sonatype.com/) and create an account
2. Generate a user token for secure authentication
3. Request namespace verification for your group ID (e.g., `co.tryinhouse.android`)

### 2. Generate GPG Key

```bash
# Generate a new GPG key
gpg --full-generate-key

# List your keys to get the key ID
gpg --list-keys --keyid-format SHORT

# Export your public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
```

### 3. Configure Credentials

Create a `.env` file in the project root:

```bash
# OSSRH (Maven Central) credentials
OSSRH_USERNAME=your-actual-ossrh-username
OSSRH_PASSWORD=your-actual-ossrh-password

# GPG signing configuration
SIGNING_KEY_ID=your-actual-gpg-key-id
SIGNING_PASSWORD=your-actual-gpg-password

# Optional: Path to GPG keyring file
# SIGNING_SECRET_KEY_RING_FILE=path/to/your/secring.gpg
```

### 4. Publish to Maven Central

```bash
# Build and test
./gradlew clean build test

# Generate documentation
./gradlew dokkaHtml

# Publish to Maven Central
./gradlew :client:publishReleasePublicationToSonatypeRepository -PenableSigning=true
```

### 5. GitHub Actions Setup

For automated publishing, add these secrets to your GitHub repository:

- `OSSRH_USERNAME`: Your Central Portal username
- `OSSRH_PASSWORD`: Your Central Portal password/token
- `SIGNING_KEY_ID`: Your GPG key ID
- `SIGNING_KEY_PASSPHRASE`: Your GPG key passphrase
- `SIGNING_KEY`: Your GPG private key (base64 encoded)

To export your GPG private key:

```bash
gpg --armor --export-secret-keys YOUR_KEY_ID | base64 -w0
```

## Usage

After publishing, users can include your SDK in their projects:

```kotlin
dependencies {
    implementation("co.tryinhouse.android:sdk:1.0.0")
}
```

## Modern Central Portal Workflow

This project uses the modern Central Portal workflow (post-July 2025) with the nexus-publish plugin, which provides:

- ✅ Simplified publishing process
- ✅ Automatic artifact validation
- ✅ Direct upload to Central Portal
- ✅ Better error handling and feedback
- ✅ Future-proof configuration

For detailed publishing instructions, see [PUBLISHING.md](PUBLISHING.md).
