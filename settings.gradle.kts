import java.net.URI

rootProject.name = "SmartFridge"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = URI("s3://micka-maven-repo")
            credentials(AwsCredentials::class.java) {
                accessKey = "AKIA5F6SSNJYTH672QBZ" // System.getenv("MOLIVE_MAVEN_ACCESS_KEY")
                secretKey = "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" // System.getenv("MOLIVE_MAVEN_PRIVATE_KEY")
            }
        }
    }
}

include(":composeApp")