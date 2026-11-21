package cl.comunidad.conectada.domain

import org.junit.Assert.*
import org.junit.Test

class AlertEventValidatorTest {
    private val now = 1_000_000L
    private val alert = CommunityAlert("f8ac3254-bdfb-4c96-99fb-08c537825c00", "comunidad01", "uid", "Alex", "311A", now)
    @Test fun acceptsFreshEventFromOwnCommunity() { assertTrue(AlertEventValidator.accepts(alert, "comunidad01", now)) }
    @Test fun rejectsDifferentCommunity() { assertFalse(AlertEventValidator.accepts(alert, "otra", now)) }
    @Test fun rejectsExpiredEvent() { assertFalse(AlertEventValidator.accepts(alert.copy(createdAt = now - AlertEventValidator.MAX_AGE_MS - 1), "comunidad01", now)) }
    @Test fun rejectsFutureReplayOrClockError() { assertFalse(AlertEventValidator.accepts(alert.copy(createdAt = now + AlertEventValidator.FUTURE_TOLERANCE_MS + 1), "comunidad01", now)) }
    @Test fun rejectsInvalidEventId() { assertFalse(AlertEventValidator.accepts(alert.copy(id = "anything"), "comunidad01", now)) }
    @Test fun rejectsClosedEvent() { assertFalse(AlertEventValidator.accepts(alert.copy(status = AlertStatus.CLOSED), "comunidad01", now)) }
    @Test fun rejectsEmptyIdentity() { assertFalse(AlertEventValidator.accepts(alert.copy(userId = ""), "comunidad01", now)) }
}
