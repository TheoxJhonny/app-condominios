# Estado y deuda técnica

El equipo verificó el acceso con Google, el perfil, el chat y la entrega de alertas MQTT. La revisión de código identifica estos pendientes, ordenados por impacto:

| Prioridad | Área | Hallazgo y cambio propuesto |
|---|---|---|
| Alta | Recuperación de alertas | El UUID y la fase de publicación o registro viven en `CommunityViewModel`. Si Android termina el proceso tras el acuse MQTT, puede quedar una alerta recibida sin historial. Persistir la operación por cuenta y comunidad, y retomar el registro con el mismo UUID. |
| Alta | Identidad de alertas | El historial RTDB valida al usuario autenticado, mientras que la alerta en vivo usa los datos del mensaje MQTT. Vincular la identidad del publicador con el evento mediante el servicio que autoriza o emite las alertas. |
| Media | Recuperación de feeds | `startFeeds()` retorna si queda cualquier listener activo. Si falla solo el de avisos o historial, los otros pueden seguir funcionando mientras ese feed queda congelado. Gestionar el reinicio por feed y exponer reintento en la pantalla afectada. |
| Media | Galería | `CommunityGallery` espera todas las descargas antes de mostrar la primera foto y conserva las imágenes en una caché en memoria. Empaquetar una imagen inicial o usar caché en disco y mostrar cada foto conforme esté disponible. |
| Media | Cobertura automatizada | Las pruebas en el repositorio cubren el dominio y la validación de eventos. Faltan pruebas de coordinación de publicación y reintentos, y de permisos de RTDB mediante Emulator Suite. |

FCM de avisos sigue siendo una funcionalidad pendiente. La atención y el cierre de alertas entre dispositivos siguen pendientes de validación manual.
