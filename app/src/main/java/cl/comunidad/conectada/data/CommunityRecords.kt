package cl.comunidad.conectada.data

data class Announcement(
    val id: String = "", val userId: String = "", val title: String = "",
    val content: String = "", val createdAt: Long = 0
)
data class AlertRecord(
    val id: String = "", val communityId: String = "", val userId: String = "",
    val name: String = "", val home: String = "", val createdAt: Long = 0, val status: String = "ACTIVE"
)
