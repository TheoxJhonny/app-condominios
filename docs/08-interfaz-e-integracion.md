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

## Próximo trabajo

1. Crear Firebase, activar correo/contraseña, RTDB y registrar Android. Añadir google-services.json y URL regional si se necesita.
2. Asignar pertenencia y administrador mediante un entorno confiable, probar reglas y crear dos residentes de prueba.
3. Configurar broker TLS con credenciales y ACL por comunidad; probar publicación, recepción, duplicados, cortes de red y rechazo entre comunidades.
4. Implementar FCM de anuncios y backend emisor seguro; no colocar credenciales Admin SDK en la app.
5. Añadir recuperación de envío pendiente tras muerte del proceso y limpieza de receipts de cuentas retiradas. El estado de publicación/registro pendiente actual vive en el ViewModel y se pierde al cerrar el proceso.
6. Revisar Excel y cerrar alcance de compraventa. Mantener Google como extra opcional.
