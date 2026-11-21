# Insumos técnicos para la persona que redacta el informe

Este archivo no es el informe. Registra qué se puede afirmar sobre la implementación y qué evidencia falta. Actualizar al cerrar cada etapa.

## Se puede documentar ahora

- Tecnología elegida: Kotlin con Jetpack Compose, confirmada por Jonathan frente a la referencia Java del enunciado.
- Responsabilidades separadas de Firebase Auth, RTDB, FCM y MQTT: ver arquitectura.
- Modelos de residente y alerta, UID frente a vivienda, UUID y tópicos por comunidad.
- Adaptadores iniciales para Auth/RTDB y MQTT. Están escritos, pero no conectados a la UI ni validados con servicios.
- Propuesta de reglas de base, aislamiento por comunidad y asignación de rol desde entorno confiable.
- Historial Git local por etapas.

## No afirmar todavía

- Que la aplicación ya cumple toda la rúbrica (el Excel está pendiente de lectura).
- Que funciona la comunicación en dos dispositivos, FCM o MQTT real.
- Que llega una alarma siempre con la app cerrada.
- Que los permisos y reglas ya se comprobaron.
- Que existen resultados de usabilidad/rendimiento o un APK compilado.

## Evidencia que entregaremos

Diseño de datos y flujo, decisiones de seguridad, fuentes, manual de configuración, matriz de pruebas ejecutadas, capturas reales, limitaciones, versiones y commits. No inventar resultados ni completar el informe con pruebas pendientes como aprobadas.

Revisar `04-pruebas.md` y `07-checkpoint.md` antes de redactar conclusiones.
