# Maven Central Archive - Ready for Upload

## ✅ Archive Created Successfully

The Maven Central archive has been created with the proper folder structure and all required files.

## 📦 Archive Details

- **Archive File**: `maven-central-upload.tar.gz` (72.8 KB)
- **Folder Structure**: Follows Maven Repository Layout convention
- **Group ID**: `co.tryinhouse.android`
- **Artifact ID**: `sdk`
- **Version**: `1.0.0`

## 📁 Folder Structure

The archive contains the following structure when extracted:

```
co/
└── tryinhouse/
    └── android/
        └── sdk/
            └── 1.0.0/
                ├── sdk-1.0.0.aar                    # Main Android library
                ├── sdk-1.0.0.aar.asc                # GPG signature
                ├── sdk-1.0.0.aar.md5                # MD5 checksum
                ├── sdk-1.0.0.aar.sha1               # SHA1 checksum
                ├── sdk-1.0.0.pom                    # Maven metadata
                ├── sdk-1.0.0.pom.asc                # GPG signature
                ├── sdk-1.0.0.pom.md5                # MD5 checksum
                ├── sdk-1.0.0.pom.sha1               # SHA1 checksum
                ├── sdk-1.0.0-sources.jar            # Source code
                ├── sdk-1.0.0-sources.jar.asc        # GPG signature
                ├── sdk-1.0.0-sources.jar.md5        # MD5 checksum
                ├── sdk-1.0.0-sources.jar.sha1       # SHA1 checksum
                ├── sdk-1.0.0-javadoc.jar            # Documentation
                ├── sdk-1.0.0-javadoc.jar.asc        # GPG signature
                ├── sdk-1.0.0-javadoc.jar.md5        # MD5 checksum
                └── sdk-1.0.0-javadoc.jar.sha1       # SHA1 checksum
```

## 🚀 Upload Process

### Step 1: Access Maven Central Portal

1. Go to [OSSRH Central Portal](https://central.sonatype.com/)
2. Sign in with your OSSRH credentials
3. Navigate to "Staging Upload"

### Step 2: Upload Archive

1. Click "Upload" or "Choose File"
2. Select the archive file: `maven-central-upload.tar.gz`
3. The portal will automatically extract and validate the structure

### Step 3: Validate and Release

1. The portal will validate all files and checksums
2. Go to "Staging Repositories"
3. Find your staging repository
4. Click "Close" to validate the artifacts
5. Click "Release" to publish to Maven Central

## ✅ Validation Checklist

The archive includes all required components:

- ✅ **Proper Maven folder structure** (`co/tryinhouse/android/sdk/1.0.0/`)
- ✅ **Main artifact** (`sdk-1.0.0.aar` - Android library)
- ✅ **POM file** (`sdk-1.0.0.pom`) with complete metadata
- ✅ **Sources JAR** (`sdk-1.0.0-sources.jar`)
- ✅ **Javadoc JAR** (`sdk-1.0.0-javadoc.jar`)
- ✅ **GPG signatures** (`.asc` files for all artifacts)
- ✅ **MD5 checksums** (`.md5` files for all artifacts)
- ✅ **SHA1 checksums** (`.sha1` files for all artifacts)

## 🔍 Verification Commands

```bash
# Check archive contents
tar -tzf maven-central-upload.tar.gz

# Extract and verify structure
tar -xzf maven-central-upload.tar.gz
find co/tryinhouse/android/sdk/1.0.0 -type f | sort

# Verify GPG signatures
cd co/tryinhouse/android/sdk/1.0.0
gpg --verify sdk-1.0.0.aar.asc sdk-1.0.0.aar
gpg --verify sdk-1.0.0.pom.asc sdk-1.0.0.pom
```

## 📋 POM File Contents

The POM file includes:

- ✅ Group ID: `co.tryinhouse.android`
- ✅ Artifact ID: `sdk`
- ✅ Version: `1.0.0`
- ✅ All dependencies with proper scopes
- ✅ License information (Apache 2.0)
- ✅ Developer contact details
- ✅ SCM repository links

## 🎯 After Successful Upload

Once published to Maven Central, users can include the SDK:

```gradle
dependencies {
    implementation 'co.tryinhouse.android:sdk:1.0.0'
}
```

## 🔄 Regenerate Archive

To regenerate the archive with updated code:

```bash
./prepare-maven-central-archive.sh
```

## 📞 Support

If you encounter any issues:

1. Check that your OSSRH account is approved for group ID `co.tryinhouse.android`
2. Verify your GPG key is properly configured
3. Ensure all files in the archive are valid

The archive is now ready for upload to Maven Central! 🚀
