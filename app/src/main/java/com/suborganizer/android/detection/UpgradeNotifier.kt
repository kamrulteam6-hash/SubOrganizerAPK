package com.suborganizer.android.detection

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.suborganizer.android.R
import com.suborganizer.android.util.FREE_PLAN_LIMIT
import com.suborganizer.android.util.PRICING_URL

private const val CHANNEL_ID = "free_limit_reached"
private const val NOTIFICATION_ID = 90210

/**
 * Fires once, right when a free-plan user's subscription count first hits the cap —
 * mirrors DraftNotifier's structure/style. Tapping it opens the website's pricing
 * section directly; there's no in-app checkout to route to.
 */
object UpgradeNotifier {

    fun notify(context: Context) {
        ensureChannel(context)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val contentIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(Intent.ACTION_VIEW, Uri.parse(PRICING_URL)),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("You've hit your free plan limit")
            .setContentText("Tracking $FREE_PLAN_LIMIT of $FREE_PLAN_LIMIT subscriptions — upgrade to Pro for unlimited tracking.")
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(CHANNEL_ID, "Plan limit reached", NotificationManager.IMPORTANCE_DEFAULT)
            manager?.createNotificationChannel(channel)
        }
    }
}
