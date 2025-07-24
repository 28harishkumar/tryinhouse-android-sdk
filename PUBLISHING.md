# Publishing to Maven Central

This guide explains how to publish the TryInhouse Android SDK to Maven Central.

## Prerequisites

1. **OSSRH Account**: Create an account at [OSSRH](https://s01.oss.sonatype.org/)
2. **GPG Key**: Generate a GPG key for signing artifacts
3. **GitHub Repository**: Ensure your repository is public and has proper documentation

## Setup

### 1. Configure Credentials

Update `gradle.properties` with your actual credentials:

```properties
# OSSRH (Maven Central) credentials
ossrhUsername=your-actual-ossrh-username
ossrhPassword=your-actual-ossrh-password

# GPG signing configuration
signingKeyId=your-actual-gpg-key-id
signingPassword=your-actual-gpg-password
signingSecretKeyRingFile=path/to/your/secring.gpg
```

### 2. Update POM Information

In `client/build.gradle.kts`, update the POM information:

```kotlin
pom {
    name.set("TryInhouse Android SDK")
    description.set("Android SDK for tracking app installs, app opens, and user interactions with shortlinks")
    url.set("https://github.com/your-actual-username/tryinhouse-android-sdk")

    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }

    developers {
        developer {
            id.set("your-actual-username")
            name.set("Your Actual Name")
            email.set("your-actual-email@example.com")
        }
    }

    scm {
        connection.set("scm:git:git://github.com/your-actual-username/tryinhouse-android-sdk.git")
        developerConnection.set("scm:git:ssh://github.com:your-actual-username/tryinhouse-android-sdk.git")
        url.set("https://github.com/your-actual-username/tryinhouse-android-sdk/tree/main")
    }
}
```

### 3. Update Version

Update the version in `client/build.gradle.kts`:

```kotlin
version = "1.0.0" // Change to your desired version
```

## Publishing Process

### 1. Build and Test

```bash
./gradlew clean build
./gradlew test
```

### 2. Generate Documentation

```bash
./gradlew dokkaHtml
```

### 3. Publish to Staging

```bash
./gradlew publishReleasePublicationToOSSRHRepository
```

### 4. Release from Staging

1. Go to [OSSRH Staging](https://s01.oss.sonatype.org/#stagingRepositories)
2. Find your staging repository
3. Close the repository
4. Release the repository

## Usage

After publishing, users can include your SDK in their projects:

```kotlin
// build.gradle.kts
dependencies {
    implementation("co.tryinhouse.android:sdk:1.0.0")
}
```

## Troubleshooting

### Common Issues

1. **Authentication Failed**: Check your OSSRH credentials
2. **Signing Failed**: Verify your GPG key configuration
3. **Validation Errors**: Ensure POM information is complete and accurate

### Validation Requirements

- [ ] Repository is public
- [ ] POM contains all required fields
- [ ] Artifacts are properly signed
- [ ] Documentation is generated
- [ ] License is specified
- [ ] SCM information is correct

## CI/CD Integration

For automated publishing, you can use GitHub Actions or other CI/CD platforms. Store sensitive credentials as secrets and use them in your workflow.

Example GitHub Actions workflow:

```yaml
name: Publish to Maven Central

on:
  push:
    tags:
      - "v*"

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: "11"
      - run: ./gradlew publishReleasePublicationToOSSRHRepository
        env:
          ossrhUsername: ${{ secrets.OSSRH_USERNAME }}
          ossrhPassword: ${{ secrets.OSSRH_PASSWORD }}
          signingKeyId: ${{ secrets.SIGNING_KEY_ID }}
          signingPassword: ${{ secrets.SIGNING_PASSWORD }}
          signingSecretKey: ${{ secrets.SIGNING_SECRET_KEY }}
```
