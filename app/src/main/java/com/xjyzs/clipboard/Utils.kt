package com.xjyzs.clipboard

import android.content.Context
import android.os.PowerManager
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

enum class Status {
    CONNECTED, DISCONNECTED
}

object SocketHandler {
    private var mSocket: Socket? = null

    @Synchronized
    fun setSocket(url: String) {
        try {
            if (mSocket == null) {
                mSocket = IO.socket(url)
            }
        } catch (e: URISyntaxException) {
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