package cl.comunidad.conectada.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Ink = Color(0xFF102A38)
val Paper = Color(0xFFF2F6F6)
val Mint = Color(0xFFC5F2E4)
val Muted = Color(0xFF496168)
val AlertRed = Color(0xFFB12D3D)

@Composable
fun CommunityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(primary = Ink, onPrimary = Color.White, primaryContainer = Mint,
            secondary = Color(0xFF176B65), background = Paper, surface = Paper,
            surfaceContainer = Color(0xFFE2EFED), surfaceContainerLow = Color.White,
            onSurface = Ink, onSurfaceVariant = Muted, error = AlertRed),
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content
    )
}
