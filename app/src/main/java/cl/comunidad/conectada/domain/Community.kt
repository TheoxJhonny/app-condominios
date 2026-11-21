package cl.comunidad.conectada.domain

import java.util.UUID

enum class Role { RESIDENT, ADMIN }
enum class AlertStatus { ACTIVE, ACKNOWLEDGED, CLOSED }
enum class DeliveryStatus { PENDING, SENT, FAILED }

data class Resident(val uid: String, val name: String, val home: String, val communityId: String, val role: Role = Role.RESIDENT) {
    val label: String get() = "$name · #$home"
}
data class CommunityAlert(
    val id: String, val communityId: String, val userId: String,
    val name: String, val home: String, val createdAt: Long,
    val status: AlertStatus = AlertStatus.ACTIVE
)

object RegistrationValidator {
    fun validate(name: String, home: String, email: String, password: String): Map<String, String> = buildMap {
        if (name.trim().length !in 2..60) put("name", "Ingresa un nombre entre 2 y 60 caracteres.")
        if (!home.trim().matches(Regex("[A-Za-z0-9-]{1,12}"))) put("home", "Usa letras, números o guiones; por ejemplo, 311A.")
        if (!email.trim().matches(Regex("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))) put("email", "Ingresa un correo válido.")
        if (password.length < 6) put("password", "La contraseña debe tener al menos 6 caracteres.")
    }
}

object AlertFactory {
    fun create(resident: Resident, now: Long): CommunityAlert {
        require(resident.uid.isNotBlank() && resident.communityId.isNotBlank())
        require(resident.name.isNotBlank() && resident.home.isNotBlank() && now > 0)
        return CommunityAlert(UUID.randomUUID().toString(), resident.communityId, resident.uid, resident.name, resident.home, now)
    }
}

/** Bounded foreground-session cache. Persistent deduplication belongs in the MQTT stage. */
class AlertDeduplicator(private val capacity: Int = 512) {
    init { require(capacity > 0) }
    private val seen = LinkedHashSet<String>()
    @Synchronized fun accept(id: String): Boolean {
        if (id.isBlank() || !seen.add(id)) return false
        if (seen.size > capacity) seen.remove(seen.first())
        return true
    }
}

object CommunityTopic {
    fun alerts(communityId: String): String {
        require(communityId.matches(Regex("[A-Za-z0-9_-]{1,64}")))
        return "comunidades/$communityId/alertas"
    }
}
