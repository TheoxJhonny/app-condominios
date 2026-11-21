package cl.comunidad.conectada.data

import cl.comunidad.conectada.domain.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "", val userId: String = "", val name: String = "",
    val home: String = "", val content: String = "", val createdAt: Long = 0
)

/** Requires an initialized FirebaseApp and a controlled community membership. */
class FirebaseCommunityRepository(private val auth: FirebaseAuth, private val database: FirebaseDatabase) {
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }
    suspend fun register(name: String, home: String, email: String, password: String) {
        require(RegistrationValidator.validate(name, home, email, password).isEmpty())
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("No se pudo crear la cuenta.")
        // Membership and role are assigned by a trusted administrator, never by this payload.
        database.reference.child("profiles").child(user.uid).setValue(
            mapOf("name" to name.trim(), "home" to home.trim())
        ).await()
    }
    suspend fun saveProfile(name: String, home: String) {
        require(name.trim().length in 2..60 && home.trim().matches(Regex("[A-Za-z0-9-]{1,12}")))
        database.reference.child("profiles").child(uid()).setValue(mapOf("name" to name.trim(), "home" to home.trim())).await()
    }
    suspend fun resetPassword(email: String) { auth.sendPasswordResetEmail(email.trim()).await() }
    fun signOut() { auth.signOut() }

    fun messages(communityId: String): Flow<List<ChatMessage>> = callbackFlow {
        val query = community(communityId).child("messages").orderByChild("createdAt").limitToLast(50)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) })
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }
    suspend fun sendMessage(resident: Resident, content: String) {
        require(resident.uid == uid() && content.trim().length in 1..2000)
        val ref = community(resident.communityId).child("messages").push()
        ref.setValue(mapOf("id" to ref.key, "userId" to resident.uid, "name" to resident.name,
            "home" to resident.home, "content" to content.trim(), "createdAt" to ServerValue.TIMESTAMP)).await()
    }
    suspend fun publishAnnouncement(communityId: String, title: String, content: String) {
        require(title.trim().length in 1..120 && content.trim().length in 1..5000)
        // Database rules enforce administrator claims. FCM must be sent by a trusted backend.
        val ref = community(communityId).child("announcements").push()
        ref.setValue(mapOf("id" to ref.key, "userId" to uid(), "title" to title.trim(),
            "content" to content.trim(), "createdAt" to ServerValue.TIMESTAMP)).await()
    }
    suspend fun recordAlert(alert: CommunityAlert) {
        require(alert.userId == uid())
        community(alert.communityId).child("alerts").child(alert.id).setValue(mapOf(
            "id" to alert.id, "communityId" to alert.communityId, "userId" to alert.userId,
            "name" to alert.name, "home" to alert.home, "createdAt" to ServerValue.TIMESTAMP,
            "status" to "ACTIVE"
        )).await()
    }
    suspend fun updateAlertStatus(communityId: String, alertId: String, status: AlertStatus) {
        require(status != AlertStatus.ACTIVE)
        community(communityId).child("alerts").child(alertId).child("status").setValue(status.name).await()
    }
    private fun uid(): String = auth.currentUser?.uid ?: error("Debes iniciar sesión.")
    private fun community(id: String): DatabaseReference {
        CommunityTopic.alerts(id) // Validate before building a database path.
        uid()
        return database.reference.child("communities").child(id)
    }
}
