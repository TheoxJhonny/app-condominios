# Configuración de Firebase y MQTT

## Firebase

El entorno de pruebas usa el proyecto `neighborloop-b915b`. Para preparar una instalación de desarrollo:

1. Obtener `google-services.json` para la app Android `cl.comunidad.conectada` y colocarlo en `app/`. El archivo debe incluir la URL de Realtime Database. Gradle aplica Google Services al detectar el archivo.
2. Habilitar los proveedores de Authentication que se usarán. Para Google, verificar la huella de la firma del APK y el recurso generado `default_web_client_id`.
3. Usar Realtime Database con las reglas de `firebase/database.rules.json`. La revisión de permisos en Emulator Suite está pendiente.
4. Para admitir a un residente, ejecutar `python firebase/admit_resident.py UID` desde un entorno autorizado con Firebase Admin SDK y credenciales de aplicación. Agregar `--apply` para asignar `comunidad01`. En el teléfono, pulsar **Comprobar mi acceso** para refrescar la sesión.

## MQTT

Las pruebas manuales utilizaron EMQX Serverless por TLS en el puerto 8883. En **Alertas**, introducir host, usuario y contraseña. El broker debe restringir publicación y suscripción al tópico de la comunidad correspondiente. Al desconectar manualmente o cerrar sesión, la app descarta las credenciales de sesión. Mantener credenciales administrativas y del broker fuera del repositorio.

## FCM

La publicación de avisos todavía carece de emisor FCM. La integración requiere un backend autorizado, registro de tokens por miembro y apertura del aviso por ID con verificación de acceso.

## Repositorio

El remoto `origin` corresponde a [app-condominios](https://github.com/TheoxJhonny/app-condominios). La rama principal es `main`.
