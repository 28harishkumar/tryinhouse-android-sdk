# Maven Central Publishing Setup Guide

This guide provides step-by-step instructions for setting up Maven Central publishing for the TryInhouse Android SDK using the modern Central Portal workflow.

## Prerequisites

- GitHub account
- Domain ownership (for namespace verification) or GitHub-hosted project
- GPG key generation capability

## Step 1: Create Central Portal Account

### 1.1 Visit Central Portal

1. Go to [Central Portal](https://central.sonatype.com/)
2. Click "Sign In" at the top right
3. Choose "Create Account" with username/password (not social login)

### 1.2 Register Account

1. **Use Username/Password**: Choose the option to create an account with username and password
2. **Provide Email**: Use a valid email address for verification
3. **Set Password**: Create a secure password
4. **Verify Email**: Click the verification link sent to your inbox

### 1.3 Generate User Token

1. **Log in** to Central Portal with your credentials
2. **Navigate to Account Settings**: Click your username → Account
3. **Generate Token**: Create a user token for secure authentication
4. **Save Credentials**: Note down the `token-username` and `token-password`

**Important**: Use the user token credentials, not your UI login credentials, for publishing.

## Step 2: Generate GPG Key

### 2.1 Generate GPG Key

```bash
# Generate a new GPG key (RSA, 4096 bits recommended)
gpg --full-generate-key

# Follow the prompts:
# 1. Choose option 1 (RSA and RSA)
# 2. Choose 4096 bits
# 3. Choose 0 (no expiration)
# 4. Enter your name and email
# 5. Enter a secure passphrase
```

### 2.2 Get Key Information

```bash
# List your keys to get the key ID
gpg --list-keys --keyid-format SHORT

# Example output:
# pub   rsa4096/9ABCDEF0 2024-01-01 [SC]
#       1234567890ABCDEF1234567890ABCDEF12345678
# uid           [ultimate] Your Name <your-email@example.com>
# sub   rsa4096/12345678 2024-01-01 [E]

# The key ID is: 9ABCDEF0
```

### 2.3 Publish Public Key

```bash
# Export your public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Replace YOUR_KEY_ID with your actual key ID (e.g., 9ABCDEF0)
```

### 2.4 Export Private Key (for CI/CD)

```bash
# Export your private key as base64 for GitHub Actions
gpg --armor --export-secret-keys YOUR_KEY_ID | base64 -w0

# Save this output as the SIGNING_KEY secret in GitHub
```

## Step 3: Register Namespace

### 3.1 Choose Group ID

For this project, we're using `co.tryinhouse.android`. You have options:

1. **GitHub-hosted**: `io.github.your-username` (automatic verification)
2. **Domain-owned**: `com.yourdomain` (requires domain verification)
3. **Custom**: Contact Central Support for verification

### 3.2 Verify Namespace

1. **Log in** to Central Portal
2. **Navigate to Namespace Registration**
3. **Choose Verification Method**:
   - **GitHub**: Automatic verification for `io.github.*` namespaces
   - **Domain**: Upload verification file to your domain
   - **Manual**: Contact support for custom namespaces

### 3.3 Request OSSRH Access (if needed)

If you need legacy OSSRH access:

1. **Email Support**: Contact [support@sonatype.com](mailto:support@sonatype.com)
2. **Provide Details**: Include your username and request OSSRH access
3. **Wait for Response**: Usually processed within 24-48 hours

## Step 4: Configure Project

### 4.1 Create Environment File

Create a `.env` file in the project root:

```bash
# OSSRH (Maven Central) credentials
OSSRH_USERNAME=your-token-username
OSSRH_PASSWORD=your-token-password

# GPG signing configuration
SIGNING_KEY_ID=your-actual-gpg-key-id
SIGNING_PASSWORD=your-actual-gpg-password

# Optional: Path to GPG keyring file
# SIGNING_SECRET_KEY_RING_FILE=path/to/your/secring.gpg
```

### 4.2 Update POM Information

In `client/build.gradle.kts`, verify the POM information:

```kotlin
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "co.tryinhouse.android"
            artifactId = "sdk"
            version = "1.0.0"

            pom {
                name.set("TryInhouse Android SDK")
                description.set("Android SDK for tracking app installs, app opens, and user interactions with shortlinks")
                url.set("https://github.com/28harishkumar/tryinhouse-android-sdk")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("focks-chandan")
                        name.set("Chandan Singha")
                        email.set("ck@tryinhouse.co")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/28harishkumar/tryinhouse-android-sdk.git")
                    developerConnection.set("scm:git:ssh://github:28harishkumar/tryinhouse-android-sdk.git")
                    url.set("https://github.com/28harishkumar/tryinhouse-android-sdk/tree/master")
                }
            }
        }
    }
}
```

## Step 5: Test Publishing

### 5.1 Build and Test

```bash
# Clean and build
./gradlew clean build

# Run tests
./gradlew test

# Generate documentation
./gradlew dokkaHtml
```

### 5.2 Test Publishing (Optional)

```bash
# Publish to local Maven repository for testing
./gradlew :client:publishReleasePublicationToMavenLocal -PenableSigning=true
```

### 5.3 Publish to Maven Central

```bash
# Publish to Maven Central
./gradlew :client:publishReleasePublicationToSonatypeRepository -PenableSigning=true

# Close and release staging repository
./gradlew closeAndReleaseSonatypeStagingRepository
```

## Step 6: GitHub Actions Setup

### 6.1 Add Repository Secrets

Add these secrets to your GitHub repository:

| Secret Name              | Description                   | Value                            |
| ------------------------ | ----------------------------- | -------------------------------- |
| `OSSRH_USERNAME`         | Central Portal token username | Your token username              |
| `OSSRH_PASSWORD`         | Central Portal token password | Your token password              |
| `SIGNING_KEY_ID`         | GPG key ID                    | Your GPG key ID (e.g., 9ABCDEF0) |
| `SIGNING_KEY_PASSPHRASE` | GPG key passphrase            | Your GPG key passphrase          |
| `SIGNING_KEY`            | GPG private key               | Base64 encoded private key       |

### 6.2 Export GPG Private Key

```bash
# Export your private key as base64
gpg --armor --export-secret-keys YOUR_KEY_ID | base64 -w0

# Copy the output and add it as the SIGNING_KEY secret
```

### 6.3 Test GitHub Actions

1. **Create a tag**: `git tag v1.0.0`
2. **Push the tag**: `git push origin v1.0.0`
3. **Monitor the workflow**: Check the Actions tab in GitHub

## Step 7: Verify Publication

### 7.1 Check Maven Central

After successful publishing, verify your artifact:

1. **Search Maven Central**: [search.maven.org](https://search.maven.org/)
2. **Search for**: `co.tryinhouse.android:sdk`
3. **Verify Version**: Check that your version appears

### 7.2 Test Usage

Create a test project and verify the dependency:

```kotlin
dependencies {
    implementation("co.tryinhouse.android:sdk:1.0.0")
}
```

## Troubleshooting

### Common Issues

1. **401 Unauthorized**:

   - Verify your Central Portal credentials
   - Ensure you're using token credentials, not UI credentials
   - Wait a few minutes after generating tokens

2. **GPG Signing Failed**:

   - Verify your GPG key ID is correct
   - Ensure the passphrase is provided
   - Check that the private key is properly imported

3. **Namespace Issues**:

   - Contact Central Support for namespace verification
   - Ensure your domain or GitHub account is properly verified

4. **Validation Errors**:
   - Check that POM information is complete
   - Ensure all required fields are present
   - Verify license and SCM information

### Getting Help

- **Central Portal Support**: [support@sonatype.com](mailto:support@sonatype.com)
- **Documentation**: [Central Portal Docs](https://central.sonatype.com/)
- **GitHub Issues**: Create an issue in this repository

## Modern Central Portal Workflow

This project uses the modern Central Portal workflow (post-July 2025) which provides:

- ✅ **Simplified Process**: No manual staging repository management
- ✅ **Automatic Validation**: Built-in checks for common issues
- ✅ **Direct Publishing**: Direct upload to Central Portal
- ✅ **Better Error Handling**: Clear error messages and validation
- ✅ **Future-Proof**: Uses the recommended workflow

The nexus-publish plugin handles the complexity of the publishing process, making it much easier to publish to Maven Central compared to the old OSSRH workflow.

## Next Steps

1. **Update Version**: Change the version in `client/build.gradle.kts` for new releases
2. **Create Tags**: Use semantic versioning (e.g., `v1.0.1`, `v1.1.0`)
3. **Automate**: Set up GitHub Actions for automated publishing
4. **Document**: Update documentation for users

For detailed publishing instructions, see [PUBLISHING.md](PUBLISHING.md).
