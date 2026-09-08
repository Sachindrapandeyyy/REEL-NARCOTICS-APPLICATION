package com.zenith.focus

import android.app.Application

class ZenithApplication : Application() {
    companion object {
        lateinit var instance: ZenithApplication
            private set
    }

    lateinit var container: ZenithAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = ZenithAppContainer(this)
    }
}
