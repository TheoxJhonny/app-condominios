# NeighborLoop

Aplicación Android para residentes y administradores de condominios, desarrollada por el grupo **Git Happens**.

NeighborLoop reúne el chat vecinal, los avisos de administración y las alertas de ayuda. Está desarrollada en Kotlin con Jetpack Compose y utiliza Firebase Authentication, Realtime Database y MQTT.

## Estado del proyecto

- Acceso por correo y contraseña y con Google cuando está configurado. Los residentes completan su perfil y un administrador autoriza su acceso al condominio.
- Chat en tiempo real, avisos de administración, historial de alertas y gestión de estados por administradores.
- Alertas en vivo mediante MQTT con TLS y notificaciones locales de Android. La conexión funciona en primer plano y se restablece al volver a la app durante la sesión.
- Inicio con galería de fotografías referenciales, avisos recientes y accesos rápidos. La galería requiere Internet.
- Modo demostración con datos locales de ejemplo, sin cuentas reales ni envío de alertas.

El acceso con Google, la persistencia del perfil, el chat y el envío y la recepción de alertas se probaron entre un teléfono y un emulador. Quedan pendientes las notificaciones de avisos mediante FCM y la validación de permisos y del cierre de alertas entre dispositivos.

## Ejecutar en Android Studio

1. Abrir el repositorio como proyecto Android, instalar el SDK 36 y sincronizar Gradle.
2. Seleccionar un emulador o teléfono con Android 8.0 (API 26) o superior y ejecutar el módulo `app`.
3. Para ejecutar las pruebas unitarias y generar el APK de depuración desde PowerShell:

   ```powershell
   .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
   ```

El modo demostración está disponible sin configuración de Firebase. Para conectar la app al entorno compartido, consulta la [configuración](docs/03-configuracion.md).

## Documentación

- [Requisitos y estado](docs/01-requisitos.md)
- [Arquitectura y flujos de datos](docs/02-arquitectura.md)
- [Configuración de Firebase y MQTT](docs/03-configuracion.md)
- [Pruebas y resultados](docs/04-pruebas.md)
- [Etapas de implementación](docs/05-etapas.md)
- [Antecedentes para el informe](docs/06-insumos-informe.md)
- [Estado y deuda técnica](docs/07-checkpoint.md)
- [Interfaz e integración](docs/08-interfaz-e-integracion.md)
