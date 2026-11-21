package cl.comunidad.conectada.domain

/** Broker acknowledgement and database persistence are separate outcomes. */
sealed interface AlertDelivery {
    data object Idle : AlertDelivery
    data class Publishing(val alert: CommunityAlert) : AlertDelivery
    data class Recording(val alert: CommunityAlert) : AlertDelivery
    data class Sent(val alert: CommunityAlert) : AlertDelivery
    data class Failed(val alert: CommunityAlert, val brokerAcknowledged: Boolean) : AlertDelivery
}

fun AlertDelivery.canStartAnother(): Boolean =
    this is AlertDelivery.Idle || this is AlertDelivery.Sent
