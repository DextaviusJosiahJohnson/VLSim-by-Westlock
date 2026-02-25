package com.dextaviousjosiahjohnson.vlsim.ui.theme

import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dextaviousjosiahjohnson.vlsim.R

val TrebuchetFont = FontFamily(
    Font(R.font.trebuchet, FontWeight.Normal),
    Font(R.font.trebuchet, FontWeight.Bold)
)

val TahomaFont = FontFamily(
    Font(R.font.tahoma, FontWeight.Normal),
    Font(R.font.tahoma, FontWeight.Bold)
)

val AppTypography = Typography(
    headlineMedium = Typography().headlineMedium.copy(fontFamily = TrebuchetFont, fontWeight = FontWeight.Bold),
    titleLarge = Typography().titleLarge.copy(fontFamily = TrebuchetFont, fontWeight = FontWeight.Bold),
    titleMedium = Typography().titleMedium.copy(fontFamily = TrebuchetFont, fontWeight = FontWeight.Bold),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = TahomaFont),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = TahomaFont),
    labelLarge = Typography().labelLarge.copy(fontFamily = TrebuchetFont, fontWeight = FontWeight.Bold)
)

// Zero rounding for the "hard" look
val AppShapes = Shapes(
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp)
)

data class ThemeState(
    val primaryColor: Color = Color(0xFFFF9800), // Hard Orange
    val backgroundColor: Color = Color(0xFF121212), // Deep Black
    val surfaceColor: Color = Color(0xFF1E1E1E), // Dark Gray for cards
    val backgroundUri: Uri? = null
)

@Composable
fun VLSimTheme(
    themeState: ThemeState,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = themeState.primaryColor,
        background = themeState.backgroundColor,
        surface = themeState.surfaceColor,
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}