# Interfaz e integración

| Pantalla | Funciones |
|---|---|
| Acceso | Ingreso, registro, recuperación de contraseña y Google si está configurado |
| Pertenencia | Completar perfil, consultar UID y comprobar acceso tras la admisión |
| Inicio | Galería de fachada, piscina, áreas verdes y terraza, avisos recientes y accesos rápidos |
| Chat | Últimos 50 mensajes y envío al canal de la comunidad |
| Avisos | Últimos 30 avisos, detalle con autor, publicación para administradores y apertura por ID desde una notificación FCM |
| Alertas | Conexión MQTT, confirmación de envío con descripción opcional de la emergencia de hasta 240 caracteres, historial y cambio de estado para administradores |
| Perfil | Edición de datos de residente, permiso de notificaciones y cierre de sesión |

La galería utiliza fotografías referenciales de Unsplash. Admite deslizamiento manual y avance cada seis segundos con opción de pausa. Descarga las imágenes por Internet y las mantiene en memoria durante el proceso de la app. El modo demostración conserva sus datos de ejemplo durante la sesión y no activa alertas reales.

Inicio adapta la cabecera al ancho disponible y al tamaño de texto del sistema. La galería muestra fotos a medida que llegan y permite reintentar si ninguna carga. La navegación conserva el estado guardable de cada pestaña durante la sesión. Las barras de progreso corresponden a operaciones activas, como la descarga de imágenes o la conexión MQTT.

Las alertas MQTT generan una notificación local si Android lo permite. Al tocarla, la app dirige a Alertas tras el acceso. La recepción en vivo requiere que la app esté abierta y conectada. La conexión se intenta restablecer al regresar al primer plano. La verificación manual está registrada en [pruebas](04-pruebas.md).
