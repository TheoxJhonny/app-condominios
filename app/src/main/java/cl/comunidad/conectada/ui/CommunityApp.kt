package cl.comunidad.conectada.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    val icons = listOf(Icons.Default.Home, Icons.Default.Email, Icons.Default.DateRange, Icons.Default.Warning, Icons.Default.Person)
    Scaffold(containerColor = Paper,
        topBar = { if (state.resident != null) TopAppBar(title = { Column {
            Text("COMUNIDAD", style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(labels[tab], style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        } }, actions = { if (state.demo) Text("DEMO", Modifier.padding(20.dp), style = MaterialTheme.typography.labelMedium) }) },
        bottomBar = { if (state.member) NavigationBar(containerColor = Color.White) {
            labels.forEachIndexed { index, title -> NavigationBarItem(selected = tab == index,
                onClick = { tab = index }, icon = { Icon(icons[index], contentDescription = null) },
                label = { Text(title, style = MaterialTheme.typography.labelSmall) }) }
        } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.notice?.let { notice -> Surface(color = Mint, modifier = Modifier.fillMaxWidth()) {
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
private fun HomeScreen(state: CommunityState, onChat: () -> Unit, onAnnouncements: () -> Unit, onAlert: () -> Unit, onDetail: (Announcement) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Text("Hola, ${state.resident!!.name.substringBefore(' ')}.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Lo que pasa cerca, nos importa.", Modifier.padding(top = 8.dp), color = Muted)
        }
        item { Surface(color = Ink, shape = MaterialTheme.shapes.large) { Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("UN LUGAR EN COMÚN", color = Mint, style = MaterialTheme.typography.labelMedium)
            Text("Conversemos.\nCuidémonos.", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Mantente al día y encuentra a tus vecinos.", color = Mint)
            FilledTonalButton(onClick = onChat) { Text("Ir al chat de la comunidad") }
        } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("Información oficial", "Avisos de administración", Icons.Default.DateRange, Modifier.weight(1f), onAnnouncements)
            ActionTile("Solicitar ayuda", if (state.mqttConnected) "Servicio conectado" else "Servicio desconectado", Icons.Default.Warning, Modifier.weight(1f), onAlert)
        } }
        item { Text("En tu comunidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        if (state.announcements.isEmpty()) item { EmptyState("Todo tranquilo por aquí", "Los próximos avisos de administración aparecerán en este espacio.") }
        items(state.announcements.take(2), key = { it.id }) { AnnouncementCard(it, onDetail) }
        item { Text("Vivienda #${state.resident!!.home} · ${if (state.resident.role == Role.ADMIN) "Administrador" else "Residente"}", style = MaterialTheme.typography.labelMedium, color = Muted) }
    }
}

@Composable
private fun ActionTile(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp).heightIn(min = 130.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, tint = Ink)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
    }
}

@Composable
private fun ChatScreen(state: CommunityState, model: CommunityViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Chat general", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            if (!state.demo) TextButton(onClick = model::retryFeeds) { Text("Actualizar conexión") }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), reverseLayout = true) {
            if (state.messages.isEmpty()) item { EmptyState("La conversación empieza contigo", "Saluda o comparte algo con tus vecinos.") }
            items(state.messages.reversed(), key = { it.id }) { message ->
                val own = message.userId == state.resident?.uid
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (own) Alignment.End else Alignment.Start) {
                    Surface(color = if (own) Mint else Color.White, shape = MaterialTheme.shapes.medium, modifier = Modifier.widthIn(max = 320.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${message.name} · #${message.home}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(message.content)
                            Text(formatTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = Muted)
                        }
                    }
                }
            }
        }
        Surface(color = Color.White) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = text, onValueChange = { if (it.length <= 2000) text = it }, label = { Text("Escribe a tus vecinos") }, modifier = Modifier.weight(1f), maxLines = 4, enabled = !state.busy)
            IconButton(onClick = { model.sendMessage(text) { text = "" } }, enabled = text.isNotBlank() && !state.busy, modifier = Modifier.padding(bottom = 6.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar mensaje") }
        } }
    }
}

@Composable
private fun AnnouncementsScreen(state: CommunityState, onCreate: () -> Unit, onDetail: (Announcement) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("La información,\na tu alcance.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text("Novedades y avisos oficiales de administración.", color = Muted) }
        if (state.resident?.role == Role.ADMIN && !state.demo) item { Button(onClick = onCreate) { Text("Publicar un aviso") } }
        if (state.announcements.isEmpty()) item { EmptyState("Aún no hay avisos", "Cuando administración publique información, podrás consultarla aquí.") }
        items(state.announcements, key = { it.id }) { AnnouncementCard(it, onDetail) }
    }
}

@Composable
private fun AnnouncementCard(item: Announcement, onDetail: (Announcement) -> Unit) {
    Card(onClick = { onDetail(item) }, colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("ADMINISTRACIÓN · ${formatTime(item.createdAt)}", style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(item.content, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, color = Muted)
            Text("Leer aviso →", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun EmptyState(title: String, text: String) {
    Surface(color = Color.White, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(text, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
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
