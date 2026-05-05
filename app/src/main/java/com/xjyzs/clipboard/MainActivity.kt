package com.xjyzs.clipboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xjyzs.clipboard.ui.theme.ClipboardTheme
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClipboardTheme {
                GradientBackground {
                    MainUI()
                }
            }
        }
    }
}

@SuppressLint("BatteryLife")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainUI() {
    val context = LocalContext.current
    val status by MainStateFlow.status.collectAsStateWithLifecycle()
    val remoteTxt by MainStateFlow.remoteTxt.collectAsStateWithLifecycle()
    val log by MainStateFlow.log.collectAsStateWithLifecycle()
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(true) }
    val pref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                context as Activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2
            )
        }
        val url = pref.getString("url", "")!!
        val serIntent = Intent(context, ClipboardService::class.java).apply {
            putExtra("url", pref.getString("url", "")!!)
        }
        ContextCompat.startForegroundService(context, serIntent)
        if (url.isEmpty()) {
            context.startActivity(Intent(context, WelcomeActivity::class.java))
        }
        isIgnoringBatteryOptimizations = isIgnoringBatteryOptimizations(context)
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(log) {
        if (log.isNotEmpty())
            scope.launch { snackbarHostState.showSnackbar(log) }
    }

    if (!isIgnoringBatteryOptimizations) {
        AlertDialog(
            {},
            {
                TextButton({
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = "package:${context.packageName}".toUri()
                    context.startActivity(intent)
                    isIgnoringBatteryOptimizations = true
                }) { Text("去设置") }
            },
            dismissButton = {
                TextButton({
                    isIgnoringBatteryOptimizations = true
                }) { Text("取消") }
            },
            title = { Text("权限申请") },
            text = { Text("在手机系统设置中，允许网络剪贴板保持后台运行，否则可能无法及时同步剪贴板") })
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val density = LocalDensity.current
    var textFieldHeightPx by remember { mutableIntStateOf(0) }
    val bgColor by rememberUpdatedState(
        if (!isSystemInDarkTheme()) Color(0xFFE4EADC) else Color(
            0xFF2A2A28
        )
    )
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(stringResource(R.string.app_name))
                        Spacer(Modifier.width(4.dp))
                        Badge(
                            containerColor = if (status == Status.DISCONNECTED) Color.Red else Color(
                                0xFF0BCC00
                            )
                        ) {
                            if (status == Status.DISCONNECTED) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LoadingIndicator(Modifier.size(14.dp), color = Color.White)
                                    Text("连接中...", fontSize = 12.sp, color = Color.White)
                                }
                            } else {
                                Text("已连接", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }, actions = {
                    IconButton(
                        {
                            val actIntent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(actIntent)
                        }, colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        )
                    ) { Icon(Icons.Default.Settings, null) }
                }, colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ), scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent
    ) { innerPadding ->
        val hazeState = rememberHazeState()
        Column(
            Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val h = with(density) { textFieldHeightPx.toDp() + 20.dp }
            Spacer(Modifier.height(h))
            SelectionContainer {
                Column(
                    Modifier.fillMaxSize()
                ) {
                    if (remoteTxt != null) {
                        Text(remoteTxt.toString())
                    } else {
                        LoadingIndicator()
                    }
                }
            }
        }
        Row(
            Modifier
                .padding(innerPadding)
                .onSizeChanged { size -> textFieldHeightPx = size.height },
            verticalAlignment = Alignment.CenterVertically
        ) {
            var txtToSend by remember { mutableStateOf("") }
            AppTextField(
                txtToSend,
                { txtToSend = it },
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurRadius = 10.dp
                            backgroundColor = bgColor
                        }
                    },
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        val socket = SocketHandler.getSocket()
                        if (socket?.connected() == true) {
                            socket.send(txtToSend)
                            MainStateFlow._shouldCopy.value = false
                        }
                        txtToSend = ""
                    }
                    .hazeEffect(state = hazeState) {
                        blurEffect {
                            blurRadius = 10.dp
                            backgroundColor = bgColor
                        }
                    }, contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.ArrowUpward, null) }
        }
    }
}
