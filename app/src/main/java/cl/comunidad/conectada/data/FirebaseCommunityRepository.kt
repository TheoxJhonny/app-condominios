package cl.comunidad.conectada.data

import cl.comunidad.conectada.domain.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine

data class ChatMessage(
    val id: String = "", val userId: String = "", val name: String = "",
    val home: String = "", val content: String = "", val createdAt: Long = 0
)

/** Requires an initialized FirebaseApp and a controlled community membership. */
class FirebaseCommunityRepository(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {
    val hasSession: Boolean get() = auth.currentUser != null
    suspend fun resident(): Resident {
        val user = auth.currentUser ?: error("Debes iniciar sesión.")
        val token = user.getIdToken(true).await()
        val profile = database.reference.child("profiles").child(user.uid).get().await()
        return Resident(
            user.uid, profile.child("name").getValue(String::class.java).orEmpty(),
            profile.child("home").getValue(String::class.java).orEmpty(),
            token.claims["communityId"] as? String ?: "",
            if (token.claims["admin"] == true) Role.ADMIN else Role.RESIDENT
        )
    }

    fun announcements(communityId: String): Flow<List<Announcement>> = observe(
        community(communityId).child("announcements").orderByChild("createdAt").limitToLast(30),
        Announcement::class.java
    )

    suspend fun announcement(link: AnnouncementLink): Announcement? =
        community(link.communityId).child("announcements").child(link.id)
            .get().await().getValue(Announcement::class.java)

    fun alerts(communityId: String): Flow<List<AlertRecord>> = observe(
        community(communityId).child("alerts").orderByChild("createdAt").limitToLast(30),
        AlertRecord::class.java
    )

    private fun <T : Any> observe(query: Query, type: Class<T>): Flow<List<T>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                runCatching { snapshot.children.mapNotNull { it.getValue(type) } }
                    .onSuccess { trySend(it) }.onFailure { close(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun signInWithGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
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
        database.reference.child("profiles").child(uid())
            .setValue(mapOf("name" to name.trim(), "home" to home.trim())).await()
    }

    suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun messages(communityId: String): Flow<List<ChatMessage>> = callbackFlow {
        val query =
            community(communityId).child("messages").orderByChild("createdAt").limitToLast(50)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    suspend fun sendMessage(resident: Resident, content: String) {
        require(resident.uid == uid() && content.trim().length in 1..2000)
        val ref = community(resident.communityId).child("messages").push()
        ref.setValue(
            mapOf(
                "id" to ref.key,
                "userId" to resident.uid,
                "name" to resident.name,
                "home" to resident.home,
                "content" to content.trim(),
                "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    suspend fun publishAnnouncement(resident: Resident, title: String, content: String) {
        require(resident.uid == uid())
        require(title.trim().length in 1..120 && content.trim().length in 1..5000)
        // Database rules enforce administrator claims. FCM must be sent by a trusted backend.
        val ref = community(resident.communityId).child("announcements").push()
        ref.setValue(
            mapOf(
                "id" to ref.key, "userId" to resident.uid, "title" to title.trim(),
                "authorName" to resident.name,
                "content" to content.trim(), "createdAt" to ServerValue.TIMESTAMP
            )
        ).await()
    }

    suspend fun recordAlert(alert: CommunityAlert) {
        require(alert.userId == uid())
        val payload = mapOf(
            "id" to alert.id, "communityId" to alert.communityId, "userId" to alert.userId,
            "name" to alert.name, "home" to alert.home, "createdAt" to ServerValue.TIMESTAMP,
            "status" to "ACTIVE"
        )
        // Preserve original timestamp and administrator status when retrying the same event.
        suspendCancellableCoroutine<Unit> { continuation ->
            community(alert.communityId).child("alerts").child(alert.id)
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(current: MutableData): Transaction.Result {
                        if (current.value == null) current.value = payload
                        else if (current.child("userId").value != alert.userId || current.child("communityId").value != alert.communityId) return Transaction.abort()
                        return Transaction.success(current)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (!continuation.isActive) return
                        continuation.resumeWith(
                            if (error == null && committed) Result.success(Unit)
                            else Result.failure(
                                error?.toException()
                                    ?: IllegalStateException("No se pudo registrar la alerta.")
                            )
                        )
                    }
                }, false)
        }
    }

    suspend fun updateAlertStatus(communityId: String, alertId: String, status: AlertStatus) {
        require(status != AlertStatus.ACTIVE)
        community(communityId).child("alerts").child(alertId).child("status").setValue(status.name)
            .await()
    }

    private fun uid(): String = auth.currentUser?.uid ?: error("Debes iniciar sesión.")
    private fun community(id: String): DatabaseReference {
        CommunityTopic.alerts(id) // Validate before building a database path.
        uid()
        return database.reference.child("communities").child(id)
    }
}
