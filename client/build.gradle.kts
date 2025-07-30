plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

// Set properties from environment variables or system properties
project.ext["ossrhUsername"] = System.getProperty("OSSRH_USERNAME") ?: System.getenv("OSSRH_USERNAME") ?: ""
project.ext["ossrhPassword"] = System.getProperty("OSSRH_PASSWORD") ?: System.getenv("OSSRH_PASSWORD") ?: ""
project.ext["signingKeyId"] = System.getProperty("SIGNING_KEY_ID") ?: System.getenv("SIGNING_KEY_ID") ?: ""
project.ext["signingPassword"] = System.getProperty("SIGNING_PASSWORD") ?: System.getenv("SIGNING_PASSWORD") ?: ""
project.ext["signingSecretKeyRingFile"] = System.getProperty("SIGNING_SECRET_KEY_RING_FILE") ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE") ?: ""

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

// Create sources JAR
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
    exclude("**/build/**")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                // Add javadoc JAR
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
                        developerConnection.set("scm:git:ssh://github:28harishkumar/tryinhouse-android-sdk.git")
                        url.set("https://github.com/28harishkumar/tryinhouse-android-sdk/tree/master")
                    }
                }
            }
        }
        
        // Configure repositories for Central Portal upload
        repositories {
            maven {
                name = "OSSRH"
                url = uri("https://central.sonatype.com/repository/maven-staging")
                credentials {
                    username = project.findProperty("OSSRH_USERNAME") as String?
                    password = project.findProperty("OSSRH_PASSWORD") as String?
                }
            }
            maven {
                name = "OSSRHSnapshots"
                url = uri("https://central.sonatype.com/repository/maven-snapshots")
                credentials {
                    username = project.findProperty("OSSRH_USERNAME") as String?
                    password = project.findProperty("OSSRH_PASSWORD") as String?
                }
            }
        }
    }
    
    // Configure signing
    if (project.hasProperty("enableSigning") && project.property("enableSigning") == "true") {
        println("🔐 Signing configuration:")
        val signingKeyId: String? by project
        val signingPassword: String? by project
        
        println("   Key ID: ${signingKeyId ?: "NOT SET"}")
        println("   Password: ${if (signingPassword != null) "SET" else "NOT SET"}")
        println("   OSSRH_USERNAME: ${project.findProperty("OSSRH_USERNAME") ?: "NOT SET"}")
        println("   OSSRH_PASSWORD: ${if (project.findProperty("OSSRH_PASSWORD") != null) "SET" else "NOT SET"}")
        
        if (signingKeyId != null) {
            try {
                println("   Using GPG agent for signing")
                signing {
                    useGpgCmd()
                    // Sign all publications
                    sign(publishing.publications)
                }
                println("   ✅ GPG agent signing configured successfully")
            } catch (e: Exception) {
                println("   ❌ GPG agent signing failed: ${e.message}")
                throw e
            }
        } else {
            println("⚠️  Warning: Signing credentials not found")
            throw Exception("SIGNING_KEY_ID must be set in .env file")
        }
    } else {
        println("ℹ️  Signing disabled (use -PenableSigning=true to enable)")
    }
}