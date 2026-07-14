package com.suborganizer.android.ui.screens.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.GradientButton
import com.suborganizer.android.ui.components.UpgradeToProButton
import com.suborganizer.android.ui.theme.Amber
import com.suborganizer.android.ui.theme.Rose
import com.suborganizer.android.util.FREE_PLAN_LIMIT
import com.suborganizer.android.util.Format

private val CATEGORIES = listOf("streaming", "software", "music", "news", "fitness", "gaming", "cloud", "other")
private val CYCLES = listOf("monthly", "yearly", "weekly", "quarterly")

@Composable
fun AddSubscriptionScreen(mainViewModel: MainViewModel, onSaved: () -> Unit, onOpenPricing: () -> Unit = {}) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val isFreePlan = state.profile?.plan.isNullOrBlank() || state.profile?.plan == "free"
    val atLimit = isFreePlan && state.subscriptions.size >= FREE_PLAN_LIMIT

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(CATEGORIES.last()) }
    var cycle by remember { mutableStateOf(CYCLES.first()) }
    var isTrial by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text("Add Subscription", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(Modifier.height(20.dp))

        if (atLimit) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Free plan limit reached", color = Amber, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "You're tracking $FREE_PLAN_LIMIT of $FREE_PLAN_LIMIT subscriptions on the Free plan. Upgrade to Pro to track unlimited subscriptions.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(14.dp))
                    UpgradeToProButton(modifier = Modifier.fillMaxWidth(), onClick = onOpenPricing)
                }
            }
            return@Column
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Merchant name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        Dropdown(label = "Category", options = CATEGORIES, selected = category, onSelected = { category = it })
        Spacer(Modifier.height(12.dp))
        Dropdown(label = "Billing cycle", options = CYCLES, selected = cycle, onSelected = { cycle = it })
        Spacer(Modifier.height(12.dp))

        Row {
            Text("Currently a free trial?", color = Color.White, modifier = Modifier.padding(top = 12.dp).weight(1f))
            androidx.compose.material3.Switch(checked = isTrial, onCheckedChange = { isTrial = it })
        }

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = Rose)
        }

        Spacer(Modifier.height(20.dp))
        GradientButton(
            text = "Add Subscription",
            loading = saving,
            enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
            onClick = {
                val amt = amount.toDoubleOrNull() ?: return@GradientButton
                saving = true
                error = null
                val nextDate = Format.todayPlus(if (isTrial) 7 else 30)
                mainViewModel.addSubscription(
                    Subscription(
                        merchantName = name,
                        merchantDomain = null,
                        category = category,
                        amount = amt,
                        billingCycle = cycle,
                        status = if (isTrial) "trial" else "active",
                        trialEndDate = if (isTrial) nextDate else null,
                        nextRenewalDate = nextDate,
                        detectionSource = "manual",
                    ),
                ) { result ->
                    saving = false
                    result.onSuccess { onSaved() }
                    result.onFailure { error = it.message ?: "Failed to save." }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(label: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
        )
        // Unqualified on purpose: ExposedDropdownMenu is a member of ExposedDropdownMenuBoxScope,
        // not a top-level material3 function — fully qualifying it fails to resolve.
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
