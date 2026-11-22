package com.xjyzs.clipboard

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val url = getSharedPreferences("settings", MODE_PRIVATE).getString("url", "")!!
        if (url.isNotEmpty()) {
            SocketConnector.startOnce(this, url)
        }
    }
}
