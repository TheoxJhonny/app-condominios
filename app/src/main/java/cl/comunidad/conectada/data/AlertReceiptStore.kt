package cl.comunidad.conectada.data

import android.content.Context

/** Saves receipt IDs before displaying notifications, including across foreground reconnections. */
class AlertReceiptStore(context: Context) {
    private val prefs = context.getSharedPreferences("alert-receipts", Context.MODE_PRIVATE)
    @Synchronized fun accept(userId: String, community: String, eventId: String): Boolean {
        val key = "$userId:$community"
        val ids = prefs.getString(key, "").orEmpty().split(',').filter { it.isNotBlank() }
        if (eventId in ids) return false
        check(prefs.edit().putString(key, (ids + eventId).takeLast(512).joinToString(",")).commit())
        return true
    }
}
