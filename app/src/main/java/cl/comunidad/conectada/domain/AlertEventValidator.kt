package cl.comunidad.conectada.domain

object AlertEventValidator {
    const val MAX_AGE_MS = 10 * 60 * 1000L
    const val FUTURE_TOLERANCE_MS = 2 * 60 * 1000L
    fun accepts(alert: CommunityAlert, communityId: String, now: Long): Boolean =
        alert.communityId == communityId &&
                alert.id.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) &&
                alert.userId.isNotBlank() && alert.userId.length <= 128 &&
                alert.name.trim().length in 2..60 && alert.home.matches(Regex("[A-Za-z0-9-]{1,12}")) &&
                alert.subject.length <= AlertFactory.MAX_DESCRIPTION_LENGTH &&
                alert.subject.none { it.isISOControl() } &&
                alert.status == AlertStatus.ACTIVE && alert.createdAt > 0 &&
                alert.createdAt >= now - MAX_AGE_MS && alert.createdAt <= now + FUTURE_TOLERANCE_MS
}
