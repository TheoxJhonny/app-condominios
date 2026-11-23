package cl.comunidad.conectada.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.comunidad.conectada.data.AlertRecord
import cl.comunidad.conectada.domain.*

@Composable
fun AlertsScreen(
    state: CommunityState,
    model: CommunityViewModel,
    onAlert: () -> Unit,
    onNotificationPermission: () -> Unit
) {
    var showConnection by remember { mutableStateOf(false) }
    val canManageAlerts = state.member && !state.demo && state.resident?.role == Role.ADMIN
    var alertToDelete by remember(state.resident?.uid, state.resident?.communityId) {
        mutableStateOf<AlertRecord?>(null)
    }
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { Text("Alertas comunitarias", style = MaterialTheme.typography.headlineMedium) }
        item {
            Surface(
                color = if (state.mqttConnected) CobaltWash else Color.White,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val connectionLabel = when {
                        state.mqttConnected -> "Servicio conectado"
                        state.mqttConnecting -> "Conectando al servicio"
                        else -> "Servicio desconectado"
                    }
                    val statusColor = when {
                        state.mqttConnected -> Cobalt
                        state.mqttConnecting -> SignalOrange
                        else -> Muted
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(9.dp).background(statusColor))
                        Text(
                            connectionLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor
                        )
                    }
                    if (state.mqttConnecting) {
                        LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp))
                    }
                    Text(
                        "Las alertas en tiempo real se reciben con la aplicación abierta y conectada.",
                        color = Muted
                    )
                    val connectedOrConnecting = state.mqttConnected || state.mqttConnecting
                    OutlinedButton(
                        shape = Square,
                        onClick = {
                            when {
                                connectedOrConnecting -> model.disconnectMqtt()
                                state.mqttConfigured -> model.reconnectMqtt()
                                else -> showConnection = true
                            }
                        },
                        enabled = !state.demo
                    ) {
                        Text(
                            when {
                                connectedOrConnecting -> "Desconectar"
                                state.mqttConfigured -> "Reconectar servicio"
                                else -> "Conectar servicio"
                            }
                        )
                    }
                    if (state.mqttConfigured && !connectedOrConnecting) {
                        TextButton(
                            shape = Square,
                            onClick = { showConnection = true }) { Text("Cambiar credenciales") }
                    }
                }
            }
        }
        item {
            Button(
                shape = Square,
                onClick = onAlert,
                enabled = state.delivery.canStartAnother(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) {
                Icon(
                    Icons.Default.Warning,
                    null
                ); Spacer(Modifier.width(12.dp)); Text("Solicitar ayuda a mis vecinos")
            }
        }
        item {
            val deliveryText = when (val delivery = state.delivery) {
                AlertDelivery.Idle -> "Se solicitará confirmación antes de activar una alerta."
                is AlertDelivery.Publishing -> "Publicando alerta al servicio…"
                is AlertDelivery.Recording -> "El broker confirmó la publicación. Guardando historial…"
                is AlertDelivery.Sent -> "Publicación confirmada por el broker e historial guardado. Esto no confirma que todos los vecinos la hayan visto."
                is AlertDelivery.Failed -> if (delivery.brokerAcknowledged) "Publicada al broker. El historial está pendiente de registro." else "No se pudo confirmar el envío. Reintenta con el mismo identificador."
            }
            Text(deliveryText, style = MaterialTheme.typography.bodySmall, color = Muted)
            if (state.delivery is AlertDelivery.Failed) TextButton(
                shape = Square,
                onClick = model::retryAlert
            ) { Text("Reintentar operación pendiente") }
        }
        item {
            TextButton(
                shape = Square,
                onClick = onNotificationPermission
            ) { Text("Habilitar notificaciones en este dispositivo") }
        }
        item { SectionHeading("Historial de la comunidad") }
        if (state.alerts.isEmpty()) item {
            EmptyState(
                "Sin alertas registradas",
                "El historial mostrará quién solicitó ayuda y el estado de atención."
            )
        }
        items(state.alerts, key = { it.id }) { alert ->
            Surface(color = Color.White, shape = MaterialTheme.shapes.medium) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("${alert.name} · #${alert.home}", fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Descripción de la emergencia", style = MaterialTheme.typography.labelMedium)
                        Text(
                            alert.subject.ifBlank { "Sin descripción registrada" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (alert.subject.isBlank()) Muted else Ink
                        )
                    }
                    Text(
                        formatTime(alert.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                    Text(
                        when (alert.status) {
                            "ACTIVE" -> "Activa"; "ACKNOWLEDGED" -> "Atendida"; "CLOSED" -> "Cerrada"; else -> "Estado no reconocido"
                        }, color = if (alert.status == "ACTIVE") AlertRed else Ink
                    )
                    if (canManageAlerts && alert.status == AlertStatus.CLOSED.name) {
                        TextButton(
                            shape = Square,
                            onClick = { alertToDelete = alert },
                            enabled = !state.busy
                        ) { Text("Eliminar alerta", color = if (state.busy) Muted else AlertRed) }
                    }
                    if (canManageAlerts && alert.status != AlertStatus.CLOSED.name) Row {
                        if (alert.status == "ACTIVE") TextButton(
                            shape = Square,
                            onClick = { model.updateAlert(alert.id, AlertStatus.ACKNOWLEDGED) },
                            enabled = !state.busy
                        ) { Text("Marcar atendida") }
                        TextButton(
                            shape = Square,
                            onClick = { model.updateAlert(alert.id, AlertStatus.CLOSED) },
                            enabled = !state.busy
                        ) { Text("Cerrar") }
                    }
                }
            }
        }
    }
    alertToDelete?.takeIf { canManageAlerts }?.let { alert ->
        AlertDialog(
            shape = Square,
            onDismissRequest = { if (!state.busy) alertToDelete = null },
            title = { Text("¿Eliminar esta alerta cerrada?") },
            text = { Text("La alerta de ${alert.name} · #${alert.home} desaparecerá del historial de toda la comunidad.") },
            confirmButton = {
                TextButton(
                    shape = Square,
                    enabled = !state.busy && state.alerts.any { it.id == alert.id && it.status == AlertStatus.CLOSED.name },
                    onClick = { model.deleteClosedAlert(alert.id) { alertToDelete = null } }
                ) { Text("Eliminar", color = if (state.busy) Muted else AlertRed) }
            },
            dismissButton = {
                TextButton(
                    shape = Square,
                    enabled = !state.busy,
                    onClick = { alertToDelete = null }
                ) { Text("Cancelar") }
            }
        )
    }
    if (showConnection) ConnectionDialog(onDismiss = {
        showConnection = false
    }) { host, username, password ->
        model.connectMqtt(host, username, password)
        showConnection = false
    }
}

@Composable
private fun ConnectionDialog(onDismiss: () -> Unit, onConnect: (String, String, String) -> Unit) {
    var host by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    AlertDialog(
        shape = Square,
        onDismissRequest = onDismiss,
        title = { Text("Conectar alertas") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Usa las credenciales asignadas por administración. Conexión segura TLS. Las credenciales se conservan en memoria durante esta sesión.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    host,
                    { host = it },
                    label = { Text("Host del broker") },
                    placeholder = { Text("broker.ejemplo.cl") },
                    singleLine = true
                )
                OutlinedTextField(
                    username,
                    { username = it },
                    label = { Text("Usuario") },
                    singleLine = true
                )
                OutlinedTextField(
                    password,
                    { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            TextButton(
                shape = Square,
                onClick = { onConnect(host, username, password); password = "" },
                enabled = host.isNotBlank() && username.isNotBlank() && password.isNotEmpty()
            ) { Text("Conectar") }
        },
        dismissButton = { TextButton(shape = Square, onClick = onDismiss) { Text("Cancelar") } })
}
