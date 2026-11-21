# Etapa: interfaz y conexión con los adaptadores

21 de noviembre de 2026.

## Implementado

- Diseño propio con superficies marfil, verde profundo, tarjetas amplias y cinco destinos: Inicio, Chat, Avisos, Alertas y Perfil.
- Ingreso y registro con validaciones. Contraseñas en estado transitorio, sin persistencia por rotación ni logs. Recuperación por correo.
- Perfiles incompletos: posibilidad de completarlos; cuenta sin pertenencia: pantalla de espera con UID y refresco de claims.
- Roles procedentes de claims; no se eligen desde la interfaz.
- Chat observable de los últimos 50 mensajes con identidad y fecha; envío y reintento de listeners.
- Avisos observables de los últimos 30, lectura de detalle y publicación solo para admin en la UI. La seguridad efectiva depende de las reglas Firebase, todavía sin validar en Emulator Suite.
- Historial de 30 alertas con acciones de atención/cierre por administrador.
- Conexión manual al broker privado desde la pantalla Alertas; TLS 8883, credenciales transitorias, QoS 1, sin retain. No se conecta en demostración.
- Publicación y registro separados. Tras PUBACK se registra en Database. Reintentar registro no vuelve a publicar. El registro usa una transacción para conservar fecha y estado de un evento existente.
- Aviso visual y notificación local MQTT con permiso Android. Tocar la notificación dirige a Alertas después del acceso.
- Deduplicación en memoria y persistencia de hasta 512 IDs por usuario/comunidad para reconexiones; no guarda nombres ni contraseñas. Rechazo de payloads de más de 4096 bytes, UUID inválido, comunidad ajena, estado no activo, antigüedad mayor de 10 minutos o fecha futura más de 2 minutos. Mantener relojes de pruebas sincronizados.
- Los listeners se cancelan al pasar a segundo plano. MQTT se desconecta; al regresar se debe conectar de nuevo desde Alertas. No afirmar recepción con app cerrada.
- Demostración con datos locales de ejemplo: navegar, consultar avisos, escribir en chat y editar perfil durante la sesión. Salir descarta los datos de demostración. Nunca muestra una alarma como enviada.

## Verificación real

Android Studio pudo sincronizar y ejecutar Gradle. La primera compilación detectó metadata Kotlin 2.4.0 en Firebase Auth 25.0.0 de BoM 35.2.0. Se fijó BoM 34.7.0 compatible y se repitió la compilación.

`:app:testDebugUnitTest :app:assembleDebug`: BUILD SUCCESSFUL. XML de JUnit: CommunityTest 7/7; AlertEventValidatorTest 7/7; cero fallos y errores.

En el emulador Pixel 8 API 37.1 se verificó el arranque, el acceso a la demostración y la navegación de Inicio a Chat con mensajes de ejemplo. No existen aún pruebas reales Firebase/MQTT/FCM ni resultados de usabilidad con participantes.

## Acceso con Google

El botón de acceso utiliza Credential Manager y Firebase Authentication. Se muestra cuando Firebase está configurado y Gradle genera el recurso `default_web_client_id` a partir de `app/google-services.json`. La cuenta de Google pasa por el flujo existente para completar el perfil y comprobar pertenencia.

El archivo local de NeighborLoop contiene la configuración OAuth, pero todavía no incluye `firebase_url`. Crear Realtime Database, publicar las reglas del proyecto y descargar de nuevo la configuración permitirá probar la carga y el guardado del perfil desde el emulador.

## Verificación en teléfono y acceso de prueba

Jonathan confirmó en un teléfono físico el inicio de sesión con Google y la persistencia del perfil tras volver a abrir la app. La configuración local ya incorpora la URL de Realtime Database.

Desde una terminal administrativa con Firebase Admin SDK para Python y credenciales de aplicación autorizadas para `neighborloop-b915b`, ejecuta `python firebase/admit_resident.py UID` para ver la operación. Añade `--apply` para asignar la cuenta a `comunidad01` como residente. Copia el UID desde Firebase Authentication. Después pulsa **Comprobar mi acceso** en la app para refrescar el token. El script no solicita ni guarda claves y rechaza cuentas deshabilitadas, administradores y miembros de otra comunidad.

La cuenta de prueba fue admitida en `comunidad01` como residente. Jonathan confirmó que el chat en tiempo real funcionó entre el teléfono físico y el emulador con cuentas distintas de la misma comunidad.

## Próximo trabajo

1. Crear Firebase, activar correo/contraseña, RTDB y registrar Android. Añadir google-services.json y URL regional si se necesita.
2. Asignar pertenencia y administrador mediante un entorno confiable, probar reglas y crear dos residentes de prueba.
3. Configurar broker TLS con credenciales y ACL por comunidad; probar publicación, recepción, duplicados, cortes de red y rechazo entre comunidades.
4. Implementar FCM de anuncios y backend emisor seguro; no colocar credenciales Admin SDK en la app.
5. Añadir recuperación de envío pendiente tras muerte del proceso y limpieza de receipts de cuentas retiradas. El estado de publicación/registro pendiente actual vive en el ViewModel y se pierde al cerrar el proceso.
6. Revisar Excel y cerrar alcance de compraventa. Mantener Google como extra opcional.

### Reconexión del servicio de alertas

Las credenciales MQTT introducidas en Alertas se mantienen en memoria durante la sesión. Al pasar la app a segundo plano se cierra la conexión y, al volver, se intenta restablecer automáticamente. El botón **Reconectar servicio** permite reintentar con las mismas credenciales. Desconectar manualmente o cerrar sesión descarta las credenciales.

Para verificarlo, envía dos alertas seguidas con ambos dispositivos conectados. Luego lleva la app receptora a segundo plano, vuelve a abrirla y verifica que se reconecte sin pedir las credenciales. Envía otra alerta y comprueba el aviso en vivo y su registro en el historial.

### Pruebas manuales de integración

El 21 de noviembre de 2026 se verificaron el acceso con Google, la persistencia del perfil y el chat en tiempo real entre un teléfono Android y un emulador con cuentas distintas de `comunidad01`. Con EMQX Serverless sobre TLS, se observó la recepción de una alerta en vivo y su registro como Activa en Firebase. Tras corregir la reconexión, se enviaron dos alertas seguidas con ambos clientes conectados y se verificó que la app restablece MQTT al volver del segundo plano sin volver a escribir la contraseña.

Pendiente de prueba manual: enviar una alerta como residente, marcarla como Atendida y luego Cerrada como administrador, y comprobar que ambos dispositivos actualicen el mismo registro.
