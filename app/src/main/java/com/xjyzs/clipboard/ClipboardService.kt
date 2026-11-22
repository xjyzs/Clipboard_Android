package com.xjyzs.clipboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object MainStateFlow {
    var _status = MutableStateFlow(Status.DISCONNECTED)
    val status = _status.asStateFlow()
    var _clipboardText = MutableStateFlow("")
    val clipboardText = _clipboardText.asStateFlow()
    var _sourcePkg = MutableStateFlow("")
    val sourcePkg = _sourcePkg.asStateFlow()
    var _shouldCopy = MutableStateFlow(false)
    var shouldCopy = _shouldCopy.asStateFlow()
    var _remoteTxt = MutableStateFlow<String?>(null)
    val remoteTxt = _remoteTxt.asStateFlow()
    val _latestContent = MutableSharedFlow<String>(replay = 1)
    val latestContent = _latestContent.asSharedFlow()
    val _log = MutableStateFlow("")
    val log = _log.asStateFlow()
}

class ClipboardService : Service() {
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == "com.xjyzs.clipboard.CLIPBOARD_RECEIVER") {
                MainStateFlow._clipboardText.value = intent.getStringExtra("txt") ?: ""
                MainStateFlow._sourcePkg.value = intent.getStringExtra("sourcePackage") ?: ""
            }
            val socket = SocketHandler.getSocket()
            if (socket?.connected() == true) {
                if (MainStateFlow.sourcePkg.value != context?.packageName) {
                    socket.send(MainStateFlow.clipboardText.value)
                    MainStateFlow._shouldCopy.value = false
                }
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("com.xjyzs.clipboard.CLIPBOARD_RECEIVER")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) this.registerReceiver(
            receiver, filter, RECEIVER_EXPORTED
        )
        else ContextCompat.registerReceiver(
            this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val channel = NotificationChannel(
            "clipboard", getString(R.string.network_clipboard), NotificationManager.IMPORTANCE_LOW
        ).apply {}
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, "clipboard")
            .setContentTitle(getString(R.string.network_clipboard_started))
            .setSmallIcon(R.drawable.ic_launcher_foreground).build()
        startForeground(1002, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra("url")
            ?: getSharedPreferences("settings", MODE_PRIVATE).getString("url", "")!!
        SocketConnector.startOnce(this, url)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHandler.closeConnection()
        unregisterReceiver(receiver)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        AppState.isRunning = false
        TileService.requestListeningState(
            this.applicationContext, ComponentName(this, AppLauncherTileService::class.java)
        )
        stopSelf()
    }
}
