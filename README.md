# NeighborLoop

Aplicación Android de Jonathan Fuentealba para residentes y administradores de una comunidad. Kotlin + Jetpack Compose; Firebase Authentication, Realtime Database, FCM y MQTT.

## Estado real: interfaz funcional e integración preparada

Incluye diseño propio en Compose; pantallas de ingreso, registro, recuperación, perfil, chat, avisos y alertas; ViewModel con StateFlow; repositorio Firebase y transporte MQTT conectados a los flujos de la interfaz. Sin configuración Firebase, se puede explorar una demostración con datos locales, identificada de forma visible. La demostración no crea cuentas ni envía alarmas reales.

Firebase y broker privados todavía no existen, por lo que los flujos reales entre dispositivos siguen pendientes de verificación. FCM de anuncios y su backend emisor aún están pendientes. No confundir las notificaciones locales de MQTT con FCM.

No es todavía una entrega funcional para evaluación. No usar como sistema de emergencias.

## Abrir en Android Studio

1. Abrir esta carpeta como proyecto, con SDK Android 36 y JDK compatible con Android Gradle Plugin 9.1.1. El proyecto usa Kotlin integrado de AGP y el plugin Compose 2.2.10, alineado con Kotlin de AGP.
2. Sincronizar Gradle. La primera ejecución requiere Internet.
3. Ejecutar `./gradlew testDebugUnitTest assembleDebug` (Windows: `./gradlew.bat testDebugUnitTest assembleDebug`).
4. Ejecutar en emulador o teléfono Android 8.0/API 26 o superior.

El 21-11-2026 se ejecutaron desde Android Studio `:app:testDebugUnitTest :app:assembleDebug`: **BUILD SUCCESSFUL, 14 pruebas aprobadas**. El problema inicial del daemon por terminal se resolvió usando la ejecución del IDE. Firebase BoM quedó fijada en 34.7.0 por compatibilidad con Kotlin 2.2.10. Consultar [pruebas](docs/04-pruebas.md).

## Configuración pendiente

No se necesitan cuentas externas para explorar la demostración. Para las funciones reales se deben crear un proyecto Firebase y un broker MQTT privado con TLS y permisos por comunidad. Al añadir `app/google-services.json`, Gradle aplica Google Services automáticamente. Las credenciales MQTT se introducen en Alertas y se conservan solo durante la conexión en memoria. Nunca incluir contraseñas del broker, claves de servicio o tokens en Git.

Ver [configuración](docs/03-configuracion.md), [arquitectura](docs/02-arquitectura.md) y [etapas](docs/05-etapas.md). El informe lo prepara otra persona: [antecedentes técnicos para compartir](docs/06-insumos-informe.md).

## Documentación

- [Requisitos y revisión documental](docs/01-requisitos.md)
- [Arquitectura y flujo entre dispositivos](docs/02-arquitectura.md)
- [Firebase, MQTT y GitHub](docs/03-configuracion.md)
- [Pruebas y resultados reales](docs/04-pruebas.md)
- [Etapas y criterios de cierre](docs/05-etapas.md)
- [Insumos para quien redacta el informe](docs/06-insumos-informe.md)
- [Checkpoint de continuidad](docs/07-checkpoint.md)

## Git

Repositorio local en rama `main`; remoto GitHub aún no creado. Los commits describen etapas reales. No hay licencia de distribución definida todavía.
