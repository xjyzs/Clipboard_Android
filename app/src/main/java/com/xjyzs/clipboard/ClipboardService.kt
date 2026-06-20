package com.xjyzs.clipboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URI

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

    private fun initSocket(url: String) {
        MainStateFlow._status.value = Status.DISCONNECTED
        SocketHandler.closeConnection()
        try {
            val uri = URI(url)
            val pathString = uri.path ?: ""
            val opts = IO.Options().apply {
                transports = arrayOf("websocket")
            }
            if (pathString.isNotEmpty() && pathString != "/") {
                val portStr = if (uri.port != -1) ":${uri.port}" else ""
                val baseUrl = "${uri.scheme}://${uri.host}$portStr"
                val cleanSubRoute = pathString.removeSuffix("/")
                val formattedPath = "$cleanSubRoute/socket.io/"
                opts.path = formattedPath
                SocketHandler.setSocket(baseUrl, opts)
            } else {
                SocketHandler.setSocket(url)
            }

            SocketHandler.establishConnection()
            val socket = SocketHandler.getSocket()
            socket?.on(Socket.EVENT_CONNECT) {
                MainStateFlow._status.value = Status.CONNECTED
            }
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                MainStateFlow._shouldCopy.value = false
                MainStateFlow._log.value = getString(R.string.error_with_message, args.lastOrNull() ?: getString(R.string.connection_failed))
                MainStateFlow._status.value = Status.DISCONNECTED
            }
            socket?.on(Socket.EVENT_DISCONNECT) {
                MainStateFlow._shouldCopy.value = false
                MainStateFlow._status.value = Status.DISCONNECTED
            }
            socket?.on("message") { args ->
                val data = args.last().toString()
                MainStateFlow._remoteTxt.value = data
                if (MainStateFlow.shouldCopy.value) {
                    val cm = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(null, data))
                }
                MainStateFlow._shouldCopy.value = true
            }
        } catch (e: Exception) {
            if (e.message?.contains("parse the host") == true) {
                MainStateFlow._log.value = getString(R.string.please_configure_server_url)
            } else {
                MainStateFlow._log.value = getString(R.string.error_with_message, e.message)
            }
        }
    }

    private val prefListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "url") {
                val newUrl = sharedPreferences.getString("url", "")!!
                if (newUrl.isNotEmpty()) {
                    initSocket(newUrl)
                }
            }
        }
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter("com.xjyzs.clipboard.CLIPBOARD_RECEIVER")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            this.registerReceiver(receiver, filter, RECEIVER_EXPORTED)
        else
            ContextCompat.registerReceiver(
                this,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        getSharedPreferences("settings", MODE_PRIVATE)
            .registerOnSharedPreferenceChangeListener(prefListener)
        val channel = NotificationChannel(
            "clipboard", getString(R.string.network_clipboard), NotificationManager.IMPORTANCE_LOW
        ).apply {}
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        val notification =
            NotificationCompat.Builder(this, "clipboard").setContentTitle(getString(R.string.network_clipboard_started))
                .setSmallIcon(R.drawable.ic_launcher_foreground).build()
        startForeground(1002, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initSocket(intent!!.getStringExtra("url")!!)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHandler.closeConnection()
        unregisterReceiver(receiver)
        getSharedPreferences("settings", MODE_PRIVATE)
            .unregisterOnSharedPreferenceChangeListener(prefListener)
    }
}