package com.suborganizer.android.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.UpgradeToProButton
import com.suborganizer.android.ui.theme.Emerald
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.ui.theme.Rose

@Composable
fun SettingsScreen(mainViewModel: MainViewModel, onSignedOut: () -> Unit, onOpenPricing: () -> Unit = {}) {
    val context = LocalContext.current
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val isFreePlan = state.profile?.plan.isNullOrBlank() || state.profile?.plan == "free"

    var notificationAccessGranted by remember {
        mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName))
    }
    var smsGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED)
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        smsGranted = result.values.all { it }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(Modifier.height(20.dp))

        Text("ACCOUNT", style = MaterialTheme.typography.labelMedium, color = Muted)
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(mainViewModel.currentUserEmail ?: "—", color = Color.White)
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { mainViewModel.signOut(onSignedOut) }) {
                    Text("Sign Out", color = Rose)
                }
            }
        }

        if (isFreePlan) {
            Spacer(Modifier.height(24.dp))
            Text("PLAN", style = MaterialTheme.typography.labelMedium, color = Muted)
            Spacer(Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "You're on the Free plan — tracking ${state.subscriptions.size} of 3 subscriptions.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Upgrade on the website for unlimited subscriptions, price-hike alerts, and family sharing.",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(12.dp))
                    UpgradeToProButton(modifier = Modifier.fillMaxWidth(), onClick = onOpenPricing)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("SMART COLLECTION", style = MaterialTheme.typography.labelMedium, color = Muted)
        Spacer(Modifier.height(4.dp))
        Text(
            "SubOrganizer can watch for subscription charges in notifications and messages, entirely on this device. Nothing is saved until you approve it in Review.",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))

        PermissionRow(
            title = "Notification access",
            description = "Reads notification text from banking, payment, and email apps to detect charges.",
            granted = notificationAccessGranted,
            onRequest = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
        )
        Spacer(Modifier.height(10.dp))
        PermissionRow(
            title = "SMS access",
            description = "Reads incoming SMS for bank/payment \"you were charged\" messages. Optional distribution builds only — see README.",
            granted = smsGranted,
            onRequest = {
                smsPermissionLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
            },
        )
    }
}

@Composable
private fun PermissionRow(title: String, description: String, granted: Boolean, onRequest: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    if (granted) "Granted" else "Not granted",
                    color = if (granted) Emerald else Muted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(description, color = Muted, style = MaterialTheme.typography.bodyMedium)
            if (!granted) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRequest) {
                    Text("Enable")
                }
            }
        }
    }
}
