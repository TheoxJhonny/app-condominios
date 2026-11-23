package cl.comunidad.conectada.ui

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.net.Uri
import cl.comunidad.conectada.data.AnnouncementLink
import cl.comunidad.conectada.MainActivity
import cl.comunidad.conectada.domain.CommunityAlert

object CommunityNotifications {
    fun showAnnouncement(context: Context, link: AnnouncementLink) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = "community-announcements"
        manager.createNotificationChannel(
            NotificationChannel(channel, "Avisos de administración", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val intent = Intent(context, MainActivity::class.java)
            .setData(Uri.Builder().scheme("neighborloop").authority("announcement")
                .appendPath(link.communityId).appendPath(link.id).build())
            .putExtra("announcementCommunity", link.communityId)
            .putExtra("announcementId", link.id)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = Notification.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOnlyAlertOnce(true)
            .setContentTitle("Nuevo aviso de administración")
            .setContentText("Toca para leer el aviso en NeighborLoop.")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        runCatching { manager.notify("announcement:${link.communityId}:${link.id}", 1, notification) }
    }

    fun showAlert(context: Context, alert: CommunityAlert) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                "community-alerts",
                "Alertas de la comunidad",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val intent = Intent(context, MainActivity::class.java).putExtra("openAlerts", true)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = Notification.Builder(context, "community-alerts")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Alerta comunitaria")
            .setContentText("${alert.name} · #${alert.home} ha solicitado ayuda.")
            .setContentIntent(pending).setAutoCancel(true).build()
        runCatching { manager.notify(alert.id, 1, notification) }
    }
}
