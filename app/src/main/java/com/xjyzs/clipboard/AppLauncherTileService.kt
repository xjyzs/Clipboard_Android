package com.xjyzs.clipboard

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class AppLauncherTileService : TileService() {
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else startActivityAndCollapse(intent)
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        if (AppState.isRunning) tile.state = Tile.STATE_ACTIVE
        else tile.state = Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onBind(intent: Intent?): IBinder? {
        requestListeningState(
            applicationContext,
            ComponentName(applicationContext, AppLauncherTileService::class.java)
        )
        return super.onBind(intent)
    }
}