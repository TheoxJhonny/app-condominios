# Checkpoint de continuidad

Fecha: 21 de noviembre de 2026. Rama: `main`. Proyecto local: `AppCondominios`.

## Preparado

- README y documentación de requisitos, arquitectura, configuración, pruebas, etapas e insumos del informe.
- Proyecto Android Kotlin/Compose, navegación y estado de servicios desconectados.
- Modelos, validación de registro y pruebas de dominio.
- Adaptador Firebase Auth/RTDB y transporte Paho MQTT TLS; sin conexión a UI.
- Propuesta inicial de reglas Firebase. No desplegada ni validada.
- Gradle Wrapper 9.3.1.

## Confirmado por el usuario

Kotlin + Compose; diseño propio moderno; informe a cargo de otra persona. No existen todavía Firebase ni GitHub para este proyecto.

## Pendiente prioritario

1. Abrir Excel para completar revisión de rúbrica y alcance de compraventa.
2. Sincronizar y compilar en Android Studio; resolver cualquier incompatibilidad antes de continuar funciones.
3. Crear Firebase y broker privados, sin activar facturación sin acuerdo.
4. Conectar Auth/perfil a UI con ViewModel y estado de pertenencia pendiente.
5. Probar reglas en Emulator Suite y ajustar.
6. Implementar chat/anuncios/FCM, luego coordinar MQTT con historial y avisos.
7. Crear remoto GitHub y subir historial local.

## Validación

Siete pruebas de dominio aprobadas con Kotlin/JUnit ejecutados directamente. Build Android bloqueado por restricciones de conexión al daemon. No hay pruebas de integración ni APK generado. Ver `04-pruebas.md` para resultados y límites.

Para obtener el último commit real: `git log -1 --oneline`. No fijar un SHA dentro del mismo commit que se está creando.
