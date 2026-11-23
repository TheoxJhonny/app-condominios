package cl.comunidad.conectada.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cl.comunidad.conectada.ui.CommunityNotifications
import java.util.UUID

data class AnnouncementLink(val communityId: String, val id: String) {
    companion object {
        private val validKey = Regex("[A-Za-z0-9_-]{1,128}")

        fun parse(communityId: String?, id: String?): AnnouncementLink? {
            if (communityId == null || id == null ||
                !validKey.matches(communityId) || !validKey.matches(id)
            ) return null
            return AnnouncementLink(communityId, id)
        }
    }
}

internal object AnnouncementPush {
    private fun preferences(context: Context) =
        context.getSharedPreferences("announcement_notifications", Context.MODE_PRIVATE)

    fun bind(context: Context, uid: String, communityId: String) {
        val preferences = preferences(context)
        val installation = preferences.getString("installation", null) ?: UUID.randomUUID().toString()
        preferences.edit().putString("installation", installation)
            .putString("uid", uid).putString("community", communityId).apply()
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            updateToken(context, token)
        }
    }

    fun unbind(context: Context) {
        val preferences = preferences(context)
        val uid = preferences.getString("uid", null)
        val community = preferences.getString("community", null)
        val installation = preferences.getString("installation", null)
        preferences.edit().remove("uid").remove("community").apply()
        if (uid != null && community != null && installation != null && FirebaseAuth.getInstance().currentUser?.uid == uid) {
            tokenRef(community, uid, installation).removeValue()
        }
    }

    fun updateToken(context: Context, token: String) {
        val preferences = preferences(context)
        val uid = preferences.getString("uid", null) ?: return
        val installation = preferences.getString("installation", null) ?: return
        val community = preferences.getString("community", null) ?: return
        if (FirebaseAuth.getInstance().currentUser?.uid != uid) return
        tokenRef(community, uid, installation).setValue(token)
    }

    fun accepts(context: Context, recipient: String?, link: AnnouncementLink): Boolean {
        val preferences = preferences(context)
        return recipient != null &&
            FirebaseAuth.getInstance().currentUser?.uid == recipient &&
            preferences.getString("uid", null) == recipient &&
            preferences.getString("community", null) == link.communityId
    }

    private fun tokenRef(communityId: String, uid: String, installation: String) =
        FirebaseDatabase.getInstance().reference.child("announcementDevices").child(communityId).child(uid)
            .child(installation)
}

class AnnouncementMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AnnouncementPush.updateToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["kind"] != "announcement") return
        val link = AnnouncementLink.parse(message.data["communityId"], message.data["announcementId"])
            ?: return
        if (AnnouncementPush.accepts(this, message.data["recipientUid"], link)) {
            CommunityNotifications.showAnnouncement(this, link)
        }
    }
}
