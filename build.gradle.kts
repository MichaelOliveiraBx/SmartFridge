plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.mokoResources) apply false
}


tasks.register("archiveForIos") {
    group = "build"
    description = "Build and archive iOS framework"

    doLast {
        // Path to your Xcode workspace or project
        val xcodeProjectPath = "iosApp/iosApp.xcodeproj"  // Adjust this to your Xcode project path
        val scheme = "iosApp"  // Replace with your actual scheme name
        val sdk = "iphoneos"   // SDK for iOS device builds

        exec {
            commandLine(
                "xcodebuild",
                "-project", xcodeProjectPath,
                "-scheme", scheme,
                "-sdk", sdk,
                "-configuration", "Release",
                "archive",
                "-archivePath", "$buildDir/iosArchives/$scheme.xcarchive",
            )
        }
    }
}

tasks.register("exportIosArchive") {
    group = "build"
    description = "Export iOS archive to an .ipa file"

    doLast {
        val archivePath = "$buildDir/iosArchives/iosApp.xcarchive"
        val exportPath = "$buildDir/iosArchives/Export"
        val exportOptionsPlist = "iosApp/iosApp/Info.plist"  // Path to your export options plist file

        exec {
            commandLine(
                "xcodebuild",
                "-exportArchive",
                "-archivePath", archivePath,
                "-exportPath", exportPath,
                "-exportOptionsPlist", exportOptionsPlist,
            )
        }
    }
}