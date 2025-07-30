#!/bin/bash

# Script to prepare Maven Central archive with proper folder structure
# This creates the exact layout required by Maven Central Portal

set -e

echo "📦 Preparing Maven Central archive with proper folder structure..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
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

# Configuration
GROUP_ID="co.tryinhouse.android"
ARTIFACT_ID="sdk"
VERSION="1.0.0"

# Create the Maven repository structure
MAVEN_DIR="maven-central-archive"
rm -rf "$MAVEN_DIR"
mkdir -p "$MAVEN_DIR"

# Convert group ID to folder structure
GROUP_PATH=$(echo "$GROUP_ID" | sed 's/\./\//g')
FULL_PATH="$MAVEN_DIR/$GROUP_PATH/$ARTIFACT_ID/$VERSION"
mkdir -p "$FULL_PATH"

print_status "Creating Maven repository structure: $FULL_PATH"

# Generate all required artifacts
print_status "Generating artifacts..."

# Build release AAR
./gradlew :client:assembleRelease

# Generate POM file
./gradlew :client:generatePomFileForReleasePublication

# Generate sources JAR
./gradlew :client:releaseSourcesJar

# Generate javadoc JAR
./gradlew :client:dokkaJavadoc

print_success "All artifacts generated successfully"

# Copy files to Maven structure
print_status "Copying files to Maven structure..."

# Copy AAR file (keep .aar extension for Android libraries)
cp client/build/outputs/aar/client-release.aar "$FULL_PATH/$ARTIFACT_ID-$VERSION.aar"

# Copy POM file
cp client/build/publications/release/pom-default.xml "$FULL_PATH/$ARTIFACT_ID-$VERSION.pom"

# Copy sources JAR
cp client/build/libs/client-sources.jar "$FULL_PATH/$ARTIFACT_ID-$VERSION-sources.jar"

# Copy javadoc JAR (if it exists, otherwise create empty)
if [ -f "client/build/dokka/javadoc/client-javadoc.jar" ]; then
    cp client/build/dokka/javadoc/client-javadoc.jar "$FULL_PATH/$ARTIFACT_ID-$VERSION-javadoc.jar"
else
    print_warning "Javadoc JAR not found, creating empty one for Maven Central requirements"
    touch "$FULL_PATH/$ARTIFACT_ID-$VERSION-javadoc.jar"
fi

print_success "Files copied to Maven structure"

# Create GPG signatures
print_status "Creating GPG signatures..."
cd "$FULL_PATH"

# Sign all files
for file in *.aar *.jar *.pom; do
    if [ -f "$file" ]; then
        gpg --armor --detach-sign "$file"
        print_status "Signed: $file"
    fi
done

cd - > /dev/null

# Create checksums (MD5 and SHA1)
print_status "Creating checksums..."

cd "$FULL_PATH"

for file in *.aar *.jar *.pom; do
    if [ -f "$file" ]; then
        # Create MD5 checksum
        md5sum "$file" | cut -d' ' -f1 > "$file.md5"
        
        # Create SHA1 checksum
        shasum "$file" | cut -d' ' -f1 > "$file.sha1"
        
        print_status "Created checksums for: $file"
    fi
done

cd - > /dev/null

# Create archive
ARCHIVE_NAME="maven-central-upload.tar.gz"
print_status "Creating archive: $ARCHIVE_NAME"

cd "$MAVEN_DIR"
tar -czf "../$ARCHIVE_NAME" .
cd - > /dev/null

print_success "Archive created: $ARCHIVE_NAME"

# List all files in the structure
print_status "Maven Central archive structure:"
tree "$MAVEN_DIR"

print_success "Maven Central archive preparation complete!"
print_status "Archive file: $ARCHIVE_NAME"
print_status ""
print_status "To upload to Maven Central:"
print_status "1. Go to https://central.sonatype.com/"
print_status "2. Navigate to 'Staging Upload'"
print_status "3. Upload the archive file: $ARCHIVE_NAME"
print_status "4. Close and release the staging repository"
print_status ""
print_status "Archive contains:"
print_status "- Proper Maven repository folder structure"
print_status "- All required artifacts (JAR, POM, sources, javadoc)"
print_status "- GPG signatures (.asc files)"
print_status "- Checksums (.md5 and .sha1 files)"
print_status ""
print_status "Folder structure:"
print_status "$GROUP_ID/$ARTIFACT_ID/$VERSION/"
print_status "├── $ARTIFACT_ID-$VERSION.aar"
print_status "├── $ARTIFACT_ID-$VERSION.aar.asc"
print_status "├── $ARTIFACT_ID-$VERSION.aar.md5"
print_status "├── $ARTIFACT_ID-$VERSION.aar.sha1"
print_status "├── $ARTIFACT_ID-$VERSION.pom"
print_status "├── $ARTIFACT_ID-$VERSION.pom.asc"
print_status "├── $ARTIFACT_ID-$VERSION.pom.md5"
print_status "├── $ARTIFACT_ID-$VERSION.pom.sha1"
print_status "├── $ARTIFACT_ID-$VERSION-sources.jar"
print_status "├── $ARTIFACT_ID-$VERSION-sources.jar.asc"
print_status "├── $ARTIFACT_ID-$VERSION-sources.jar.md5"
print_status "├── $ARTIFACT_ID-$VERSION-sources.jar.sha1"
print_status "├── $ARTIFACT_ID-$VERSION-javadoc.jar"
print_status "├── $ARTIFACT_ID-$VERSION-javadoc.jar.asc"
print_status "├── $ARTIFACT_ID-$VERSION-javadoc.jar.md5"
print_status "└── $ARTIFACT_ID-$VERSION-javadoc.jar.sha1" 