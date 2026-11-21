# Configuración y seguridad

Estado: instrucciones para el siguiente paso; servicios aún no creados.

## Firebase

1. Crear un proyecto en la consola Firebase de la cuenta del propietario. Registrar Android con package `cl.comunidad.conectada`.
2. Activar Authentication por correo y contraseña.
3. Crear **Realtime Database**, no sustituirla por Firestore. Mantener acceso denegado inicialmente.
4. Descargar `google-services.json` a `app/` (ignorado por Git). Añadir el plugin Google Services compatible en raíz/módulo y sincronizar. Aún no se añadió para que la base no dependa de un archivo inexistente.
5. Inicializar los adaptadores después de confirmar FirebaseApp. Usar `FirebaseDatabase.getInstance(URL_REAL_DE_LA_BASE)` si corresponde por región.
6. Crear dos cuentas de prueba y un administrador. Desde entorno confiable con Admin SDK, asignar claims de comunidad y administrador; nunca desde la app ni mediante registro libre.
7. Probar `firebase/database.rules.json` en Emulator Suite. Son una propuesta sin validar, no reglas certificadas. No desplegar antes de ejecutar pruebas positivas y negativas.
8. Implementar FCM Android, permisos API 33+, canales, actualización de token y apertura por ID. Añadir backend que observe creación de anuncios y envíe FCM a miembros autorizados.

Si Auth crea cuenta pero falla escritura de perfil, la pantalla deberá permitir completar el perfil al reingresar. No tratar el registro Auth como una operación atómica con Database.

Antes de elegir Cloud Functions u otro alojamiento, revisar requisitos de facturación y cuotas con el propietario. No activar pagos automáticamente. El envío manual de una notificación desde consola no reemplaza la integración anuncio → backend → FCM.

## Broker MQTT

Elegir un broker administrado privado con TLS y ACL, o Mosquitto propio con certificado válido. Falta escoger proveedor y verificar condiciones actuales. No usar un broker público anónimo para identidades reales.

- Host DNS válido y puerto TLS 8883 para el cliente preparado.
- Una credencial por dispositivo/usuario de prueba; no una contraseña global en el APK.
- ACL que permita publicar/suscribirse únicamente al tópico exacto de su comunidad.
- TLS con CA confiable y verificación del nombre del servidor; no aceptar todos los certificados.
- QoS 1 y eventos sin retain. Historial en Database.
- Provisionamiento seguro de credenciales pendiente; el adaptador solo las recibe en memoria. Paho puede conservarlas para reconectar durante la sesión. No persistirlas como texto plano.
- Restricciones de tamaño, frecuencia y clientes en broker. Probar rechazo de acceso entre comunidades.

Cerrar el cliente fuera del hilo principal al terminar la sesión; recoger Flow de Firebase con lifecycle y cancelarlo al salir. La reconexión automática restaura suscripción después de una conexión ya establecida; falta estrategia explícita para reintentar el primer intento fallido y una suscripción rechazada.

## GitHub

Ya existe un repositorio local en `main`. Falta un repositorio remoto en la cuenta del propietario y autenticación GitHub. Recomendada visibilidad privada mientras se prepara la evaluación.

Cuando exista un remoto vacío:

```bash
git remote add origin URL_DEL_REPOSITORIO
git push -u origin main
```

No se ha ejecutado push ni creado un repositorio remoto. El propietario aún no tiene uno. El plugin GitHub está disponible para conectar, pero no está instalado en esta sesión. Nunca guardar tokens en la URL del remoto ni en documentación.

## Fuentes primarias

- https://firebase.google.com/docs/android/setup
- https://firebase.google.com/docs/database/android/start
- https://firebase.google.com/docs/database/security
- https://firebase.google.com/docs/cloud-messaging/send/admin-sdk
- https://eclipse.dev/paho/clients/java/index.html
- https://eclipse.dev/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttAsyncClient.html
