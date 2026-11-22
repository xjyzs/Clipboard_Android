package com.xjyzs.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.service.quicksettings.TileService
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.net.URI
import java.util.concurrent.TimeUnit

object SocketConnector {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val classPrefixRegex = Regex("""^(?:[\w.$]+(?:Exception|Error):\s*)+""")

    private val sharedClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Volatile
    private var initialized = false
    @Volatile
    private var currentNetwork: Network? = null
    @Volatile
    private var hadNetworkLost = false

    fun startOnce(context: Context, url: String) {
        if (url.isBlank() || initialized) return
        initialized = true

        val appCtx = context.applicationContext
        scope.launch {
            initAndConnect(appCtx, url)
        }
        registerNetworkMonitor(appCtx)
    }

    private fun initAndConnect(appCtx: Context, url: String) {
        MainStateFlow._status.value = Status.DISCONNECTED
        try {
            val uri = URI(url)
            val pathString = uri.path ?: ""
            val opts = IO.Options().apply {
                transports = arrayOf("websocket")
                callFactory = sharedClient
                webSocketFactory = sharedClient
                reconnection = true
                reconnectionAttempts = Int.MAX_VALUE
                reconnectionDelay = 500L
                reconnectionDelayMax = 3000L
                randomizationFactor = 0.2
                timeout = 8000L
            }

            if (pathString.isNotEmpty() && pathString != "/") {
                val portStr = if (uri.port != -1) ":${uri.port}" else ""
                val baseUrl = "${uri.scheme}://${uri.host}$portStr"
                opts.path = "${pathString.removeSuffix("/")}/socket.io/"
                SocketHandler.setSocket(baseUrl, opts)
            } else {
                SocketHandler.setSocket(url, opts)
            }

            SocketHandler.establishConnection()
            val socket = SocketHandler.getSocket()

            socket?.on(Socket.EVENT_CONNECT) {
                MainStateFlow._status.value = Status.CONNECTED
                AppState.isRunning = true
                TileService.requestListeningState(
                    appCtx, ComponentName(appCtx, AppLauncherTileService::class.java)
                )
            }
            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                MainStateFlow._shouldCopy.value = false
                val errMsg = when (val err = args.lastOrNull()) {
                    is Throwable -> {
                        val root = generateSequence(err) { it.cause }.last()
                        val raw = root.message?.takeIf { it.isNotBlank() }
                            ?: err.message?.takeIf { it.isNotBlank() }
                            ?: appCtx.getString(R.string.connection_failed)
                        raw.replace(classPrefixRegex, "")
                    }
                    is String -> err.ifBlank { appCtx.getString(R.string.connection_failed) }
                    else -> err?.toString() ?: appCtx.getString(R.string.connection_failed)
                }
                MainStateFlow._log.value = appCtx.getString(R.string.error_with_message, errMsg)
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
                    val cm = appCtx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(null, data))
                }
                MainStateFlow._shouldCopy.value = true
            }
        } catch (e: Exception) {
            initialized = false
            val msg = e.message ?: ""
            if (msg.contains("parse the host")) {
                MainStateFlow._log.value = appCtx.getString(R.string.please_configure_server_url)
            } else {
                MainStateFlow._log.value = appCtx.getString(
                    R.string.error_with_message,
                    msg.replace(classPrefixRegex, "")
                )
            }
        }
    }

    private fun registerNetworkMonitor(appCtx: Context) {
        val cm = appCtx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val prev = currentNetwork
                currentNetwork = network

                val networkSwitched = prev != null && prev != network
                if (hadNetworkLost || networkSwitched) {
                    hadNetworkLost = false
                    scope.launch {
                        val socket = SocketHandler.getSocket() ?: return@launch
                        if (networkSwitched || !socket.connected()) {
                            socket.disconnect()
                            socket.connect()
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                if (currentNetwork == network) {
                    hadNetworkLost = true
                    MainStateFlow._status.value = Status.DISCONNECTED
                }
            }
        })
    }
}
