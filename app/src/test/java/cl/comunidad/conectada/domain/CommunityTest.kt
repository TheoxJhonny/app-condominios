package cl.comunidad.conectada.domain

import org.junit.Assert.*
import org.junit.Test

class CommunityTest {
    @Test fun emptyRegistrationReportsEveryRequiredField() {
        assertEquals(setOf("name", "home", "email", "password"), RegistrationValidator.validate("", "", "", "").keys)
    }
    @Test fun alphanumericHomeIsAccepted() {
        assertTrue(RegistrationValidator.validate("Jonathan", "311A", "vecino@example.com", "12345678").isEmpty())
    }
    @Test fun alertsHaveUniqueIdsAndResidentIdentity() {
        val user = Resident("uid", "Jonathan", "311A", "comunidad01")
        val first = AlertFactory.create(user, 1000)
        assertNotEquals(first.id, AlertFactory.create(user, 1000).id)
        assertEquals("311A", first.home)
        assertEquals("uid", first.userId)
        assertEquals(AlertStatus.ACTIVE, first.status)
    }
    @Test fun repeatedAlertIsIgnored() {
        val cache = AlertDeduplicator()
        assertTrue(cache.accept("id")); assertFalse(cache.accept("id")); assertFalse(cache.accept(""))
    }
    @Test fun duplicateCacheIsBounded() {
        val cache = AlertDeduplicator(2)
        cache.accept("a"); cache.accept("b"); cache.accept("c")
        assertFalse(cache.accept("c")); assertTrue(cache.accept("a"))
    }
    @Test(expected = IllegalArgumentException::class) fun topicRejectsWildcardInjection() { CommunityTopic.alerts("otra/#") }
    @Test fun topicSeparatesCommunities() { assertEquals("comunidades/comunidad01/alertas", CommunityTopic.alerts("comunidad01")) }
}
