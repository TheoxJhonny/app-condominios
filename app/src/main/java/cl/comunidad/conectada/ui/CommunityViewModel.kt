package cl.comunidad.conectada.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.comunidad.conectada.data.*
import cl.comunidad.conectada.domain.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class CommunityState(
    val configured: Boolean = false,
    val demo: Boolean = false,
    val busy: Boolean = false,
    val resident: Resident? = null,
    val messages: List<ChatMessage> = emptyList(),
    val announcements: List<Announcement> = emptyList(),
    val alerts: List<AlertRecord> = emptyList(),
    val notice: String? = null,
    val mqttConnected: Boolean = false,
    val mqttConnecting: Boolean = false,
    val mqttConfigured: Boolean = false,
    val incoming: CommunityAlert? = null,
    val delivery: AlertDelivery = AlertDelivery.Idle
) {
    val member: Boolean get() = resident?.let { it.communityId.isNotBlank() && it.name.isNotBlank() && it.home.isNotBlank() } == true
}

class CommunityViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = runCatching {
        val firebase =
            FirebaseApp.getApps(app).firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
                ?: return@runCatching null
        FirebaseCommunityRepository(
            FirebaseAuth.getInstance(firebase),
            FirebaseDatabase.getInstance(firebase)
        )
    }.getOrNull()
    private val mutable = MutableStateFlow(CommunityState(configured = repository != null))
    val state: StateFlow<CommunityState> = mutable.asStateFlow()
    private val feeds = mutableListOf<Job>()

    private class MqttCredentials(val host: String, val username: String, val password: CharArray)

    private var mqttCredentials: MqttCredentials? = null
    private var mqtt: MqttAlertClient? = null
    private var connectionGeneration = 0
    private var foreground = false
    private val receipts = AlertReceiptStore(app)

    init {
        if (repository?.hasSession == true) refreshSession()
    }

    fun dismissNotice() {
        mutable.update { it.copy(notice = null) }
    }

    fun signInWithGoogle(requestToken: suspend () -> String?) = operation {
        val repo = requireRepository()
        val idToken = requestToken() ?: return@operation
        repo.signInWithGoogle(idToken)
        loadSession(repo)
    }

    fun dismissIncoming() {
        mutable.update { it.copy(incoming = null) }
    }

    fun notifyPermission(granted: Boolean) {
        notice(if (granted) "Notificaciones habilitadas." else "Puedes consultar los avisos y las alertas dentro de la aplicación. Las notificaciones están desactivadas.")
    }

    fun authenticate(
        register: Boolean,
        name: String,
        home: String,
        email: String,
        password: String
    ) = operation {
        val repo = requireRepository()
        if (register) {
            require(RegistrationValidator.validate(name, home, email, password).isEmpty())
            repo.register(name, home, email, password)
        } else repo.signIn(email, password)
        loadSession(repo)
    }

    fun resetPassword(email: String) = operation {
        require(
            email.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
        ) { "Ingresa un correo válido." }
        requireRepository().resetPassword(email)
        notice("Si el correo tiene una cuenta, recibirás instrucciones para recuperar el acceso.")
    }

    fun refreshSession() = operation { loadSession(requireRepository()) }
    private suspend fun loadSession(repo: FirebaseCommunityRepository) {
        stopFeeds()
        val resident = repo.resident()
        mutable.update {
            it.copy(
                resident = resident,
                demo = false,
                messages = emptyList(),
                announcements = emptyList(),
                alerts = emptyList()
            )
        }
        val app = getApplication<Application>()
        if (state.value.member) AnnouncementPush.bind(app, resident.uid, resident.communityId)
        else AnnouncementPush.unbind(app)
        startFeeds()
    }

    fun announcementUnavailable() = notice("No se pudo abrir el aviso. Puedes volver a buscarlo en Avisos.")

    suspend fun openAnnouncement(link: AnnouncementLink): Announcement? {
        val current = state.value
        val resident = current.resident ?: return null
        if (current.demo || !current.member || resident.communityId != link.communityId) return null
        return try {
            val announcement = requireRepository().announcement(link)
            val active = state.value
            announcement?.takeIf {
                active.member && !active.demo &&
                    active.resident?.uid == resident.uid &&
                    active.resident.communityId == link.communityId
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
    }

    fun saveProfile(name: String, home: String) = operation {
        if (state.value.demo) {
            val user = state.value.resident ?: return@operation
            require(
                RegistrationValidator.validate(name, home, "demo@example.com", "123456").isEmpty()
            )
            mutable.update { it.copy(resident = user.copy(name = name.trim(), home = home.trim())) }
            notice("Perfil de demostración actualizado solo en esta sesión.")
        } else {
            val repo = requireRepository()
            repo.saveProfile(name, home)
            loadSession(repo)
            notice("Perfil actualizado.")
        }
    }

    fun signOut() {
        stopFeeds(); disconnectMqtt()
        AnnouncementPush.unbind(getApplication<Application>())
        repository?.signOut()
        mutable.value = CommunityState(configured = repository != null)
    }

    fun enterDemo() {
        if (state.value.busy) return
        stopFeeds(); disconnectMqtt()
        AnnouncementPush.unbind(getApplication<Application>())
        val now = System.currentTimeMillis()
        mutable.value = CommunityState(
            configured = repository != null, demo = true,
            resident = Resident("demo", "Alex", "311A", "comunidad-demo"),
            messages = listOf(
                ChatMessage(
                    "demo-1",
                    "elena",
                    "Elena",
                    "204",
                    "¡Hola! ¿Alguien recibió un paquete a mi nombre?",
                    now - 3600000
                ),
                ChatMessage(
                    "demo-2",
                    "tomas",
                    "Tomás",
                    "105B",
                    "Sí, quedó en conserjería. Puedes pasar a retirarlo.",
                    now - 3000000
                )
            ), announcements = listOf(
                Announcement(
                    "demo-a",
                    "admin-demo",
                    "Un espacio para encontrarnos",
                    "Este sábado compartiremos una jornada en el jardín común. Trae tus ideas para mejorar nuestra comunidad.",
                    now - 7200000
                ),
                Announcement(
                    "demo-b",
                    "admin-demo",
                    "Mantención de ascensores",
                    "El martes se realizará una revisión preventiva entre las 10:00 y las 12:00. Gracias por tu comprensión.",
                    now - 86400000
                )
            )
        )
    }

    fun sendMessage(text: String, onSent: () -> Unit) = operation {
        require(text.trim().length in 1..2000) { "Escribe un mensaje de hasta 2000 caracteres." }
        val resident = state.value.resident ?: error("Debes iniciar sesión.")
        if (state.value.demo) {
            val message = ChatMessage(
                java.util.UUID.randomUUID().toString(),
                resident.uid,
                resident.name,
                resident.home,
                text.trim(),
                System.currentTimeMillis()
            )
            mutable.update { it.copy(messages = (it.messages + message).takeLast(50)) }
        } else requireRepository().sendMessage(resident, text)
        onSent()
    }

    fun publishAnnouncement(title: String, content: String, onSaved: () -> Unit) = operation {
        check(!state.value.demo && state.value.resident?.role == Role.ADMIN) { "Solo el administrador puede publicar." }
        requireRepository().publishAnnouncement(checkNotNull(state.value.resident), title, content)
        onSaved()
        notice("Aviso publicado.")
    }

    fun deleteAnnouncement(id: String, onDeleted: () -> Unit) = operation {
        val current = state.value
        val resident = checkNotNull(current.resident)
        check(current.member && !current.demo && resident.role == Role.ADMIN)
        val link = requireNotNull(AnnouncementLink.parse(resident.communityId, id))
        requireRepository().deleteAnnouncement(link)
        onDeleted()
        notice("Aviso eliminado.")
    }

    fun updateAlert(id: String, status: AlertStatus) = operation {
        check(!state.value.demo && state.value.resident?.role == Role.ADMIN)
        requireRepository().updateAlertStatus(state.value.resident!!.communityId, id, status)
    }

    fun setForeground(active: Boolean) {
        foreground = active
        if (active) {
            startFeeds()
            reconnectMqtt()
        } else {
            stopFeeds()
            closeMqttConnection()
        }
    }

    fun retryFeeds() {
        stopFeeds(); startFeeds()
    }

    private fun startFeeds() {
        val current = state.value
        if (!foreground || current.demo || !current.member || feeds.any { it.isActive }) return
        val repo = repository ?: return
        val community = current.resident!!.communityId
        feeds += viewModelScope.launch {
            repo.messages(community)
                .catch { notice("No se pudo cargar el chat. Comprueba tu conexión y reintenta.") }
                .collect { value -> mutable.update { it.copy(messages = value) } }
        }
        feeds += viewModelScope.launch {
            repo.announcements(community)
                .catch { notice("No se pudieron cargar los anuncios. Puedes reintentar.") }
                .collect { value -> mutable.update { it.copy(announcements = value.reversed()) } }
        }
        feeds += viewModelScope.launch {
            repo.alerts(community)
                .catch { notice("No se pudo cargar el historial de alertas. Puedes reintentar.") }
                .collect { value -> mutable.update { it.copy(alerts = value.reversed()) } }
        }
    }

    private fun stopFeeds() {
        feeds.forEach { it.cancel() }; feeds.clear()
    }

    fun connectMqtt(host: String, username: String, password: String) {
        if (state.value.demo) {
            notice("El modo demostración no se conecta a servicios externos."); return
        }
        if (!state.value.member || !foreground || state.value.mqttConnecting) return
        disconnectMqtt()
        mqttCredentials = MqttCredentials(host.trim(), username.trim(), password.toCharArray())
        mutable.update { it.copy(mqttConfigured = true) }
        reconnectMqtt()
    }

    fun reconnectMqtt() {
        val credentials = mqttCredentials ?: return
        val current = state.value
        if (!foreground || !current.member || current.demo || current.mqttConnected || current.mqttConnecting) return
        closeMqttConnection()
        val generation = connectionGeneration
        mutable.update { it.copy(mqttConnecting = true) }
        runCatching {
            mqtt = MqttAlertClient(
                credentials.host, current.resident!!.communityId,
                onConnection = { connected ->
                    viewModelScope.launch {
                        if (generation == connectionGeneration) mutable.update {
                            it.copy(
                                mqttConnected = connected,
                                mqttConnecting = false
                            )
                        }
                    }
                },
                onAlert = { alert ->
                    viewModelScope.launch {
                        val user = state.value.resident
                        if (generation == connectionGeneration && user != null) {
                            val accepted = runCatching {
                                withContext(Dispatchers.IO) {
                                    receipts.accept(
                                        user.uid,
                                        user.communityId,
                                        alert.id
                                    )
                                }
                            }.getOrDefault(false)
                            if (accepted && generation == connectionGeneration) {
                                mutable.update { it.copy(incoming = alert) }
                                CommunityNotifications.showAlert(getApplication(), alert)
                            }
                        }
                    }
                },
                onError = { failure ->
                    viewModelScope.launch {
                        if (generation == connectionGeneration) {
                            mutable.update { it.copy(mqttConnecting = false) }
                            val delivery = state.value.delivery
                            if (delivery is AlertDelivery.Publishing) mutable.update {
                                it.copy(
                                    delivery = AlertDelivery.Failed(delivery.alert, false)
                                )
                            }
                            notice("No se completó la operación MQTT. Comprueba host, credenciales y conexión.")
                        }
                    }
                }
            ).also { it.connect(credentials.username, credentials.password) }
        }.onFailure { mutable.update { it.copy(mqttConnecting = false) }; notice("Revisa el host y los datos de conexión MQTT.") }
    }

    fun disconnectMqtt() {
        mqttCredentials?.password?.fill('\u0000')
        mqttCredentials = null
        mutable.update { it.copy(mqttConfigured = false) }
        closeMqttConnection()
    }

    private fun closeMqttConnection() {
        connectionGeneration++
        val previous = mqtt; mqtt = null
        mutable.update { current ->
            current.copy(
                mqttConnected = false, mqttConnecting = false,
                delivery = (current.delivery as? AlertDelivery.Publishing)?.let {
                    AlertDelivery.Failed(
                        it.alert,
                        false
                    )
                } ?: current.delivery)
        }
        if (previous != null) viewModelScope.launch(Dispatchers.IO) { previous.close() }
    }

    fun activateAlert(subject: String) {
        if (!state.value.delivery.canStartAnother()) return
        if (state.value.demo || !state.value.mqttConnected) {
            notice("Alerta sin enviar. Debes conectarte al servicio de alertas."); return
        }
        val user = state.value.resident ?: return
        publish(AlertFactory.create(user, System.currentTimeMillis(), subject))
    }

    fun retryAlert() {
        val failed = state.value.delivery as? AlertDelivery.Failed ?: return
        if (failed.brokerAcknowledged) recordAlert(failed.alert)
        else if (state.value.mqttConnected) publish(failed.alert) else notice("Conéctate a MQTT antes de reintentar.")
    }

    private fun publish(alert: CommunityAlert) {
        val transport = mqtt ?: return
        mutable.update { it.copy(delivery = AlertDelivery.Publishing(alert)) }
        val generation = connectionGeneration
        transport.publish(alert) {
            viewModelScope.launch {
                if (generation == connectionGeneration) recordAlert(alert)
            }
        }
    }

    private fun recordAlert(alert: CommunityAlert) {
        mutable.update { it.copy(delivery = AlertDelivery.Recording(alert)) }
        viewModelScope.launch {
            try {
                requireRepository().recordAlert(alert)
                if (state.value.resident?.uid == alert.userId) mutable.update {
                    it.copy(
                        delivery = AlertDelivery.Sent(
                            alert
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                if (state.value.resident?.uid == alert.userId) {
                    mutable.update { it.copy(delivery = AlertDelivery.Failed(alert, true)) }
                    notice("Alerta publicada al broker; falta guardar el historial. Reintenta el registro.")
                }
            }
        }
    }

    private fun operation(block: suspend () -> Unit) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true, notice = null) }
        viewModelScope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                notice(
                    when (failure) {
                        is FirebaseAuthException -> "No se pudo completar el acceso. Revisa tus datos y la configuración de la cuenta."
                        is IllegalArgumentException -> failure.message
                            ?: "Revisa los campos ingresados."

                        else -> "No se pudo completar la operación. Comprueba la conexión y vuelve a intentar."
                    }
                )
            } finally {
                mutable.update { it.copy(busy = false) }
            }
        }
    }

    private fun notice(text: String) {
        mutable.update { it.copy(notice = text) }
    }

    private fun requireRepository() = repository ?: error("Firebase aún no está configurado.")
    override fun onCleared() {
        mqttCredentials?.password?.fill('\u0000')
        mqttCredentials = null
        connectionGeneration++; stopFeeds()
        // Lifecycle-independent cleanup; no callback can mutate a disposed session.
        mqtt?.let { client -> Thread { client.close() }.start() }
        mqtt = null
    }
}
