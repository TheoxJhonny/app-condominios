# Comunidad Conectada

Aplicación Android de Jonathan Fuentealba para residentes y administradores de una comunidad. Kotlin + Jetpack Compose; Firebase Authentication, Realtime Database, FCM y MQTT.

## Estado real: etapa inicial

Se incluyen una interfaz inicial propia, modelos, validaciones, siete pruebas unitarias, un adaptador Firebase y un transporte MQTT. Los adaptadores todavía no están conectados a la interfaz ni probados contra servicios reales. No hay autenticación operativa desde la pantalla, ni notificaciones FCM implementadas. El botón de ayuda informa explícitamente que no ha enviado ninguna alerta.

No es todavía una entrega funcional para evaluación. No usar como sistema de emergencias.

## Abrir en Android Studio

1. Abrir esta carpeta como proyecto, con SDK Android 36 y JDK compatible con Android Gradle Plugin 9.1.1. El proyecto usa Kotlin integrado de AGP y el plugin Compose 2.2.10, alineado con Kotlin de AGP.
2. Sincronizar Gradle. La primera ejecución requiere Internet.
3. Ejecutar `./gradlew testDebugUnitTest assembleDebug` (Windows: `./gradlew.bat testDebugUnitTest assembleDebug`).
4. Ejecutar en emulador o teléfono Android 8.0/API 26 o superior.

El APK aún no se ha compilado en este entorno: la conexión local con el daemon de Gradle fue bloqueada. Consultar [pruebas](docs/04-pruebas.md).

## Configuración pendiente

No se necesitan cuentas externas para revisar el código y la interfaz inicial. Para las funciones reales se deben crear un proyecto Firebase y un broker MQTT privado con TLS y permisos por comunidad. Nunca incluir contraseñas del broker, claves de servicio o tokens en Git.

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
