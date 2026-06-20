package com.xjyzs.clipboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xjyzs.clipboard.ui.theme.ClipboardTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClipboardTheme {
                Surface {WelcomeUI()}
            }
        }
    }
}

@Composable
fun WelcomeUI() {
    val context = LocalContext.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null)
            Text(stringResource(R.string.welcome_to_network_clipboard), fontSize = 32.sp)
            Text(stringResource(R.string.free_open_source_cross_device_clipboard_sync_tool), color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.size(40.dp))
            Button({
                context.startActivity(Intent(context, SettingsActivity::class.java))
                (context as ComponentActivity).finish()
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.start_configuration))
                    Icon(Icons.Default.ArrowUpward, null, Modifier.rotate(90f))
                }
            }
        }
    }
}