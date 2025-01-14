package com.moliveira.app.smartfridge.modules.sdk

import platform.Foundation.NSProcessInfo

actual fun isRunningOnSimulator(): Boolean {
    val processInfo = NSProcessInfo.processInfo.environment
    return processInfo["SIMULATOR_DEVICE_NAME"] != null
}