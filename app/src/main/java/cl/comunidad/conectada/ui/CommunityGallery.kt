package cl.comunidad.conectada.ui

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import java.io.IOException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class GalleryPhoto(val label: String, val source: String)

// Public Unsplash sample photographs. They represent fictional amenities in the app.
private val GalleryPhotos = listOf(
    GalleryPhoto("Fachada", "1780686616260-2944a28024e1"), // Marcus Lenk
    GalleryPhoto("Piscina", "1771317778033-3473a576c5e9"), // Polina Kuzovkova
    GalleryPhoto("Áreas verdes", "1744037215345-313aaa161596"), // tommao wang
    GalleryPhoto("Terraza", "1777052854737-7893f50de539") // Kristaps Ungurs
)

private val Monochrome = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
private val PhotoCache = LruCache<String, ImageBitmap>(GalleryPhotos.size)

@Composable
internal fun CommunityGallery(modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var attempt by remember { mutableIntStateOf(0) }
    val photos = remember(attempt) { mutableStateMapOf<String, ImageBitmap>() }
    var completed by remember(attempt) { mutableIntStateOf(0) }
    LaunchedEffect(attempt) {
        GalleryPhotos.forEach { photo ->
            launch {
                val bitmap = withContext(Dispatchers.IO) { loadPhoto(photo) }
                if (bitmap != null) photos[photo.source] = bitmap
                completed++
            }
        }
    }
    val available = GalleryPhotos.mapNotNull { photo ->
        photos[photo.source]?.let { photo to it }
    }
    val loading = completed < GalleryPhotos.size
    val pager = rememberPagerState { available.size }
    var playing by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(lifecycle, playing, available.size) {
        if (playing && available.size > 1) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(6000)
                    if (!pager.isScrollInProgress) {
                        pager.animateScrollToPage((pager.currentPage + 1) % available.size)
                    }
                }
            }
        }
    }

    Column(modifier) {
        Box(Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Ink)) {
            if (available.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (loading) "Cargando galería" else "Galería no disponible",
                        color = Paper,
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (!loading) {
                        TextButton(onClick = { attempt++ }, shape = Square) {
                            Text("Reintentar", color = Paper)
                        }
                    }
                }
            } else {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    key = { index -> available[index].first.source }
                ) { index ->
                    val (photo, bitmap) = available[index]
                    Box(Modifier.fillMaxSize()) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Imagen referencial",
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = Monochrome,
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            photo.label,
                            Modifier
                                .align(Alignment.BottomStart)
                                .background(Ink)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Paper,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            if (loading) {
                LinearProgressIndicator(
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().height(3.dp),
                    color = CobaltWash,
                    trackColor = Ink
                )
            }
        }
        if (available.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${pager.currentPage + 1} / ${available.size}",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted
                )
                if (available.size > 1) {
                    TextButton(onClick = { playing = !playing }, shape = Square) {
                        Text(if (playing) "Pausar" else "Reanudar")
                    }
                }
            }
        }
    }
}

private fun loadPhoto(photo: GalleryPhoto): ImageBitmap? {
    PhotoCache.get(photo.source)?.let { return it }
    return try {
        val connection =
            URL("https://images.unsplash.com/photo-${photo.source}?fit=crop&fm=jpg&w=640&h=800&q=78")
                .openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.getInputStream().use { stream ->
            BitmapFactory.decodeStream(stream)?.asImageBitmap()?.also {
                PhotoCache.put(photo.source, it)
            }
        }
    } catch (_: IOException) {
        null
    }
}
