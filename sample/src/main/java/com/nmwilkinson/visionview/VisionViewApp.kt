package com.nmwilkinson.visionview

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class VisionViewApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
        }
    }
}