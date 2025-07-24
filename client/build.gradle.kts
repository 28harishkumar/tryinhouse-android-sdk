plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

// Set properties from environment variables
project.ext["ossrhUsername"] = System.getProperty("OSSRH_USERNAME") ?: ""
project.ext["ossrhPassword"] = System.getProperty("OSSRH_PASSWORD") ?: ""
project.ext["signingKeyId"] = System.getProperty("SIGNING_KEY_ID") ?: ""
project.ext["signingPassword"] = System.getProperty("SIGNING_PASSWORD") ?: ""
project.ext["signingSecretKeyRingFile"] = System.getProperty("SIGNING_SECRET_KEY_RING_FILE") ?: ""

android {
    namespace = "co.tryinhouse.android"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Dokka configuration
tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    // Install Referrer
    implementation("com.android.installreferrer:installreferrer:2.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

// Create Javadoc JAR from Dokka output
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaHtml.get().outputDirectory)
    dependsOn(tasks.dokkaHtml)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(javadocJar.get())
                
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
                        developerConnection.set("scm:git:ssh://github.com:28harishkumar/tryinhouse-android-sdk.git")
                        url.set("https://github.com/28harishkumar/tryinhouse-android-sdk/tree/master")
                    }
                }
            }
            
            create<MavenPublication>("snapshot") {
                from(components["release"])
                artifact(javadocJar.get())
                
                groupId = "co.tryinhouse.android"
                artifactId = "sdk"
                version = "1.0.0-SNAPSHOT"
                
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
        
        repositories {
            maven {
                name = "OSSRH"
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = project.findProperty("ossrhUsername") as String?
                    password = project.findProperty("ossrhPassword") as String?
                }
            }
            maven {
                name = "OSSRH-Snapshots"
                url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                credentials {
                    username = project.findProperty("ossrhUsername") as String?
                    password = project.findProperty("ossrhPassword") as String?
                }
            }
        }
    }
    
    tasks.findByName("publishReleasePublicationToMavenLocal")?.let { publishTask ->
        publishTask.dependsOn(tasks.named("releaseSourcesJar"))
        publishTask.dependsOn(javadocJar)
    }
    tasks.findByName("generateMetadataFileForReleasePublication")?.let { metaTask ->
        metaTask.dependsOn(tasks.named("releaseSourcesJar"))
        metaTask.dependsOn(javadocJar)
    }
    
    // Configure signing after publications are created
    // Only enable signing for production releases
    if (project.hasProperty("enableSigning") && project.property("enableSigning") == "true") {
        signing {
            val signingKeyId: String? by project
            val signingPassword: String? by project
            val signingSecretKeyRingFile: String? by project
            
            // Use the existing keyring file directly
            if (signingSecretKeyRingFile != null && signingKeyId != null && signingPassword != null) {
                useInMemoryPgpKeys(signingKeyId, signingSecretKeyRingFile, signingPassword)
            } else if (signingKeyId != null && signingPassword != null) {
                // Fallback to in-memory signing
                useInMemoryPgpKeys(signingKeyId, signingPassword)
            }
            sign(publishing.publications["release"])
        }
    }
}