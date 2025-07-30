// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.central.publishing)
}

// Load environment variables from .env file
val envFile = file(".env")
if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        if (line.isNotBlank() && !line.startsWith("#")) {
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                System.setProperty(key, value)
                project.ext[key] = value
            }
        }
    }
}

// Configure nexus-publish plugin for modern Central Portal workflow
nexusPublishing {
    repositories {
        sonatype {
            // Use Central Portal URLs (modern workflow)
            nexusUrl.set(uri("https://central.sonatype.com/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(project.findProperty("OSSRH_USERNAME") as String?)
            password.set(project.findProperty("OSSRH_PASSWORD") as String?)
        }
    }
}
