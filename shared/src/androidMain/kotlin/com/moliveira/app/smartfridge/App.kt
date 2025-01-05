package com.moliveira.app.smartfridge

import android.app.Application
import android.view.Surface
import com.moliveira.app.smartfridge.modules.camera.ORIENTATIONS

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidKmmSetup.setup(this)

        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }
}