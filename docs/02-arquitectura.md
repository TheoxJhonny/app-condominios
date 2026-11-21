# Arquitectura y comunicación

## Responsabilidades

| Componente | Responsabilidad |
|---|---|
| Compose | Pantallas, estados y confirmación de alarma |
| ViewModel / casos de uso (pendientes) | Sesión, carga, reintentos, coordinación de envío e historial |
| Firebase Auth | Correo/contraseña, UID, recuperación y sesión |
| Realtime Database | Perfiles, chat, anuncios e historial de alertas |
| Backend confiable (pendiente) | Envío FCM y asignación controlada de pertenencia/rol |
| Broker MQTT | Distribución de eventos a clientes conectados según ACL |
| Transporte MQTT | TLS, QoS 1, suscripción, reconexión y deduplicación de sesión |

El proyecto tiene paquetes `domain` y `data`. La UI inicial todavía no consume los adaptadores. Agregar ViewModels y repositorios observables antes de conectar pantallas; no llamar a los SDK directamente desde composables.

## Dos dispositivos

Ambos teléfonos instalan la misma app y usan cuentas distintas. Al pertenecer a la misma comunidad, consultan la misma ruta en Firebase. Un listener recibe los cambios del otro teléfono sin actualizar manualmente.

En MQTT ambos se conectan al mismo broker con identificadores de cliente diferentes. Se suscriben, por ejemplo, a `comunidades/comunidad01/alertas`. Cuando A publica, el broker distribuye el evento a A y B. No es una conexión directa entre teléfonos; pueden estar en redes distintas si el broker es accesible por Internet.

TLS protege el transporte y el broker debe verificar credenciales y ACL. El nombre del tópico por sí solo no impide que otra comunidad se suscriba. El broker tampoco reconoce automáticamente la sesión Firebase: hay que configurar la identidad y permisos MQTT por separado.

## Datos propuestos

```text
profiles/{uid}: name, home
communities/{communityId}/messages/{id}: userId, name, home, content, createdAt
communities/{communityId}/announcements/{id}: userId, title, content, createdAt
communities/{communityId}/alerts/{id}: communityId, userId, name, home, createdAt, status
```

Claims controlados mediante Admin SDK: `communityId` y `admin`. El cliente no escribe roles. Tras crear cuenta, puede completar perfil, pero no acceder a la comunidad hasta recibir pertenencia controlada. Implementar una pantalla de aprobación pendiente. Los claims requieren refrescar el token tras su asignación.

## Alarma: coordinación pendiente

1. Solicitar confirmación y bloquear pulsaciones repetidas durante el envío.
2. Crear un UUID una sola vez. Mantener el mismo ID en reintentos.
3. Si MQTT no está listo, mostrar sin enviar, nunca éxito.
4. Publicar con QoS 1 y `retain=false`.
5. Tras PUBACK, mostrar publicada al broker. PUBACK no prueba recepción humana ni entrega a todos los teléfonos.
6. Guardar el historial en Firebase con el mismo ID. Si falla, mostrar enviada pero pendiente de registro y reintentar solo el registro. Firebase y MQTT no forman una transacción atómica.
7. Receptores validan comunidad, payload y duplicados; muestran aviso y notificación local según permisos.
8. Solo el administrador modifica estado a atendida/cerrada; no genera una alarma nueva.

El adaptador actual deduplica hasta 512 IDs en memoria y rechaza mensajes retained. Falta deduplicación persistente con caducidad, validación de antigüedad y protección contra reproducción. La identidad incluida en un payload MQTT no es prueba criptográfica de autoría: para mayor seguridad se necesita autenticación por usuario y validación/sellado por backend.

## FCM y Android

Anuncio en Database → backend confiable → FCM dirigido a tokens de miembros autorizados → notificación → anuncio por ID, verificando acceso de nuevo. No incluir credenciales Admin SDK en Android. Usar un tópico FCM público predecible no garantiza privacidad entre comunidades.

El transporte MQTT propuesto es para primer plano. Android puede suspender o cerrar procesos. No se implementará un servicio permanente sin justificarlo. Registrar resultados con app abierta, en segundo plano y cerrada. Si más adelante se añade FCM de respaldo para alarmas, conservar MQTT y deduplicar ambos canales por el mismo ID.
