package cl.comunidad.conectada.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role as AccessibilityRole
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.comunidad.conectada.data.*
import cl.comunidad.conectada.domain.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityApp(state: CommunityState, model: CommunityViewModel, openAlerts: Boolean, onAlertsOpened: () -> Unit, onNotificationPermission: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf(false) }
    var composeAnnouncement by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Announcement?>(null) }
    LaunchedEffect(openAlerts, state.resident) { if (openAlerts && state.member) { tab = 3; onAlertsOpened() } }
    val labels = listOf("Inicio", "Chat", "Avisos", "Alertas", "Perfil")
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.Email, Icons.Outlined.DateRange, Icons.Outlined.Warning, Icons.Outlined.Person)
    Scaffold(
        containerColor = Paper,
        topBar = {
            if (state.resident != null) {
                Column {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                BrandMark()
                                Text("NeighborLoop", style = MaterialTheme.typography.titleLarge)
                            }
                        },
                        actions = {
                            if (state.demo) Text("DEMO", Modifier.padding(horizontal = 20.dp), color = Cobalt, style = MaterialTheme.typography.labelMedium)
                        }
                    )
                    HorizontalDivider(color = Ink)
                }
            }
        },
        bottomBar = {
            if (state.member) {
                Surface(color = Paper) {
                    Column(Modifier.navigationBarsPadding()) {
                        HorizontalDivider(color = Ink)
                        Row(Modifier.fillMaxWidth().selectableGroup()) {
                            labels.forEachIndexed { index, title ->
                                val selected = tab == index
                                val color = if (selected) Cobalt else Ink
                                Column(
                                    Modifier.weight(1f)
                                        .selectable(selected, role = AccessibilityRole.Tab, onClick = { tab = index })
                                        .heightIn(min = 60.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(Modifier.fillMaxWidth().height(3.dp).background(if (selected) Cobalt else Paper))
                                    Icon(icons[index], null, Modifier.padding(top = 6.dp).size(22.dp), tint = color)
                                    Text(title, Modifier.padding(top = 3.dp, bottom = 6.dp), color = color, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.notice?.let { notice -> Surface(color = CobaltWash, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(notice, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    IconButton(onClick = model::dismissNotice) { Icon(Icons.Default.Close, "Cerrar mensaje") }
                }
            } }
            when {
                state.resident == null -> AccessScreen(state, model)
                !state.member -> MembershipScreen(state, model)
                else -> {
                    if (state.demo) Text("Demostración · Datos de ejemplo, sin envío a otros dispositivos", Modifier.padding(horizontal = 20.dp, vertical = 8.dp), style = MaterialTheme.typography.labelSmall, color = Muted)
                    when (tab) {
                        0 -> HomeScreen(state, onChat = { tab = 1 }, onAnnouncements = { tab = 2 }, onAlert = { confirm = true }, onDetail = { detail = it })
                        1 -> ChatScreen(state, model)
                        2 -> AnnouncementsScreen(state, onCreate = { composeAnnouncement = true }, onDetail = { detail = it })
                        3 -> AlertsScreen(state, model, onAlert = { confirm = true }, onNotificationPermission = onNotificationPermission)
                        4 -> ProfileScreen(state, model, onNotificationPermission)
                    }
                }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, icon = { Icon(Icons.Default.Warning, null) }, title = { Text("¿Necesitas ayuda?") },
        text = { Text(if (state.demo) "Estás en una demostración. No se enviará ninguna alerta real." else "Se compartirá tu nombre y vivienda con los vecinos conectados. Activa la alerta solo si necesitas asistencia.") },
        confirmButton = { TextButton(onClick = { confirm = false; model.activateAlert() }) { Text(if (state.demo) "Comprobar estado" else "Confirmar alerta") } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } })
    state.incoming?.let { alert -> AlertDialog(onDismissRequest = model::dismissIncoming,
        title = { Text("Un vecino necesita ayuda") }, text = { Text("${alert.name} · Vivienda #${alert.home}\n\nHa activado una alerta comunitaria.") },
        confirmButton = { TextButton(onClick = { model.dismissIncoming(); tab = 3 }) { Text("Ver alertas") } }) }
    detail?.let { announcement -> AlertDialog(onDismissRequest = { detail = null }, title = { Text(announcement.title) },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("ADMINISTRACIÓN · ${formatTime(announcement.createdAt)}", style = MaterialTheme.typography.labelSmall)
            Text(announcement.content)
        } }, confirmButton = { TextButton(onClick = { detail = null }) { Text("Cerrar") } }) }
    if (composeAnnouncement) AnnouncementEditor(state.busy, onDismiss = { composeAnnouncement = false }) { title, content -> model.publishAnnouncement(title, content) { composeAnnouncement = false } }
}

@Composable
internal fun BrandMark(modifier: Modifier = Modifier.size(24.dp)) {
    Icon(
        painter = androidx.compose.ui.res.painterResource(cl.comunidad.conectada.R.drawable.neighborloop_icon),
        contentDescription = null,
        modifier = modifier,
        tint = Color.Unspecified
    )
}

@Composable
private fun HomeScreen(state: CommunityState, onChat: () -> Unit, onAnnouncements: () -> Unit, onAlert: () -> Unit, onDetail: (Announcement) -> Unit) {
    val resident = checkNotNull(state.resident)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VIVIENDA #${resident.home}", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text("Tu condominio", style = MaterialTheme.typography.headlineLarge)
                    Box(Modifier.width(36.dp).height(4.dp).background(Cobalt))
                }
                Facade(Modifier.width(70.dp).height(120.dp))
            }
            HorizontalDivider(Modifier.padding(top = 16.dp), color = Ink)
        }
        item { Text("Avisos recientes", style = MaterialTheme.typography.headlineMedium) }
        item {
            Column {
                state.announcements.take(2).forEach { AnnouncementRow(it, onDetail, showPreview = false) }
                if (state.announcements.isEmpty()) {
                    EmptyState("Aún no hay avisos", "Aquí aparecerán las publicaciones de administración.")
                }
                TextButton(onClick = onAnnouncements) { Text("Ver todos los avisos") }
            }
        }
        item {
            Button(onClick = onAlert, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text("Solicitar ayuda", Modifier.weight(1f))
                Icon(Icons.Outlined.Warning, null)
            }
        }
        item {
            OutlinedButton(onClick = onChat, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("Abrir chat", Modifier.weight(1f))
                Icon(Icons.Outlined.Email, null)
            }
        }
    }
}

@Composable
private fun Facade(modifier: Modifier) {
    Canvas(modifier) {
        drawRect(Ink)
        val bay = size.width / 4
        val floor = size.height / 5
        repeat(5) { level ->
            val y = level * floor
            drawRect(Muted, Offset(0f, y + floor * 0.10f), Size(size.width, floor * 0.24f))
            repeat(3) { window ->
                val x = bay * (window + 0.6f)
                drawRect(Paper, Offset(x, y + floor * 0.42f), Size(bay * 0.61f, floor * 0.42f))
            }
        }
    }
}

@Composable
private fun ChatScreen(state: CommunityState, model: CommunityViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Chat general", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
            if (!state.demo) TextButton(onClick = model::retryFeeds) { Text("Actualizar conexión") }
        }
        HorizontalDivider(color = Rule)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), reverseLayout = true) {
            if (state.messages.isEmpty()) item { EmptyState("La conversación empieza contigo", "Saluda o comparte algo con tus vecinos.") }
            items(state.messages.reversed(), key = { it.id }) { message ->
                val own = message.userId == state.resident?.uid
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (own) Alignment.End else Alignment.Start) {
                    Surface(color = if (own) CobaltWash else Color.White, shape = MaterialTheme.shapes.medium, modifier = Modifier.widthIn(max = 320.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${message.name} · #${message.home}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(message.content)
                            Text(formatTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = Rule)
        Surface(color = Paper) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = text, onValueChange = { if (it.length <= 2000) text = it }, label = { Text("Escribe a tus vecinos") }, modifier = Modifier.weight(1f), maxLines = 4, enabled = !state.busy)
            IconButton(onClick = { model.sendMessage(text) { text = "" } }, enabled = text.isNotBlank() && !state.busy, modifier = Modifier.padding(bottom = 6.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar mensaje") }
        } }
    }
}

@Composable
private fun AnnouncementsScreen(state: CommunityState, onCreate: () -> Unit, onDetail: (Announcement) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Avisos de administración", style = MaterialTheme.typography.headlineMedium) }
        if (state.resident?.role == Role.ADMIN && !state.demo) item { Button(onClick = onCreate) { Text("Publicar un aviso") } }
        if (state.announcements.isEmpty()) item { EmptyState("Aún no hay avisos", "Cuando administración publique información, podrás consultarla aquí.") }
        items(state.announcements, key = { it.id }) { AnnouncementRow(it, onDetail) }
    }
}

