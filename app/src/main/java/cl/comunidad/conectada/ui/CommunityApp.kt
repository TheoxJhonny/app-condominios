package cl.comunidad.conectada.ui

import android.app.NotificationManager
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role as AccessibilityRole
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.comunidad.conectada.data.*
import cl.comunidad.conectada.domain.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityApp(
    state: CommunityState,
    model: CommunityViewModel,
    openAlerts: Boolean,
    onAlertsOpened: () -> Unit,
    announcementLink: AnnouncementLink?,
    onAnnouncementOpened: () -> Unit,
    onNotificationPermission: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirm by remember { mutableStateOf(false) }
    var composeAnnouncement by remember { mutableStateOf(false) }
    var detail by remember(state.resident?.uid, state.resident?.communityId) { mutableStateOf<Announcement?>(null) }
    LaunchedEffect(openAlerts, state.resident) {
        if (openAlerts && state.member) {
            tab = 3; onAlertsOpened()
        }
    }
    LaunchedEffect(announcementLink, state.member, state.resident?.uid, state.demo) {
        if (announcementLink != null && state.member && !state.demo) {
            val announcement = model.openAnnouncement(announcementLink)
            if (announcement != null) {
                tab = 2
                detail = announcement
            } else model.announcementUnavailable()
            onAnnouncementOpened()
        }
    }
    val labels = listOf("Inicio", "Chat", "Avisos", "Alertas", "Perfil")
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.Outlined.Email,
        Icons.Outlined.DateRange,
        Icons.Outlined.Warning,
        Icons.Outlined.Person
    )
    Scaffold(
        containerColor = Paper,
        topBar = {
            if (state.resident != null) {
                Column {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BrandMark()
                                Text(
                                    "NeighborLoop",
                                    style = MaterialTheme.typography.headlineMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            if (state.demo) Text(
                                "DEMO",
                                Modifier.padding(horizontal = 20.dp),
                                color = Cobalt,
                                style = MaterialTheme.typography.labelMedium
                            )
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
                        Row(Modifier
                            .fillMaxWidth()
                            .selectableGroup()) {
                            labels.forEachIndexed { index, title ->
                                val selected = tab == index
                                val color by animateColorAsState(
                                    if (selected) Cobalt else Ink, label = "Color de navegación"
                                )
                                val background by animateColorAsState(
                                    if (selected) CobaltWash else Paper, label = "Fondo de navegación"
                                )
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .background(background)
                                        .selectable(
                                            selected,
                                            role = AccessibilityRole.Tab,
                                            onClick = { tab = index })
                                        .heightIn(min = 60.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(if (selected) Cobalt else Paper)
                                    )
                                    Icon(
                                        icons[index],
                                        null,
                                        Modifier
                                            .padding(top = 6.dp)
                                            .size(22.dp),
                                        tint = color
                                    )
                                    Text(
                                        title,
                                        Modifier.padding(top = 3.dp, bottom = 6.dp),
                                        color = color,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier
            .padding(padding)
            .fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(3.dp)) {
                if (state.busy) LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = Cobalt,
                    trackColor = CobaltWash
                )
            }
            state.notice?.let { notice ->
                Surface(
                    color = CobaltWash,
                    modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    Row(
                        Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            notice,
                            Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        SquareIconButton(
                            onClick = model::dismissNotice,
                            icon = Icons.Default.Close,
                            label = "Cerrar mensaje"
                        )
                    }
                }
            }
            when {
                state.resident == null -> AccessScreen(state, model)
                !state.member -> MembershipScreen(state, model)
                else -> {
                    key(state.resident?.uid, state.resident?.communityId, state.demo) {
                        val tabs = rememberSaveableStateHolder()
                        tabs.SaveableStateProvider(tab) {
                            when (tab) {
                                0 -> HomeScreen(
                                    state,
                                    onChat = { tab = 1 },
                                    onAnnouncements = { tab = 2 },
                                    onAlert = { confirm = true },
                                    onDetail = { detail = it })

                                1 -> ChatScreen(state, model)
                                2 -> AnnouncementsScreen(
                                    state,
                                    onCreate = { composeAnnouncement = true },
                                    onNotificationPermission = onNotificationPermission,
                                    onDetail = { detail = it })

                                3 -> AlertsScreen(
                                    state,
                                    model,
                                    onAlert = { confirm = true },
                                    onNotificationPermission = onNotificationPermission
                                )

                                4 -> ProfileScreen(state, model, onNotificationPermission)
                            }
                        }
                    }
                }
            }
        }
    }
    if (confirm) {
        var subject by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            shape = Square,
            onDismissRequest = { confirm = false },
            icon = { Icon(Icons.Default.Warning, null) },
            title = { Text("¿Necesitas ayuda?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (state.demo) "Estás en una demostración. No se enviará ninguna alerta real."
                        else "Se compartirán la descripción, tu nombre y vivienda con los vecinos conectados.")
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {
                            subject = it.filterNot { character -> character.isISOControl() }
                                .take(AlertFactory.MAX_DESCRIPTION_LENGTH)
                        },
                        label = { Text("Descripción de la emergencia (opcional)") },
                        placeholder = { Text("Ej. El portón quedó bloqueado y hay un vehículo atrapado.") },
                        minLines = 3,
                        maxLines = 5,
                        supportingText = { Text("${subject.length} / ${AlertFactory.MAX_DESCRIPTION_LENGTH}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    shape = Square,
                    onClick = {
                        confirm = false
                        model.activateAlert(subject)
                    }) { Text(if (state.demo) "Comprobar estado" else "Confirmar alerta") }
            },
            dismissButton = {
                TextButton(
                    shape = Square,
                    onClick = { confirm = false }) { Text("Cancelar") }
            })
    }
    state.incoming?.let { alert ->
        AlertDialog(
            shape = Square,
            onDismissRequest = model::dismissIncoming,
            title = { Text("Un vecino necesita ayuda") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${alert.name} · Vivienda #${alert.home}")
                    Text(alert.subject.ifBlank { "Ha activado una alerta comunitaria." })
                }
            },
            confirmButton = {
                TextButton(
                    shape = Square,
                    onClick = { model.dismissIncoming(); tab = 3 }) { Text("Ver alertas") }
            })
    }
    detail?.let { announcement ->
        AlertDialog(
            shape = Square,
            onDismissRequest = { detail = null },
            title = { Text(announcement.title) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "${announcement.authorName.ifBlank { "Administración" }} · ${formatTime(announcement.createdAt)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(announcement.content)
                }
            },
            confirmButton = {
                TextButton(
                    shape = Square,
                    onClick = { detail = null }) { Text("Cerrar") }
            })
    }
    if (composeAnnouncement) AnnouncementEditor(
        state.busy,
        onDismiss = { composeAnnouncement = false }) { title, content ->
        model.publishAnnouncement(title, content) {
            composeAnnouncement = false
        }
    }
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
private fun HomeScreen(
    state: CommunityState,
    onChat: () -> Unit,
    onAnnouncements: () -> Unit,
    onAlert: () -> Unit,
    onDetail: (Announcement) -> Unit
) {
    val resident = checkNotNull(state.resident)
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HomeHeader(if (state.demo) "COMUNIDAD LOS ARRAYANES" else "VIVIENDA #${resident.home}")
        }
        item {
            SectionHeading("Avisos recientes")
        }
        item {
            Column {
                if (state.announcements.isEmpty()) {
                    EmptyState(
                        "Aún no hay avisos",
                        "Aquí aparecerán las publicaciones de administración."
                    )
                }
                state.announcements.take(2).forEachIndexed { index, announcement ->
                    AnnouncementRow(
                        announcement,
                        onDetail,
                        showPreview = false,
                        highlighted = index == 0
                    )
                }
            }
        }
        item {
            Button(
                onClick = onAlert,
                shape = Square,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 62.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    "Solicitar ayuda",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )
                Box(
                    Modifier
                        .size(34.dp)
                        .border(1.dp, Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
        item {
            SectionHeading("Accesos rápidos")
            Row(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickLink("Ver avisos", Icons.Outlined.DateRange, Modifier.weight(1f), onAnnouncements)
                QuickLink("Abrir chat", Icons.Outlined.Email, Modifier.weight(1f), onChat)
            }
        }
        if (state.demo) {
            item {
                Text(
                    "Demostración · Datos de ejemplo",
                    color = Muted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(community: String) {
    val fontScale = LocalDensity.current.fontScale
    Column {
        Text(
            community,
            Modifier.background(CobaltWash).padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Cobalt,
            letterSpacing = 1.sp
        )
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            if (maxWidth < 330.dp || fontScale > 1.25f) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeTitle()
                    CommunityGallery(Modifier.fillMaxWidth().height(220.dp))
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) { HomeTitle() }
                    CommunityGallery(Modifier.weight(1.1f).height(236.dp))
                }
            }
        }
        HorizontalDivider(color = Ink)
    }
}

@Composable
private fun HomeTitle() {
    Text(
        "Tu condominio",
        Modifier.semantics { heading() },
        style = MaterialTheme.typography.headlineLarge
    )
    Box(Modifier.padding(top = 10.dp).width(48.dp).height(5.dp).background(Cobalt))
}

@Composable
internal fun SectionHeading(title: String) {
    Row(
        Modifier.fillMaxWidth().semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, Modifier.weight(1f, fill = false), style = MaterialTheme.typography.titleLarge)
        HorizontalDivider(Modifier.weight(1f), color = Rule)
    }
}

@Composable
private fun QuickLink(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        shape = Square,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label)
        }
    }
}

