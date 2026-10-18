package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TwinSpaceDarkColorScheme = darkColorScheme(
  primary = ElectricCyan,
  onPrimary = Color(0xFF041F24),
  primaryContainer = Color(0xFF0A323D),
  onPrimaryContainer = Color(0xFF9CF7FF),
  secondary = PrismViolet,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF281C4D),
  onSecondaryContainer = Color(0xFFDDD6FE),
  tertiary = NeonEmerald,
  onTertiary = Color(0xFF03281C),
  tertiaryContainer = Color(0xFF0A3D2C),
  onTertiaryContainer = Color(0xFFA7F3D0),
  background = ObsidianBg,
  onBackground = TextPrimaryDark,
  surface = ObsidianSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = ObsidianSurfaceElevated,
  onSurfaceVariant = TextSecondaryDark,
  surfaceContainerHigh = ObsidianSurfaceBright,
  outline = OutlineDark,
  error = CrimsonAlert,
  onError = Color.White,
)

private val TwinSpaceLightColorScheme = lightColorScheme(
  primary = DeepCyanPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFCCFBF1),
  onPrimaryContainer = Color(0xFF042F2E),
  secondary = DeepVioletSecondary,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFEDE9FE),
  onSecondaryContainer = Color(0xFF2E1065),
  tertiary = Color(0xFF059669),
  background = DaylightBg,
  onBackground = Color(0xFF0F172A),
  surface = DaylightSurface,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = DaylightSurfaceVariant,
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Intentional cyber-obsidian dark theme by default
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) TwinSpaceDarkColorScheme else TwinSpaceLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content,
  )
}
