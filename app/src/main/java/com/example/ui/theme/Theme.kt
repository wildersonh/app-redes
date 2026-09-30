package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DarkBrandBluePrimary,
    onPrimary = DarkBrandBlueOnPrimary,
    primaryContainer = DarkBrandBlueContainer,
    onPrimaryContainer = DarkBrandBlueOnContainer,
    secondary = DarkBrandTealSecondary,
    onSecondary = DarkBrandTealOnSecondary,
    secondaryContainer = DarkBrandTealContainer,
    onSecondaryContainer = DarkBrandTealOnContainer,
    tertiary = DarkBrandIndigoTertiary,
    onTertiary = DarkBrandIndigoOnTertiary,
    tertiaryContainer = DarkBrandIndigoContainer,
    onTertiaryContainer = DarkBrandIndigoOnContainer,
    background = NeutralDarkBackground,
    surface = NeutralDarkSurface,
    surfaceVariant = NeutralDarkSurfaceVariant,
    onBackground = NeutralDarkOnSurface,
    onSurface = NeutralDarkOnSurface,
    outline = NeutralDarkOutline,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandBluePrimary,
    onPrimary = BrandBlueOnPrimary,
    primaryContainer = BrandBlueContainer,
    onPrimaryContainer = BrandBlueOnContainer,
    secondary = BrandTealSecondary,
    onSecondary = BrandTealOnSecondary,
    secondaryContainer = BrandTealContainer,
    onSecondaryContainer = BrandTealOnContainer,
    tertiary = BrandIndigoTertiary,
    onTertiary = BrandIndigoOnTertiary,
    tertiaryContainer = BrandIndigoContainer,
    onTertiaryContainer = BrandIndigoOnContainer,
    background = NeutralLightBackground,
    surface = NeutralLightSurface,
    surfaceVariant = NeutralLightSurfaceVariant,
    onBackground = NeutralLightOnSurface,
    onSurface = NeutralLightOnSurface,
    outline = NeutralLightOutline,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our handcrafted network palette for consistent visual identity
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