@Composable
private fun SquareIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        shape = Square,
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(icon, label, Modifier.size(22.dp))
    }
}

@Composable
private fun ChatScreen(state: CommunityState, model: CommunityViewModel) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(Modifier
        .fillMaxSize()
        .imePadding()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Chat general",
                Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall
            )
            if (!state.demo) TextButton(
                shape = Square,
                onClick = model::retryFeeds
            ) { Text("Actualizar conexión") }
        }
        HorizontalDivider(color = Rule)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            if (state.messages.isEmpty()) item {
                EmptyState(
                    "La conversación empieza contigo",
                    "Saluda o comparte algo con tus vecinos."
                )
            }
            items(state.messages.reversed(), key = { it.id }) { message ->
                val own = message.userId == state.resident?.uid
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = if (own) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (own) CobaltWash else Color.White,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.widthIn(max = 320.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "${message.name} · #${message.home}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(message.content)
                            Text(
                                formatTime(message.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = Rule)
        Surface(color = Paper) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { if (it.length <= 2000) text = it },
                    label = { Text("Escribe a tus vecinos") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    enabled = !state.busy
                )
                SquareIconButton(
                    onClick = { model.sendMessage(text) { text = "" } },
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = "Enviar mensaje",
                    enabled = text.isNotBlank() && !state.busy,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AnnouncementsScreen(
    state: CommunityState,
    onCreate: () -> Unit,
    onNotificationPermission: () -> Unit,
    onDetail: (Announcement) -> Unit
) {
    val notificationManager = LocalContext.current.getSystemService(NotificationManager::class.java)
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Avisos de administración", style = MaterialTheme.typography.headlineMedium) }
        if (!state.demo && !notificationManager.areNotificationsEnabled()) item {
            OutlinedButton(onClick = onNotificationPermission, shape = Square) {
                Text("Habilitar notificaciones de avisos")
            }
        }
        if (state.resident?.role == Role.ADMIN && !state.demo) item {
            Button(
                shape = Square,
                onClick = onCreate
            ) { Text("Publicar un aviso") }
        }
        if (state.announcements.isEmpty()) item {
            EmptyState(
                "Aún no hay avisos",
                "Cuando administración publique información, podrás consultarla aquí."
            )
        }
        items(state.announcements, key = { it.id }) { AnnouncementRow(it, onDetail) }
    }
}

@Composable
private fun AnnouncementRow(
    item: Announcement,
    onDetail: (Announcement) -> Unit,
    showPreview: Boolean = true,
    highlighted: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = AccessibilityRole.Button) { onDetail(item) }
            .heightIn(min = 56.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier
            .size(9.dp)
            .background(if (highlighted) SignalOrange else Paper))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            if (showPreview) {
                Text(
                    "${item.authorName.ifBlank { "Administración" }} · ${formatTime(item.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted
                )
                Text(item.content, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Muted)
            }
        }
        Box(Modifier
            .size(32.dp)
            .border(1.dp, Ink), contentAlignment = Alignment.Center) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    HorizontalDivider(color = Rule)
}

@Composable
fun EmptyState(title: String, text: String) {
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Rule)
        Column(
            Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(text, color = Muted, style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider(color = Rule)
    }
}

fun formatTime(timestamp: Long): String =
    if (timestamp <= 0) "Fecha pendiente" else SimpleDateFormat(
        "dd MMM · HH:mm",
        Locale.forLanguageTag("es-CL")
    ).format(Date(timestamp))

@Composable
private fun AnnouncementEditor(
    busy: Boolean,
    onDismiss: () -> Unit,
    onPublish: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    AlertDialog(
        shape = Square,
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Nuevo aviso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    title,
                    { if (it.length <= 120) title = it },
                    label = { Text("Título") },
                    enabled = !busy
                )
                OutlinedTextField(
                    content,
                    { if (it.length <= 5000) content = it },
                    label = { Text("Información para los vecinos") },
                    minLines = 4,
                    maxLines = 8,
                    enabled = !busy
                )
            }
        },
        confirmButton = {
            TextButton(
                shape = Square,
                onClick = { onPublish(title, content) },
                enabled = title.isNotBlank() && content.isNotBlank() && !busy
            ) { Text("Publicar") }
        },
        dismissButton = {
            TextButton(
                shape = Square,
                onClick = onDismiss,
                enabled = !busy
            ) { Text("Cancelar") }
        })
}
