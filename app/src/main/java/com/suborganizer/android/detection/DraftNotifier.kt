package com.suborganizer.android.detection

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.suborganizer.android.R
import com.suborganizer.android.data.model.DraftSubscription
import com.suborganizer.android.util.Format

private const val CHANNEL_ID = "draft_detected"

/**
 * Posts a "was this expected?" notification for a newly detected draft, with inline
 * Approve/Discard actions so the user never has to open the app for the common case —
 * mirrors the browser extension's toast-on-detect, but actionable instead of passive.
 */
object DraftNotifier {

    fun notify(context: Context, draft: DraftSubscription) {
        ensureChannel(context)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notificationId = draft.id.hashCode()

        val approveIntent = actionIntent(context, ACTION_APPROVE_DRAFT, draft.id, notificationId, requestCode = notificationId * 2)
        val discardIntent = actionIntent(context, ACTION_DISCARD_DRAFT, draft.id, notificationId, requestCode = notificationId * 2 + 1)

        val amountText = draft.amount?.let { Format.currency(it) } ?: "unknown amount"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Detected: ${draft.merchantName}")
            .setContentText("$amountText / ${draft.billingCycle} — was this expected?")
            .setAutoCancel(true)
            .addAction(0, "Approve", approveIntent)
            .addAction(0, "Discard", discardIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun actionIntent(context: Context, action: String, draftId: String, notificationId: Int, requestCode: Int): PendingIntent {
        val intent = Intent(context, DraftActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_DRAFT_ID, draftId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Detected charges", NotificationManager.IMPORTANCE_DEFAULT)
            manager?.createNotificationChannel(channel)
        }
    }
}
