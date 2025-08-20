package com.artix.callnote.phone

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.artix.callnote.CallNoteApp
import com.artix.callnote.R
import com.artix.callnote.data.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val number = intent?.getStringExtra(EXTRA_NUMBER)
        startForeground(NOTIF_ID, buildNotification())
        if (number != null) {
            showOverlay(number)
        } else {
            removeOverlay()
        }
        return START_STICKY
    }

    private fun showOverlay(number: String) {
        if (!Settings.canDrawOverlays(this)) return
        val existing = composeView
        if (existing != null) {
            updateOverlay(existing, number)
            return
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 120
        }
        val view = ComposeView(this)
        view.setContent {
            MaterialTheme {
                OverlayContent(phoneNumber = number)
            }
        }
        composeView = view
        windowManager.addView(view, params)
    }

    private fun updateOverlay(view: ComposeView, number: String) {
        view.setContent {
            MaterialTheme {
                OverlayContent(phoneNumber = number)
            }
        }
    }

    private fun removeOverlay() {
        composeView?.let {
            try { windowManager.removeView(it) } catch (_: Throwable) {}
            composeView = null
        }
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val activityIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pi = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        return NotificationCompat.Builder(this, CallNoteApp.OVERLAY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.overlay_notification))
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val EXTRA_NUMBER = "extra_number"
        private const val NOTIF_ID = 1001

        fun showOverlay(context: Context, number: String) {
            val i = Intent(context, OverlayService::class.java).putExtra(EXTRA_NUMBER, number)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i)
            } else {
                context.startService(i)
            }
        }

        fun hideOverlay(context: Context) {
            context.stopService(Intent(context, OverlayService::class.java))
        }
    }
}

@Composable
private fun OverlayContent(phoneNumber: String) {
    val repository = remember { NoteRepository.get(LocalContext.current) }
    var note by remember { mutableStateOf("Loading…") }

    LaunchedEffect(phoneNumber) {
        val result = repository.find(phoneNumber)
        note = result?.content?.ifBlank { "(No note saved)" } ?: "(No note saved)"
    }

    Surface(tonalElevation = 6.dp) {
        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.padding(12.dp)) {
            Text(text = "Note for $phoneNumber", fontSize = 14.sp)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))
            Text(text = note, fontSize = 16.sp)
        }
    }
}
