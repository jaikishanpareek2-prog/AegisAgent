package com.aegis.agent.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

val AegisCyan = Color(0xFF00E5FF)
val AegisPurple = Color(0xFF7C4DFF)
val AegisBg = Color(0xFF0A0E17)
val AegisSurface = Color(0xFF121820)
val AegisCard = Color(0xFF1A2230)
val AegisOnSurface = Color(0xFFE8EEF7)
val AegisMuted = Color(0xFF8B9BB4)
val AegisSuccess = Color(0xFF00C853)
val AegisWarning = Color(0xFFFFAB00)
val AegisError = Color(0xFFFF5252)

private val DarkColors = darkColorScheme(
    primary = AegisCyan,
    onPrimary = Color.Black,
    secondary = AegisPurple,
    onSecondary = Color.White,
    background = AegisBg,
    onBackground = AegisOnSurface,
    surface = AegisSurface,
    onSurface = AegisOnSurface,
    surfaceVariant = AegisCard,
    onSurfaceVariant = AegisMuted,
    error = AegisError,
    outline = Color(0xFF2A3548)
)

private val AegisTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

@Composable
fun AegisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AegisTypography,
        content = content
    )
}


@androidx.compose.runtime.Composable
fun GlassCard(content:@androidx.compose.runtime.Composable ()->Unit){
 androidx.compose.material3.Card(modifier=androidx.compose.ui.Modifier.fillMaxWidth(),colors=androidx.compose.material3.CardDefaults.cardColors(containerColor=AegisCard)){androidx.compose.foundation.layout.Column(modifier=androidx.compose.ui.Modifier.padding(14.dp),content=content)}
}