@Composable
private fun AnnouncementRow(item: Announcement, onDetail: (Announcement) -> Unit, showPreview: Boolean = true) {
    Row(
        Modifier.fillMaxWidth().clickable(role = AccessibilityRole.Button) { onDetail(item) }
            .heightIn(min = 56.dp).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(6.dp).background(Cobalt))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            if (showPreview) {
                Text(formatTime(item.createdAt), style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(item.content, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Muted)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Cobalt)
    }
    HorizontalDivider(color = Rule)
}

@Composable
fun EmptyState(title: String, text: String) {
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Rule)
        Column(Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider(color = Rule)
    }
}

fun formatTime(timestamp: Long): String = if (timestamp <= 0) "Fecha pendiente" else SimpleDateFormat("dd MMM · HH:mm", Locale.forLanguageTag("es-CL")).format(Date(timestamp))

@Composable
private fun AnnouncementEditor(busy: Boolean, onDismiss: () -> Unit, onPublish: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Nuevo aviso") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { if (it.length <= 120) title = it }, label = { Text("Título") }, enabled = !busy)
            OutlinedTextField(content, { if (it.length <= 5000) content = it }, label = { Text("Información para los vecinos") }, minLines = 4, maxLines = 8, enabled = !busy)
        }
    }, confirmButton = { TextButton(onClick = { onPublish(title, content) }, enabled = title.isNotBlank() && content.isNotBlank() && !busy) { Text("Publicar") } }, dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancelar") } })
}
