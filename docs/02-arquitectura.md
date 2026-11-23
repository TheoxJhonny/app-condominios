# Arquitectura y comunicación

| Componente | Responsabilidad |
|---|---|
| Jetpack Compose | Navegación, formularios, estados y confirmación de alertas |
| CommunityViewModel | Sesión, suscripciones de datos y coordinación de alertas |
| Firebase Authentication | Identidad, acceso por correo o Google y recuperación |
| Realtime Database | Perfiles, chat, avisos e historial |
| Cliente Paho y broker MQTT | Envío y recepción en vivo por comunidad mediante TLS |
| Android | Notificaciones locales de alertas y apertura de la pantalla Alertas |
| Emisor FCM | Cloud Function que recibe el evento de creación de un aviso y avisa a instalaciones registradas de miembros vigentes |

## Datos y pertenencia

- `profiles/{uid}` guarda nombre y vivienda. El UID identifica la cuenta y una vivienda puede agrupar a varios residentes.
- `communities/{communityId}/messages/{id}` guarda el chat.
- `communities/{communityId}/announcements/{id}` guarda los avisos.
- `communities/{communityId}/alerts/{id}` guarda el historial y estado de las alertas.

La app obtiene la pertenencia y el rol de los claims `communityId` y `admin`. Una cuenta puede completar su perfil antes de recibir acceso a su comunidad. **Comprobar mi acceso** refresca el token. Las reglas de RTDB y los permisos del broker se administran por separado.

## Alertas

1. La persona confirma la solicitud de ayuda. La app asigna un UUID que reutiliza en reintentos.
2. El cliente publica en `comunidades/{communityId}/alertas` con QoS 1 y sin retención.
3. Tras el acuse del broker, la app registra el evento en Firebase con el mismo ID. El acuse confirma la publicación al broker. La recepción en cada dispositivo depende de su conexión.
4. Los receptores validan antigüedad, comunidad, tamaño y duplicados. Si Android tiene permiso, se muestra una notificación local.
5. Un administrador puede cambiar el estado del registro a Atendida o Cerrada.

Al pasar a segundo plano se cierran los listeners de datos y MQTT. Al volver se restablecen las suscripciones y se intenta reconectar con las credenciales de la sesión. El envío pendiente se conserva en memoria del ViewModel.
