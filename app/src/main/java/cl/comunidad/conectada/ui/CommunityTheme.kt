package cl.comunidad.conectada.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF191A17)
val Paper = Color(0xFFF5F2E9)
val Cobalt = Color(0xFF2239D7)
val CobaltWash = Color(0xFFE5E9FC)
val Muted = Color(0xFF56584F)
val SignalOrange = Color(0xFFBE4200)
val Rule = Color(0xFFCDC9BD)
val AlertRed = Color(0xFFB12D3D)

internal val Square = RoundedCornerShape(0.dp)
private val EditorialTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-1.2).sp),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp),
        headlineMedium = headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp
        ),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        labelSmall = labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
    )
}

@Composable
fun CommunityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Cobalt, onPrimary = Color.White, primaryContainer = CobaltWash,
            onPrimaryContainer = Ink, secondary = Cobalt, secondaryContainer = CobaltWash,
            onSecondaryContainer = Cobalt, background = Paper, onBackground = Ink,
            surface = Paper, surfaceContainer = Color(0xFFEBE7DC),
            surfaceContainerLow = Color.White, onSurface = Ink, onSurfaceVariant = Muted,
            outline = Muted, outlineVariant = Rule, error = AlertRed
        ),
        shapes = Shapes(
            extraSmall = Square,
            small = Square,
            medium = Square,
            large = Square,
            extraLarge = Square
        ),
        typography = EditorialTypography,
        content = content
    )
}
