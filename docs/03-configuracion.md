# Configuración de Firebase y MQTT

## Firebase

El entorno de pruebas usa el proyecto `neighborloop-b915b`. Para preparar una instalación de desarrollo:

1. Obtener `google-services.json` para la app Android `cl.comunidad.conectada` y colocarlo en `app/`. El archivo debe incluir la URL de Realtime Database. Gradle aplica Google Services al detectar el archivo.
2. Habilitar los proveedores de Authentication que se usarán. Para Google, verificar la huella de la firma del APK y el recurso generado `default_web_client_id`.
3. Usar Realtime Database con las reglas de `firebase/database.rules.json`. La revisión de permisos en Emulator Suite está pendiente.
4. Para admitir a un residente, ejecutar `python firebase/admit_resident.py UID` desde un entorno autorizado con Firebase Admin SDK y credenciales de aplicación. Agregar `--apply` para asignar `comunidad01`. En el teléfono, pulsar **Comprobar mi acceso** para refrescar la sesión.

## Administrador de prueba

Usa una cuenta de Google distinta de la cuenta vecina. Inicia sesión al menos una vez para que Firebase Authentication cree el usuario y copia su UID. Desde un entorno con Firebase Admin SDK y credenciales autorizadas, ejecuta `python firebase/admit_resident.py UID --admin` para revisar la asignación. Agrega `--apply` para guardar el rol administrador en `comunidad01`. Cierra sesión y vuelve a entrar. En **Perfil** debe aparecer **Administrador** y en **Avisos**, **Publicar un aviso**.

## MQTT

Las pruebas manuales utilizaron EMQX Serverless por TLS en el puerto 8883. En **Alertas**, introducir host, usuario y contraseña. El broker debe restringir publicación y suscripción al tópico de la comunidad correspondiente. La app cifra las credenciales en el dispositivo por cuenta y comunidad, y las reutiliza al volver a entrar. Cerrar sesión limpia la conexión y las credenciales en memoria. Para borrar los datos guardados, usa **Desconectar y olvidar credenciales** o **Olvidar credenciales** en Alertas. Mantener credenciales administrativas y del broker fuera del repositorio.

## FCM

El emisor vive en `firebase/functions`. Usa una Cloud Function de segunda generación en `us-central1` para la base predeterminada de `neighborloop-b915b`. El despliegue de Cloud Functions requiere que el proyecto esté en el plan Blaze y que la cuenta tenga permisos para desplegar.

Desde la raíz del repositorio, con Node.js 22 y Firebase CLI autenticada:

```powershell
npm --prefix firebase/functions install
firebase deploy --only database,functions:notifyAnnouncement --project neighborloop-b915b
```

Después del despliegue, instalar la app en ambos dispositivos, volver a abrir la sesión para registrar cada instalación y habilitar las notificaciones desde **Avisos** o **Perfil**. Publicar un aviso nuevo desde la cuenta administradora. El aviso debe aparecer en Avisos y la notificación del vecino debe abrir ese mismo registro. El emisor solo envía identificadores y vuelve a comprobar la pertenencia de cada destinatario. Los títulos y contenidos se leen desde Realtime Database al abrir el aviso.

## Repositorio

El remoto `origin` corresponde a [app-condominios](https://github.com/TheoxJhonny/app-condominios). La rama principal es `main`.
