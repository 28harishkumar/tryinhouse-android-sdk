plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "com.inhouse.client"
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

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // Optionally, add files if you generate javadoc/dokka output
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(javadocJar.get())
                groupId = "com.inhouse"
                artifactId = "client"
                version = "1.0.0"
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
}