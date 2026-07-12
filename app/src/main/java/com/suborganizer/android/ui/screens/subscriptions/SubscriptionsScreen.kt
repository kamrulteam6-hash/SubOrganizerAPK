package com.suborganizer.android.ui.screens.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.CategoryBadge
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.MerchantAvatar
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.util.Format

@Composable
fun SubscriptionsScreen(mainViewModel: MainViewModel) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Subscription?>(null) }
    val currency = state.profile?.settings?.currency ?: "USD"

    val filtered = state.subscriptions.filter { it.merchantName.contains(query, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "My Subscriptions",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(20.dp),
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        )

        if (filtered.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("No subscriptions found.", color = Muted)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filtered, key = { it.id ?: it.merchantName }) { sub ->
                    SubscriptionRow(sub, currency, onClick = { selected = sub })
                }
            }
        }
    }

    selected?.let { sub ->
        SubscriptionDetailDialog(
            sub = sub,
            currency = currency,
            onDismiss = { selected = null },
            onMarkCanceled = {
                mainViewModel.updateSubscriptionStatus(sub.id ?: return@SubscriptionDetailDialog, "canceled")
                selected = null
            },
            onDelete = {
                mainViewModel.deleteSubscription(sub.id ?: return@SubscriptionDetailDialog)
                selected = null
            },
        )
    }
}

@Composable
private fun SubscriptionRow(sub: Subscription, currency: String, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MerchantAvatar(sub.merchantName, sub.merchantDomain, sub.category, size = 36.dp)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(sub.merchantName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                CategoryBadge(sub.category)
            }
            Text(Format.currency(sub.amount, currency), color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SubscriptionDetailDialog(
    sub: Subscription,
    currency: String,
    onDismiss: () -> Unit,
    onMarkCanceled: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(sub.merchantName) },
        text = {
            Column {
                Text("${Format.currency(sub.amount, currency)} · ${sub.billingCycle}")
                Text("Next: ${sub.nextRenewalDate ?: "—"}", color = Muted)
                Text("Status: ${sub.status}", color = Muted)
            }
        },
        confirmButton = {
            TextButton(onClick = onMarkCanceled) { Text("Mark canceled") }
        },
        dismissButton = {
            TextButton(onClick = onDelete) { Text("Delete", color = com.suborganizer.android.ui.theme.Rose) }
        },
    )
}
