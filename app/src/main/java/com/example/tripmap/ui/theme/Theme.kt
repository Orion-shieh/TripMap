package com.example.tripmap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val MyShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(12.dp) // 这就是 DropdownMenu 的圆角
)
private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = SkyBlue80,
    surface = Color(0xFF303030),
    error = Color(0xFFD23333),
    background = Color(0xFF353535),

    onPrimaryContainer = Color.LightGray,
    onSecondaryContainer = Color(0xFF2B2B2B),
    onTertiaryContainer = Color(0xFFB8B8B8),
    onSurfaceVariant = Color.Black,

    onSurface = Color(0xFFDCDCDC),
    onSecondary = Color(0xFF808080),
    onTertiary = Color(0xD9B5B5B5),

)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = SkyBlue40,
    surface = LightGray,
    background = Color.White,
    error = Color.Red,

    surfaceVariant = Color.White,

    onPrimaryContainer = Color.DarkGray,
    onSecondaryContainer = Color(0xFFFFFFFF),
    onTertiaryContainer = Color(0xFF636363),
    onSurfaceVariant = Color.Gray,

    onSurface = Color(0xFF2A2A2A),
    onSecondary = Color.LightGray,
    onTertiary = Color(0xF0F4F4F4),

    // Other default colors to override
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,

)

@Composable
fun TripMapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        shapes = MyShapes,
    )
}