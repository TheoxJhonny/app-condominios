package cl.comunidad.conectada.data

import cl.comunidad.conectada.domain.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.UUID
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.HttpsURLConnection

/** Foreground transport. Caller owns lifecycle and marshals callbacks to the UI thread. */
class MqttAlertClient(
    host: String,
    private val communityId: String,
    private val onConnection: (Boolean) -> Unit,
    private val onAlert: (CommunityAlert) -> Unit,
    private val onError: (Throwable) -> Unit
) : AutoCloseable {
    init {
        require(host.matches(Regex("[A-Za-z0-9.-]+")))
    }

    private val topic = CommunityTopic.alerts(communityId)
    private val deduplicator = AlertDeduplicator()
    private val client =
        MqttAsyncClient("ssl://$host:8883", "android-${UUID.randomUUID()}", MemoryPersistence())

    init {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                // Restore the subscription after every reconnect; one callback per client instance.
                client.subscribe(topic, 1, null, listener({ onConnection(true) }))
            }

            override fun connectionLost(cause: Throwable?) {
                onConnection(false)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (topic != this@MqttAlertClient.topic || message == null || message.isRetained) return
                runCatching {
                    require(message.payload.size <= 4096)
                    val data = JSONObject(String(message.payload, Charsets.UTF_8))
                    val alert = CommunityAlert(
                        data.getString("id"),
                        data.getString("communityId"),
                        data.getString("userId"),
                        data.getString("name"),
                        data.getString("home"),
                        data.getLong("createdAt"),
                        subject = if (data.has("subject")) {
                            require(data.get("subject") is String)
                            data.getString("subject")
                        } else ""
                    )
                    require(data.getString("status") == "ACTIVE")
                    if (!AlertEventValidator.accepts(
                            alert,
                            communityId,
                            System.currentTimeMillis()
                        )
                    ) return
                    if (deduplicator.accept(alert.id)) onAlert(alert)
                }.onFailure(onError)
            }
        })
    }

    fun connect(username: String, password: CharArray) {
        require(username.isNotBlank() && password.isNotEmpty())
        val options = MqttConnectOptions().apply {
            userName = username
            setPassword(password.copyOf())
            isAutomaticReconnect = true
            isCleanSession = true
            connectionTimeout = 15
            keepAliveInterval = 30
            socketFactory = SSLSocketFactory.getDefault()
            sslHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
        }
        runCatching {
            client.connect(
                options,
                null,
                listener({ /* Subscription callback reports readiness. */ })
            )
        }.onFailure(onError)
    }

    fun publish(alert: CommunityAlert, onAcknowledged: () -> Unit) {
        require(alert.communityId == communityId)
        if (!client.isConnected) {
            onError(IllegalStateException("Alerta sin enviar: MQTT desconectado.")); return
        }
        val json = JSONObject().put("id", alert.id).put("communityId", alert.communityId)
            .put("userId", alert.userId)
            .put("name", alert.name).put("home", alert.home).put("createdAt", alert.createdAt)
            .put("status", "ACTIVE")
        if (alert.subject.isNotEmpty()) json.put("subject", alert.subject)
        runCatching {
            client.publish(
                topic,
                json.toString().toByteArray(Charsets.UTF_8),
                1,
                false,
                null,
                listener(onAcknowledged, affectsConnection = false)
            )
        }.onFailure(onError)
    }

    private fun listener(success: () -> Unit, affectsConnection: Boolean = true) =
        object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) = success()
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                if (affectsConnection) onConnection(false)
                onError(exception ?: IllegalStateException("Operación MQTT fallida."))
            }
        }

    override fun close() {
        // Invoke off the main thread: disconnectForcibly may wait for the transport.
        runCatching { if (client.isConnected) client.disconnectForcibly(100, 500) }
        runCatching { client.close(true) }
        onConnection(false)
    }
}
