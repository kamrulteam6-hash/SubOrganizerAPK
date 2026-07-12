package com.suborganizer.android.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import com.suborganizer.android.data.model.Subscription
import com.suborganizer.android.ui.MainViewModel
import com.suborganizer.android.ui.components.GlassCard
import com.suborganizer.android.ui.components.MerchantAvatar
import com.suborganizer.android.ui.theme.BorderDark
import com.suborganizer.android.ui.theme.IndigoAccent
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.ui.theme.categoryTheme
import com.suborganizer.android.util.Format
import java.util.Calendar

@Composable
fun CalendarScreen(mainViewModel: MainViewModel) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val currency = state.profile?.settings?.currency ?: "USD"
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDay by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.US)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply { set(year, month, 1) }.get(Calendar.DAY_OF_WEEK) - 1

    fun subsOnDay(day: Int): List<Subscription> = state.subscriptions.filter { sub ->
        val date = sub.nextRenewalDate ?: return@filter false
        if (sub.status == "canceled") return@filter false
        val parts = date.split("-")
        parts.size == 3 && parts[0].toIntOrNull() == year && (parts[1].toIntOrNull()?.minus(1)) == month && parts[2].toIntOrNull() == day
    }

    val selectedDaySubs = subsOnDay(selectedDay)

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Payment Calendar", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                selectedDay = 1
            }) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = Muted) }
            Text("$monthName $year", color = Color.White, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = {
                calendar = (calendar.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                selectedDay = 1
            }) { Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = Muted) }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                Text(it, color = Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(firstDayOfWeek) { Box(Modifier.aspectRatio(1f)) }
            items(daysInMonth) { index ->
                val day = index + 1
                val daySubs = subsOnDay(day)
                DayCell(
                    day = day,
                    subs = daySubs,
                    isSelected = day == selectedDay,
                    onClick = { selectedDay = day },
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                if (selectedDaySubs.isEmpty()) "Nothing renews on $monthName $selectedDay" else "Renewing on $monthName $selectedDay",
                color = Muted,
                style = MaterialTheme.typography.labelMedium,
            )
            selectedDaySubs.forEach { sub ->
                GlassCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MerchantAvatar(sub.merchantName, sub.merchantDomain, sub.category, size = 32.dp)
                        Text(
                            sub.merchantName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 12.dp).weight(1f),
                        )
                        Text(Format.currency(sub.amount, currency), color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, subs: List<Subscription>, isSelected: Boolean, onClick: () -> Unit) {
    val hasSubs = subs.isNotEmpty()
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) IndigoAccent.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (hasSubs) {
            val theme = categoryTheme(subs.first().category)
            val domain = subs.first().merchantDomain
                ?: (subs.first().merchantName.lowercase().replace(Regex("[^a-z0-9]"), "") + ".com")
            SubcomposeAsyncImage(
                model = "https://logo.clearbit.com/$domain",
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                error = {
                    Box(
                        Modifier.fillMaxSize().background(Brush.linearGradient(listOf(theme.gradientStart, theme.gradientEnd))),
                    )
                },
                loading = {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(theme.gradientStart, theme.gradientEnd))))
                },
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent), startY = 0f, endY = 60f),
                ),
            )
        }
        Text(
            text = day.toString(),
            color = if (hasSubs) Color.White else Muted,
            style = MaterialTheme.typography.labelMedium,
        )
        if (subs.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 3.dp),
            ) {
                Text(subs.size.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
