package com.suborganizer.android.ui.theme

import androidx.compose.ui.graphics.Color

// Matches the web app's dark premium palette (src/app/globals.css).
val BackgroundDark = Color(0xFF06070C)
val SurfaceDark = Color(0xFF0D0F1A)
val SurfaceRaised = Color(0x0AFFFFFF) // rgba(255,255,255,.04)
val SurfaceRaised2 = Color(0x0FFFFFFF) // rgba(255,255,255,.06)
val BorderDark = Color(0x14FFFFFF) // rgba(255,255,255,.08)

val Foreground = Color(0xFFF1F2F7)
val Muted = Color(0xFF8B8FA8)
val Muted2 = Color(0xFFC7C9D9)

val IndigoAccent = Color(0xFF6366F1)
val FuchsiaAccent = Color(0xFFD946EF)

val Emerald = Color(0xFF10B981)
val EmeraldSoft = Color(0xFF6EE7B7)
val Amber = Color(0xFFF59E0B)
val AmberSoft = Color(0xFFFCD34D)
val Rose = Color(0xFFF43F5E)
val RoseSoft = Color(0xFFFCA5A5)

// Category accent colors — mirrors src/lib/format.ts CAT_THEME on the web.
data class CategoryTheme(val gradientStart: Color, val gradientEnd: Color, val soft: Color, val emoji: String)

val CategoryThemes: Map<String, CategoryTheme> = mapOf(
    "streaming" to CategoryTheme(Color(0xFFF43F5E), Color(0xFFEC4899), Color(0x1AF43F5E), "🎬"),
    "software" to CategoryTheme(Color(0xFF6366F1), Color(0xFF3B82F6), Color(0x1A6366F1), "💻"),
    "music" to CategoryTheme(Color(0xFF10B981), Color(0xFF14B8A6), Color(0x1A10B981), "🎵"),
    "news" to CategoryTheme(Color(0xFFF59E0B), Color(0xFFEAB308), Color(0x1AF59E0B), "📰"),
    "fitness" to CategoryTheme(Color(0xFFF97316), Color(0xFFEF4444), Color(0x1AF97316), "💪"),
    "gaming" to CategoryTheme(Color(0xFF8B5CF6), Color(0xFFD946EF), Color(0x1A8B5CF6), "🎮"),
    "cloud" to CategoryTheme(Color(0xFF0EA5E9), Color(0xFF06B6D4), Color(0x1A0EA5E9), "☁️"),
    "other" to CategoryTheme(Color(0xFF64748B), Color(0xFF475569), Color(0x1A64748B), "🔖"),
)

fun categoryTheme(category: String?): CategoryTheme = CategoryThemes[category] ?: CategoryThemes.getValue("other")
