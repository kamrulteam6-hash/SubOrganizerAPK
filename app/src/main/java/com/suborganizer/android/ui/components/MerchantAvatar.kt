package com.suborganizer.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.suborganizer.android.ui.theme.categoryTheme

private fun domainFor(merchantName: String, merchantDomain: String?): String {
    if (!merchantDomain.isNullOrBlank()) return merchantDomain
    val raw = merchantName.lowercase().trim()
    val known = mapOf(
        "claude" to "claude.ai", "chatgpt" to "chatgpt.com", "openai" to "chatgpt.com",
        "netflix" to "netflix.com", "spotify" to "spotify.com", "canva" to "canva.com",
        "notion" to "notion.so", "youtube" to "youtube.com", "disney" to "disneyplus.com",
        "hulu" to "hulu.com", "amazon" to "amazon.com", "apple" to "apple.com", "icloud" to "apple.com",
    )
    known.entries.firstOrNull { raw.contains(it.key) }?.let { return it.value }
    return raw.replace(Regex("[^a-z0-9]"), "") + ".com"
}

@Composable
fun MerchantAvatar(
    merchantName: String,
    merchantDomain: String?,
    category: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val domain = domainFor(merchantName, merchantDomain)
    val theme = categoryTheme(category)
    val fallbackBrush = Brush.linearGradient(listOf(theme.gradientStart, theme.gradientEnd))

    SubcomposeAsyncImage(
        model = "https://logo.clearbit.com/$domain",
        contentDescription = merchantName,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3.5f)),
        error = {
            SubcomposeAsyncImage(
                model = "https://www.google.com/s2/favicons?sz=128&domain=$domain",
                contentDescription = merchantName,
                modifier = Modifier.size(size),
                error = { InitialFallback(merchantName, fallbackBrush, size) },
                loading = { InitialFallback(merchantName, fallbackBrush, size) },
            )
        },
        loading = { InitialFallback(merchantName, fallbackBrush, size) },
    )
}

@Composable
private fun InitialFallback(merchantName: String, brush: Brush, size: Dp) {
    Box(
        modifier = Modifier.size(size).background(brush, RoundedCornerShape(size / 3.5f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = merchantName.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
