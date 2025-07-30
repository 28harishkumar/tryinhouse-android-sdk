# Publishing to Maven Central

This guide explains how to publish the TryInhouse Android SDK to Maven Central using the modern Central Portal workflow with the **nexus-publish plugin**.

## Prerequisites

1. **Central Portal Account**: Create an account at [Central Portal](https://central.sonatype.com/)
2. **GPG Key**: Generate a GPG key for signing artifacts
3. **GitHub Repository**: Ensure your repository is public and has proper documentation

## Setup

### 1. Configure Credentials

Create a `.env` file in the project root with your credentials:

```bash
# OSSRH (Maven Central) credentials
OSSRH_USERNAME=your-actual-ossrh-username
OSSRH_PASSWORD=your-actual-ossrh-password

# GPG signing configuration
SIGNING_KEY_ID=your-actual-gpg-key-id
SIGNING_PASSWORD=your-actual-gpg-password
SIGNING_SECRET_KEY_RING_FILE=path/to/your/secring.gpg
```

### 2. Update POM Information

In `client/build.gradle.kts`, update the POM information in the `publishing` block:

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
                    developerConnection.set("scm:git:ssh://github:your-actual-username/tryinhouse-android-sdk.git")
                    url.set("https://github.com/your-actual-username/tryinhouse-android-sdk/tree/main")
                }
            }
        }
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

### 3. Publish to Maven Central

```bash
./gradlew :client:publishReleasePublicationToSonatypeRepository -PenableSigning=true
```

The nexus-publish plugin will automatically:

- Sign your artifacts with GPG
- Upload to the Central Portal (modern workflow)
- Handle the staging and release process
- Validate your artifacts

## Usage

After publishing, users can include your SDK in their projects:

```kotlin
// build.gradle.kts
dependencies {
    implementation("co.tryinhouse.android:sdk:1.0.0")
}
```

## Modern Central Portal Workflow

The nexus-publish plugin provides several advantages:

1. **Simplified Process**: No manual staging repository management
2. **Automatic Validation**: Built-in checks for common issues
3. **Direct Publishing**: Direct upload to Central Portal
4. **Better Error Handling**: Clear error messages and validation
5. **Modern Workflow**: Uses the recommended Central Portal workflow (post-July 2025)

### Available Tasks

- `publishReleasePublicationToSonatypeRepository`: Publishes to Maven Central
- `publishToSonatype`: Publishes all publications to Sonatype
- `closeAndReleaseSonatypeStagingRepository`: Closes and releases staging repository
- `closeSonatypeStagingRepository`: Closes a staging repository
- `releaseSonatypeStagingRepository`: Releases a staging repository

## Configuration Details

### Root build.gradle.kts

The root build.gradle.kts configures the nexus-publish plugin for the modern Central Portal workflow:

```kotlin
allprojects {
    apply(plugin = "io.github.gradle-nexus.publish-plugin")

    nexusPublishing {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://central.sonatype.com/"))
                snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
                username.set(project.findProperty("OSSRH_USERNAME") as String?)
                password.set(project.findProperty("OSSRH_PASSWORD") as String?)
            }
        }
    }
}
```

### Client build.gradle.kts

The client module configures the publishing repositories for the Central Portal:

```kotlin
repositories {
    maven {
        name = "OSSRH"
        url = uri("https://central.sonatype.com/repository/maven-releases/")
        credentials {
            username = project.findProperty("OSSRH_USERNAME") as String?
            password = project.findProperty("OSSRH_PASSWORD") as String?
        }
    }
    maven {
        name = "OSSRHSnapshots"
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        credentials {
            username = project.findProperty("OSSRH_USERNAME") as String?
            password = project.findProperty("OSSRH_PASSWORD") as String?
        }
    }
}
```

## Migration from Old Workflow

This project has been configured for the modern Central Portal workflow:

### Configuration:

1. **Plugin**: Using `io.github.gradle-nexus.publish-plugin` version 1.3.0
2. **Repository**: Configured for `central.sonatype.com`
3. **Workflow**: Modern Central Portal workflow
4. **Task Names**: Using `SonatypeRepository` tasks

### Key Features:

- ✅ Modern Central Portal workflow (recommended post-July 2025)
- ✅ Simplified configuration in root `build.gradle.kts`
- ✅ Automatic GPG signing integration
- ✅ Streamlined publishing process

## Troubleshooting

### Common Issues

1. **Authentication Failed**: Check your Central Portal credentials
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
      - run: ./gradlew :client:publishReleasePublicationToSonatypeRepository -PenableSigning=true
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
          SIGNING_PASSWORD: ${{ secrets.SIGNING_KEY_PASSPHRASE }}
          SIGNING_SECRET_KEY: ${{ secrets.SIGNING_SECRET_KEY }}
```

## Migration from OSSRH

If you're migrating from the old OSSRH workflow:

1. **Update Plugin**: Use `nexus-publish` plugin version 1.3.0 or later
2. **Update Configuration**: Configure for Central Portal in root build.gradle.kts
3. **Update Tasks**: Use `publishReleasePublicationToSonatypeRepository` instead of old OSSRH tasks
4. **Update Credentials**: Use Central Portal credentials instead of OSSRH

The nexus-publish plugin with Central Portal workflow is the recommended approach for publishing to Maven Central and provides a modern, streamlined workflow that's future-proof.
