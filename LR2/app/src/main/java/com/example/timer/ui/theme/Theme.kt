package com.example.timer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.timer.data.local.preferences.FontSize

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Cyan80
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Cyan40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun TimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    // Create scaled typography based on font size preference
    val scaledTypography = androidx.compose.material3.Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (57.sp.value * fontSizeScale).sp,
            lineHeight = (64.sp.value * fontSizeScale).sp,
            letterSpacing = (-0.25.sp.value * fontSizeScale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (45.sp.value * fontSizeScale).sp,
            lineHeight = (52.sp.value * fontSizeScale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (36.sp.value * fontSizeScale).sp,
            lineHeight = (44.sp.value * fontSizeScale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (32.sp.value * fontSizeScale).sp,
            lineHeight = (40.sp.value * fontSizeScale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (28.sp.value * fontSizeScale).sp,
            lineHeight = (36.sp.value * fontSizeScale).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (24.sp.value * fontSizeScale).sp,
            lineHeight = (32.sp.value * fontSizeScale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (22.sp.value * fontSizeScale).sp,
            lineHeight = (28.sp.value * fontSizeScale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (16.sp.value * fontSizeScale).sp,
            lineHeight = (24.sp.value * fontSizeScale).sp,
            letterSpacing = (0.15.sp.value * fontSizeScale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14.sp.value * fontSizeScale).sp,
            lineHeight = (20.sp.value * fontSizeScale).sp,
            letterSpacing = (0.1.sp.value * fontSizeScale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16.sp.value * fontSizeScale).sp,
            lineHeight = (24.sp.value * fontSizeScale).sp,
            letterSpacing = (0.5.sp.value * fontSizeScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14.sp.value * fontSizeScale).sp,
            lineHeight = (20.sp.value * fontSizeScale).sp,
            letterSpacing = (0.25.sp.value * fontSizeScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (12.sp.value * fontSizeScale).sp,
            lineHeight = (16.sp.value * fontSizeScale).sp,
            letterSpacing = (0.4.sp.value * fontSizeScale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14.sp.value * fontSizeScale).sp,
            lineHeight = (20.sp.value * fontSizeScale).sp,
            letterSpacing = (0.1.sp.value * fontSizeScale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (12.sp.value * fontSizeScale).sp,
            lineHeight = (16.sp.value * fontSizeScale).sp,
            letterSpacing = (0.5.sp.value * fontSizeScale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11.sp.value * fontSizeScale).sp,
            lineHeight = (16.sp.value * fontSizeScale).sp,
            letterSpacing = (0.5.sp.value * fontSizeScale).sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}

/**
 * Convenience overload that accepts FontSize enum
 */
@Composable
fun TimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    fontSize: FontSize = FontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    TimerTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        fontSizeScale = fontSize.scale,
        content = content
    )
}