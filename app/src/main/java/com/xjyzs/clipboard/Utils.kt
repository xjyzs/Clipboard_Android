package com.xjyzs.clipboard

import android.content.Context
import android.os.PowerManager
import io.socket.client.IO
import io.socket.client.Socket

enum class Status {
    CONNECTED, DISCONNECTED
}

object SocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket(url: String, options: IO.Options? = null) {
        try {
            mSocket = if (options != null) {
                IO.socket(url, options)
            } else {
                IO.socket(url)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSocket(): Socket? {
        return mSocket
    }

    fun establishConnection() {
        mSocket?.connect()
    }

    fun closeConnection() {
        mSocket?.disconnect()
    }
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}