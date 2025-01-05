plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    alias(libs.plugins.kotlinAndroid).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinCocoapods).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)

    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.mokoResources) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

tasks.register<Exec>("archiveForIos") {
    group = "build"

    val xcodeWS = "iosApp/iosApp.xcworkspace"  // Adjust this to your Xcode project path
    val scheme = "iosApp"  // Replace with your actual scheme name
    val sdk = "iphoneos"   // SDK for iOS device builds

    commandLine(
        "xcodebuild",
        "-workspace", xcodeWS,
        "-scheme", scheme,
        "-sdk", sdk,
        "-configuration", "Release",
        "archive",
        "-archivePath", "$buildDir/iosArchives/$scheme.xcarchive",
    )
}

tasks.register<Exec>("exportIosArchive") {
    group = "build"
    description = "Export iOS archive to an .ipa file"

    val archivePath = "$buildDir/iosArchives/iosApp.xcarchive"
    val exportPath = "$buildDir/iosArchives/Export"
    val exportOptionsPlist = "iosApp/ExportOptions.plist"  // Path to your export options plist file

    commandLine(
        "xcodebuild",
        "-exportArchive",
        "-archivePath", archivePath,
        "-exportPath", exportPath,
        "-exportOptionsPlist", exportOptionsPlist,
    )
}