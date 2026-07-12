package com.suborganizer.android.ui.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suborganizer.android.data.model.DraftSubscription
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.CategoryBadge
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.MerchantAvatar
import com.suborganizer.android.ui.theme.Emerald
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.ui.theme.Rose
import com.suborganizer.android.util.Format

@Composable
fun ReviewScreen(mainViewModel: MainViewModel) {
    val drafts by mainViewModel.drafts.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Review (${drafts.size})",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(20.dp),
        )
        Text(
            "Detected from notifications and messages on this device. Nothing is saved until you approve it.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        if (drafts.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Nothing to review right now.", color = Muted)
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(drafts, key = { it.id }) { draft ->
                DraftCard(
                    draft = draft,
                    onApprove = { mainViewModel.approveDraft(draft) },
                    onDiscard = { mainViewModel.discardDraft(draft.id) },
                )
            }
        }
    }
}

@Composable
private fun DraftCard(draft: DraftSubscription, onApprove: () -> Unit, onDiscard: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MerchantAvatar(draft.merchantName, draft.merchantDomain, draft.category, size = 36.dp)
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(draft.merchantName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${draft.amount?.let { Format.currency(it) } ?: "amount unknown"} · ${draft.billingCycle}" +
                            if (draft.status == "trial") " · trial" else "",
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text("${(draft.confidence * 100).toInt()}%", color = Muted, style = MaterialTheme.typography.labelMedium)
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                CategoryBadge(draft.category)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onDiscard) {
                    Icon(Icons.Default.Close, contentDescription = "Discard", tint = Rose)
                }
                IconButton(onClick = onApprove) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", tint = Emerald)
                }
            }
        }
    }
}
