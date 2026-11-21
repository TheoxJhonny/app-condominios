package cl.comunidad.conectada.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Ink = Color(0xFF193C3B)
val Paper = Color(0xFFF5F5F0)
val Mint = Color(0xFFDDECE2)
val Muted = Color(0xFF627370)
val AlertRed = Color(0xFFAD413B)

@Composable
fun CommunityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Ink, onPrimary = Color.White, primaryContainer = Mint,
            secondary = Color(0xFFAC6B43), background = Paper, surface = Paper,
            surfaceContainer = Color(0xFFEDEFE8), surfaceContainerLow = Color.White,
            onSurface = Ink, onSurfaceVariant = Muted, error = AlertRed),
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content
    )
}
